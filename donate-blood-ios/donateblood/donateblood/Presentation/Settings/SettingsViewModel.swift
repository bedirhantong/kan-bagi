import Foundation

@MainActor
final class SettingsViewModel: ObservableObject {
    @Published private(set) var isWorking = false
    @Published var error: AppError?

    private let deleteAccount: DeleteAccountUseCase

    init(
        deleteAccount: DeleteAccountUseCase
    ) {
        self.deleteAccount = deleteAccount
    }

    func signOut(session: SessionStore) async {
        await perform { try await session.signOut() }
    }

    func deleteMyAccount() async {
        await perform { try await self.deleteAccount() }
    }

    private func perform(_ work: () async throws -> Void) async {
        guard !isWorking else { return }
        isWorking = true
        defer { isWorking = false }
        do { try await work() } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }
}

@MainActor
final class NotificationSettingsViewModel: ObservableObject {
    @Published private(set) var preferences = NotificationPreferences.default
    @Published private(set) var selectedHospitals: [Hospital] = []
    /// Seçili il/ilçelerin adlarını göstermek için (yüklenemezse özet sayıyla gösterilir).
    @Published private(set) var areaCatalog = NotificationAreaCatalog([])
    @Published private(set) var state: LoadState = .idle
    @Published var error: AppError?

    let getHospitals: GetHospitalsUseCase
    private let getPreferences: GetNotificationPreferencesUseCase
    private let updatePreferences: UpdateNotificationPreferencesUseCase
    private let getHospitalsByIds: GetHospitalsByIdsUseCase
    private let getAreas: GetNotificationAreasUseCase
    private let suggestArea: SuggestNotificationAreaUseCase
    private let push: any PushPermissionRequesting

    init(
        getPreferences: GetNotificationPreferencesUseCase,
        updatePreferences: UpdateNotificationPreferencesUseCase,
        getHospitalsByIds: GetHospitalsByIdsUseCase,
        getHospitals: GetHospitalsUseCase,
        getAreas: GetNotificationAreasUseCase,
        suggestArea: SuggestNotificationAreaUseCase,
        push: any PushPermissionRequesting
    ) {
        self.getPreferences = getPreferences
        self.updatePreferences = updatePreferences
        self.getHospitalsByIds = getHospitalsByIds
        self.getHospitals = getHospitals
        self.getAreas = getAreas
        self.suggestArea = suggestArea
        self.push = push
    }

    /// Seçili il/ilçeler (katalogdaki adlarıyla).
    var selectedAreas: [NotificationArea] {
        NotificationAreaSelection(keys: preferences.preferredAreaKeys).selectedAreas(in: areaCatalog)
    }

    /// İlan bildirimleri açık ama hiç bölge/hastane seçilmemiş: kullanıcı ilan bildirimi almaz, uyarılmalı.
    var isMissingScope: Bool {
        preferences.pushEnabled && preferences.bloodTypeAlerts && !preferences.hasScope
    }

    /// Hastane seçiminde kalan yer (il/ilçelerle aynı sınırı paylaşır).
    var hospitalLimit: Int { NotificationScope.maxSelections - preferences.preferredAreaKeys.count }

    /// Bölge seçim ekranının modeli: her değişiklik hemen kaydedilir, başarısızsa geri alınır.
    func makeAreaPicker() -> NotificationAreaPickerModel {
        NotificationAreaPickerModel(
            getAreas: getAreas, suggestArea: suggestArea,
            selectedKeys: preferences.preferredAreaKeys,
            otherSelections: preferences.preferredHospitalIds.count,
            persist: { [weak self] keys in
                guard let self else { return false }
                await self.update { $0.preferredAreaKeys = keys }
                return self.preferences.preferredAreaKeys == keys
            })
    }

    func load() async {
        state = .loading
        do {
            preferences = try await getPreferences()
            selectedHospitals = (try? await getHospitalsByIds(ids: Array(preferences.preferredHospitalIds))) ?? []
            // Bölge adları için (küçük liste); alınamazsa özet sayıyla gösterilir.
            if let catalog = try? await getAreas() { areaCatalog = catalog }
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { state = .failed(appError) }
        }
    }

    /// İyimser güncelleme; sunucu reddederse eski değere döner.
    func update(_ change: (inout NotificationPreferences) -> Void) async {
        let previous = preferences
        var next = preferences
        change(&next)
        guard next != previous else { return }
        preferences = next
        do {
            try await updatePreferences(next)
            if next.pushEnabled { await push.requestAuthorization() }
        } catch {
            preferences = previous
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }

    func setHospitals(_ hospitals: [Hospital]) async {
        let previous = selectedHospitals
        selectedHospitals = hospitals
        await update { $0.preferredHospitalIds = Set(hospitals.map(\.id)) }
        if preferences.preferredHospitalIds != Set(hospitals.map(\.id)) { selectedHospitals = previous }
    }
}
