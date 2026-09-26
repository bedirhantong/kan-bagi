import Foundation

/// Kimliği doğrulanmış (misafir dahil) oturum sahibi.
struct AuthUser: Equatable, Sendable {
    let id: UUID
    let isGuest: Bool
}

enum AuthState: Equatable, Sendable {
    case signedOut
    case signedIn(AuthUser)
}

enum UserType: String, Sendable {
    case regularUser = "REGULAR_USER"
    case guest = "GUEST"
}

struct Profile: Identifiable, Equatable, Sendable {
    let id: UUID
    var name: String?
    var surname: String?
    var email: String?
    var phoneNumber: String?
    var birthDate: Date?
    var bloodType: BloodType?
    var gender: Gender?
    var userType: UserType
    var isProfileCompleted: Bool
    var createdAt: Date?

    var fullName: String {
        [name, surname].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " ")
    }
}

/// Profil güncelleme girdisi.
struct ProfileUpdate: Sendable {
    var name: String
    var surname: String
    var phoneNumber: String?
    var birthDate: Date?
    var bloodType: BloodType?
    var gender: Gender?
}

/// Apple/Google'dan alınan kimlik bilgisi.
struct IdentityCredential: Sendable {
    enum Provider: Sendable { case apple, google }
    let provider: Provider
    let idToken: String
    let nonce: String?
    let accessToken: String?
    /// Sadece Apple ilk girişte ad-soyad döner.
    let fullName: PersonName?
}

struct PersonName: Sendable {
    let given: String?
    let family: String?
}
