import Foundation
import AuthenticationServices

/// "Apple ile Giriş" isteğinin tek sorumlusu: nonce üretir, isteği hazırlar ve sonucu domain kimliğine çevirir.
/// ViewModel, nonce/AuthenticationServices ayrıntısını bilmez.
@MainActor
final class AppleSignInFlow {
    private var nonce: Nonce?

    /// `SignInWithAppleButton(onRequest:)` içinde çağrılır.
    func prepare(_ request: ASAuthorizationAppleIDRequest) {
        let nonce = Nonce.random()
        self.nonce = nonce
        request.requestedScopes = [.fullName, .email]
        request.nonce = nonce.hashed
    }

    /// Kullanıcı iptal ettiyse `AppError.cancelled` fırlatır.
    func credential(from result: Result<ASAuthorization, Error>) throws -> IdentityCredential {
        defer { nonce = nil } // her nonce yalnızca bir kez kullanılır
        switch result {
        case .failure(let error):
            throw AppleCredentialMapper.map(error)
        case .success(let authorization):
            guard let nonce else { throw AppError.authenticationFailed }
            return try AppleCredentialMapper.credential(from: authorization, nonce: nonce)
        }
    }
}
