import Foundation

struct GetProfileUseCase: Sendable {
    let profiles: ProfileRepository
    func callAsFunction() async throws -> Profile { try await profiles.myProfile() }
}

struct UpdateProfileUseCase: Sendable {
    let profiles: ProfileRepository

    func callAsFunction(_ update: ProfileUpdate) async throws -> Profile {
        try Self.ensureValid(update, isRegistration: false)
        return try await profiles.updateMyProfile(update, markCompleted: false)
    }

    /// İlk hatalı alanı `AppError.invalidInput` olarak fırlatır (arayüz alan bazlı hatayı `ProfileValidator`'dan okur).
    static func ensureValid(_ update: ProfileUpdate, isRegistration: Bool) throws {
        let errors = ProfileValidator.validate(update, isRegistration: isRegistration)
        for (field, key) in [(ProfileField.name, "profile.name"), (.surname, "profile.surname"),
                             (.phone, "profile.phone"), (.birthDate, "profile.birthDate")] where errors[field] != nil {
            throw AppError.invalidInput(L10n.string(key))
        }
    }
}

/// Kayıt: profil ilk kez tamamlanır. Kan grubu bilinmiyorsa `nil` olabilir (uyumlu ilan bildirimi gelmez,
/// kullanıcı sonradan ekleyebilir); ad, soyad ve 18 yaş şartını taşıyan doğum tarihi zorunludur.
struct CompleteProfileUseCase: Sendable {
    let profiles: ProfileRepository

    func callAsFunction(_ update: ProfileUpdate) async throws -> Profile {
        try UpdateProfileUseCase.ensureValid(update, isRegistration: true)
        return try await profiles.updateMyProfile(update, markCompleted: true)
    }
}
