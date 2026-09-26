import Foundation

/// İlk girişten sonra profili tamamlatan dört adımlı kayıt akışı:
/// 1) kişisel bilgiler, 2) bağışçı bilgileri, 3) bildirim bölgeleri (profil burada kaydedilir),
/// 4) bildirim izni (isteğe bağlı).
///
/// Kurallar (bir sağlık uygulaması olduğu için):
/// - Kan grubu, doğum tarihi ve cinsiyet **önceden seçili gelmez**; kullanıcı bilerek seçer.
/// - Kan grubunu bilmeyen "Bilmiyorum" diyerek devam edebilir.
/// - Hatalar kullanıcı "Devam"a bastıktan sonra, ilgili alanın altında gösterilir.
/// - Bildirim yalnızca seçilen il/ilçelerden gelir; bölge seçmeyen kullanıcıya ilan bildirimi gitmez.
/// - Profil, bölge adımında tamamlanır: uygulama arada kapanırsa kullanıcı bu adımı atlamış olmaz.
///
/// `SessionStore`'u tanımaz: bitiş ve çıkış işlemleri dışarıdan verilir (bağımlılığın tersine çevrilmesi).
@MainActor
final class RegistrationViewModel: ObservableObject {
    enum Step: Int, CaseIterable, Comparable {
        case personal, donor, areas, notifications
        static func < (lhs: Step, rhs: Step) -> Bool { lhs.rawValue < rhs.rawValue }
    }

    enum BloodTypeChoice: Equatable, Hashable {
        case known(BloodType)
        case unknown

        var bloodType: BloodType? {
            if case .known(let type) = self { return type }
            return nil
        }
    }

    @Published private(set) var step: Step = .personal
    @Published var name: String
    @Published var surname: String
    @Published var phone: String
    @Published var bloodType: BloodTypeChoice?
    @Published var birthDate: Date?
    @Published var gender: Gender?
    @Published private(set) var isSaving = false
    @Published private(set) var isFinishing = false
    @Published var error: AppError?
    /// Hataları gösterilen adımlar (kullanıcı en az bir kez "Devam"a bastı).
    @Published private(set) var validatedSteps: Set<Step> = []

    let birthDateRange: ClosedRange<Date>
    /// Doğum tarihi seçici ilk açıldığında gösterilen tarih (kaydedilmez).
    let suggestedBirthDate: Date
    /// Bölge adımındaki seçici (arama, konumdan öneri, sınır).
    let areaPicker: NotificationAreaPickerModel

    private let completeProfile: CompleteProfileUseCase
    private let setAreas: SetNotificationAreasUseCase
    private let push: any PushPermissionRequesting
    private let onCompleted: @MainActor (Profile) async -> Void
    private let onSignOut: @MainActor () async -> Void
    private var savedProfile: Profile?

    init(
        profile: Profile,
        completeProfile: CompleteProfileUseCase,
        setAreas: SetNotificationAreasUseCase,
        areaPicker: NotificationAreaPickerModel,
        push: any PushPermissionRequesting,
        now: Date = .now,
        calendar: Calendar = .current,
        initialStep: Step = .personal,
        onCompleted: @escaping @MainActor (Profile) async -> Void,
        onSignOut: @escaping @MainActor () async -> Void
    ) {
        self.completeProfile = completeProfile
        self.setAreas = setAreas
        self.areaPicker = areaPicker
        self.push = push
        self.onCompleted = onCompleted
        self.onSignOut = onSignOut
        step = initialStep
        // Apple/Google'dan gelen ad önceden doldurulur; sağlık bilgileri asla varsayılanla doldurulmaz.
        name = profile.name ?? ""
        surname = profile.surname ?? ""
        phone = profile.phoneNumber ?? ""
        bloodType = profile.bloodType.map(BloodTypeChoice.known)
        birthDate = profile.birthDate
        gender = profile.gender
        let latest = calendar.date(byAdding: .year, value: -18, to: now) ?? now
        let earliest = calendar.date(byAdding: .year, value: -100, to: now) ?? now
        birthDateRange = earliest...latest
        suggestedBirthDate = calendar.date(byAdding: .year, value: -25, to: now) ?? latest
    }

    // MARK: Durum

    var progress: Double { Double(step.rawValue + 1) / Double(Step.allCases.count) }
    var canGoBack: Bool { (step == .donor || step == .areas) && !isSaving }

    private var update: ProfileUpdate {
        ProfileUpdate(
            name: name.trimmingCharacters(in: .whitespacesAndNewlines),
            surname: surname.trimmingCharacters(in: .whitespacesAndNewlines),
            phoneNumber: phone.trimmingCharacters(in: .whitespaces).isEmpty ? nil : phone,
            birthDate: birthDate, bloodType: bloodType?.bloodType, gender: gender)
    }

    private var allErrors: [ProfileField: ProfileValidationError] {
        ProfileValidator.validate(update, isRegistration: true)
    }

    /// Alanın hatası (yalnızca kullanıcı o adımda "Devam"a bastıysa).
    func error(for field: ProfileField) -> ProfileValidationError? {
        guard validatedSteps.contains(Self.step(of: field)) else { return nil }
        return allErrors[field]
    }

    var isBloodTypeMissing: Bool { validatedSteps.contains(.donor) && bloodType == nil }

    private static func step(of field: ProfileField) -> Step {
        switch field {
        case .name, .surname, .phone: return .personal
        case .birthDate: return .donor
        }
    }

    // MARK: Eylemler

    func continueFromPersonal() {
        validatedSteps.insert(.personal)
        let errors = allErrors
        guard errors[.name] == nil, errors[.surname] == nil, errors[.phone] == nil else {
            Haptics.warning()
            return
        }
        step = .donor
    }

    /// Bağışçı bilgilerini doğrular; kaydetmez (profil bölge adımında tamamlanır).
    func continueFromDonor() {
        validatedSteps.insert(.donor)
        guard bloodType != nil, allErrors.isEmpty else {
            Haptics.warning()
            return
        }
        step = .areas
    }

    /// Seçilen bölgeleri ve profili kaydeder, bildirim izni adımına geçer.
    func continueFromAreas() async {
        guard !areaPicker.selectedKeys.isEmpty else {
            Haptics.warning()
            return
        }
        await save(areas: areaPicker.selectedKeys)
    }

    /// Bölge seçmeden devam: profil kaydedilir, ilan bildirimi gelmez (sonra Ayarlar'dan eklenebilir).
    func skipAreas() async { await save(areas: nil) }

    private func save(areas: Set<String>?) async {
        guard !isSaving else { return }
        isSaving = true
        defer { isSaving = false }
        do {
            // Önce bölgeler: profil tamamlanınca akış kapanır, bölgeler yarım kalmasın.
            if let areas { try await setAreas(areas) }
            savedProfile = try await completeProfile(update)
            step = .notifications
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }

    func goBack() {
        guard canGoBack else { return }
        switch step {
        case .donor: step = .personal
        case .areas: step = .donor
        case .personal, .notifications: break
        }
    }

    func enableNotifications() async {
        await push.requestAuthorization()
        await finish()
    }

    func skipNotifications() async { await finish() }

    func signOut() async { await onSignOut() }

    private func finish() async {
        guard let savedProfile, !isFinishing else { return }
        isFinishing = true
        Haptics.success()
        await onCompleted(savedProfile)
    }
}
