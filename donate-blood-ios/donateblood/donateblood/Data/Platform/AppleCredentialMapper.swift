import Foundation
import AuthenticationServices

enum AppleCredentialMapper {
    static func credential(from authorization: ASAuthorization, nonce: Nonce) throws -> IdentityCredential {
        guard
            let apple = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = apple.identityToken,
            let token = String(data: tokenData, encoding: .utf8)
        else { throw AppError.authenticationFailed }

        let hasName = apple.fullName?.givenName != nil || apple.fullName?.familyName != nil
        return IdentityCredential(
            provider: .apple, idToken: token, nonce: nonce.raw, accessToken: nil,
            fullName: hasName ? PersonName(given: apple.fullName?.givenName, family: apple.fullName?.familyName) : nil
        )
    }

    /// Kullanıcı iptal ettiyse `.cancelled`, aksi halde `.authenticationFailed`.
    static func map(_ error: Error) -> AppError {
        if let authError = error as? ASAuthorizationError, authError.code == .canceled { return .cancelled }
        return .authenticationFailed
    }
}
