import Foundation

struct ObserveAuthStateUseCase: Sendable {
    let auth: AuthRepository
    func callAsFunction() -> AsyncStream<AuthState> { auth.authStateStream() }
}

struct SignInWithAppleUseCase: Sendable {
    let auth: AuthRepository
    let profiles: ProfileRepository
    func callAsFunction(_ credential: IdentityCredential) async throws {
        try await auth.signIn(with: credential)
        if let name = credential.fullName { try? await profiles.prefillNameIfEmpty(name) }
    }
}

struct SignInWithGoogleUseCase: Sendable {
    let auth: AuthRepository
    let profiles: ProfileRepository
    let googleProvider: IdentityCredentialProvider
    func callAsFunction() async throws {
        let credential = try await googleProvider.credential()
        try await auth.signIn(with: credential)
        if let name = credential.fullName { try? await profiles.prefillNameIfEmpty(name) }
    }
}

/// E-posta/şifre ile giriş: Apple/Google hesabı olmadan test edebilmek içindir (open source katılımcıları).
struct SignInWithEmailUseCase: Sendable {
    let auth: AuthRepository
    func callAsFunction(email: String, password: String) async throws {
        let email = email.trimmingCharacters(in: .whitespacesAndNewlines)
        guard Validators.isValidEmail(email) else { throw AppError.invalidInput(L10n.string("login.email")) }
        guard !password.isEmpty else { throw AppError.invalidInput(L10n.string("login.password")) }
        try await auth.signIn(email: email, password: password)
    }
}

struct SignInAsGuestUseCase: Sendable {
    let auth: AuthRepository
    func callAsFunction() async throws { try await auth.signInAsGuest() }
}

struct SignOutUseCase: Sendable {
    let auth: AuthRepository
    func callAsFunction() async throws { try await auth.signOut() }
}

struct DeleteAccountUseCase: Sendable {
    let auth: AuthRepository
    func callAsFunction() async throws { try await auth.deleteAccount() }
}

