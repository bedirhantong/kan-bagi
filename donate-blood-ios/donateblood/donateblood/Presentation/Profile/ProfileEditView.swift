import SwiftUI

/// Profil düzenleme. Kayıt akışıyla aynı kurallar: sağlık bilgileri varsayılanla doldurulmaz
/// (bilinmeyen kan grubu "Bilmiyorum", belirtilmemiş cinsiyet boş kalır); hatalar alan altında gösterilir.
@MainActor
final class ProfileEditViewModel: ObservableObject {
    @Published var name: String
    @Published var surname: String
    @Published var phone: String
    @Published var bloodType: BloodType?
    @Published var gender: Gender?
    @Published var birthDate: Date?
    @Published private(set) var isSaving = false
    @Published private(set) var didSave = false
    @Published private(set) var hasTriedToSave = false
    @Published var error: AppError?

    private let updateProfile: UpdateProfileUseCase
    private let onSaved: @MainActor (Profile) -> Void

    init(
        updateProfile: UpdateProfileUseCase,
        profile: Profile,
        onSaved: @escaping @MainActor (Profile) -> Void
    ) {
        self.updateProfile = updateProfile
        self.onSaved = onSaved
        name = profile.name ?? ""
        surname = profile.surname ?? ""
        phone = profile.phoneNumber ?? ""
        bloodType = profile.bloodType
        gender = profile.gender
        birthDate = profile.birthDate
    }

    /// Doğum tarihi hiç girilmemişse seçicinin açılacağı makul başlangıç (25 yaş).
    var suggestedBirthDate: Date {
        Calendar.current.date(byAdding: .year, value: -25, to: .now) ?? .now
    }

    private var update: ProfileUpdate {
        ProfileUpdate(
            name: name.trimmingCharacters(in: .whitespacesAndNewlines),
            surname: surname.trimmingCharacters(in: .whitespacesAndNewlines),
            phoneNumber: phone.trimmingCharacters(in: .whitespaces).isEmpty ? nil : phone,
            birthDate: birthDate, bloodType: bloodType, gender: gender)
    }

    func error(for field: ProfileField) -> ProfileValidationError? {
        guard hasTriedToSave else { return nil }
        return ProfileValidator.validate(update, isRegistration: false)[field]
    }

    func save() async {
        hasTriedToSave = true
        guard ProfileValidator.validate(update, isRegistration: false).isEmpty else {
            Haptics.warning()
            return
        }
        isSaving = true
        defer { isSaving = false }
        do {
            onSaved(try await updateProfile(update))
            didSave = true
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }
}

struct ProfileEditView: View {
    @StateObject var viewModel: ProfileEditViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    field("profile.name", error: viewModel.error(for: .name)) {
                        TextField("profile.name", text: $viewModel.name).textContentType(.givenName)
                    }
                    field("profile.surname", error: viewModel.error(for: .surname)) {
                        TextField("profile.surname", text: $viewModel.surname).textContentType(.familyName)
                    }
                    field("profile.phone", error: viewModel.error(for: .phone)) {
                        TextField("profile.phone", text: $viewModel.phone).keyboardType(.phonePad).textContentType(.telephoneNumber)
                    }
                }
                Section {
                    Picker("profile.bloodType", selection: $viewModel.bloodType) {
                        Text("registration.bloodType.unknown").tag(BloodType?.none)
                        ForEach(BloodType.allCases) { Text(verbatim: $0.displayName).tag(BloodType?.some($0)) }
                    }
                    Picker("profile.gender", selection: $viewModel.gender) {
                        Text("registration.gender.unspecified").tag(Gender?.none)
                        ForEach(Gender.allCases) { Text($0.localizedName).tag(Gender?.some($0)) }
                    }
                    if let birthDate = viewModel.birthDate {
                        DatePicker("profile.birthDate",
                                   selection: Binding(get: { birthDate }, set: { viewModel.birthDate = $0 }),
                                   in: ...Date.now, displayedComponents: .date)
                    } else {
                        // Tarih hiç girilmemişse varsayılan uydurmayız; kullanıcı dokununca seçici açılır.
                        Button { viewModel.birthDate = viewModel.suggestedBirthDate } label: {
                            LabeledContent("profile.birthDate") { Text("registration.select").foregroundStyle(Theme.brand) }
                        }
                        .tint(.primary)
                    }
                } footer: { Text("registration.privacy") }
            }
            .navigationTitle("profile.edit")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("common.cancel") { dismiss() }.tint(.primary) }
                ToolbarItem(placement: .confirmationAction) {
                    if viewModel.isSaving {
                        ProgressView()
                    } else {
                        Button("common.save") { Task { await viewModel.save() } }.fontWeight(.semibold).tint(Theme.brand)
                    }
                }
            }
            .onChange(of: viewModel.didSave) { if $0 { dismiss() } }
            .errorAlert($viewModel.error)
        }
        .interactiveDismissDisabled(viewModel.isSaving)
    }

    /// Alan + (varsa) altında hata metni.
    private func field<Content: View>(_ label: LocalizedStringKey, error: ProfileValidationError?, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
            content()
            if let error {
                Label(error.message, systemImage: "exclamationmark.circle.fill").font(.footnote).foregroundStyle(.red)
            }
        }
        .accessibilityElement(children: .contain)
    }
}
