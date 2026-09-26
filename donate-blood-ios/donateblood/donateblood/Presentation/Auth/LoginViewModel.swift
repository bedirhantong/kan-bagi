import Foundation
import AuthenticationServices

/// Giriş yöntemleri. Aynı anda tek bir giriş yürür; yükleme göstergesi yalnızca ilgili düğmede görünür.
enum AuthProvider: Equatable, Sendable {
    case apple, google, guest, email
}

@MainActor
final class LoginViewModel: ObservableObject {
    @Published private(set) var activeProvider: AuthProvider?
    @Published var error: AppError?
    @Published var email = ""
    @Published var password = ""
    /// Geliştirici girişi (test hesapları, e-posta/şifre) yalnızca yapılandırma izin veriyorsa görünür.
    let isEmailLoginEnabled: Bool

    private let signInWithApple: SignInWithAppleUseCase
    private let signInWithGoogle: SignInWithGoogleUseCase
    private let signInWithEmail: SignInWithEmailUseCase
    private let signInAsGuest: SignInAsGuestUseCase
    private let appleFlow: AppleSignInFlow

    init(
        signInWithApple: SignInWithAppleUseCase,
        signInWithGoogle: SignInWithGoogleUseCase,
        signInWithEmail: SignInWithEmailUseCase,
        signInAsGuest: SignInAsGuestUseCase,
        appleFlow: AppleSignInFlow = AppleSignInFlow(),
        isEmailLoginEnabled: Bool
    ) {
        self.signInWithApple = signInWithApple
        self.signInWithGoogle = signInWithGoogle
        self.signInWithEmail = signInWithEmail
        self.signInAsGuest = signInAsGuest
        self.appleFlow = appleFlow
        self.isEmailLoginEnabled = isEmailLoginEnabled
    }

    var isLoading: Bool { activeProvider != nil }
    var canSubmitEmail: Bool { !email.trimmingCharacters(in: .whitespaces).isEmpty && !password.isEmpty && !isLoading }

    // MARK: Apple

    func prepareApple(_ request: ASAuthorizationAppleIDRequest) { appleFlow.prepare(request) }

    func handleApple(_ result: Result<ASAuthorization, Error>) async {
        await run(.apple) {
            let credential = try self.appleFlow.credential(from: result)
            try await self.signInWithApple(credential)
        }
    }

    // MARK: Diğer yöntemler

    func google() async { await run(.google) { try await self.signInWithGoogle() } }
    func guest() async { await run(.guest) { try await self.signInAsGuest() } }

    /// Hazır test hesabıyla tek dokunuşla giriş. Yalnızca geliştirici girişi açıkken çalışır.
    func signIn(as account: TestAccount) async {
        guard isEmailLoginEnabled else { return }
        await run(.email) { try await self.signInWithEmail(email: account.email, password: account.password) }
    }

    func emailSignIn() async {
        guard isEmailLoginEnabled else { return }
        await run(.email) { try await self.signInWithEmail(email: self.email, password: self.password) }
        if error == nil { password = "" }
    }

    private func run(_ provider: AuthProvider, _ work: () async throws -> Void) async {
        guard activeProvider == nil else { return }
        activeProvider = provider
        defer { activeProvider = nil }
        do {
            try await work()
            Haptics.success()
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError } // iptal sessizdir
        }
    }
}
