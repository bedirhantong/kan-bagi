import Foundation
import UIKit
import GoogleSignIn

/// GoogleSignIn SDK'sı ile native Google girişi yapar.
final class GoogleCredentialProvider: IdentityCredentialProvider {
    func credential() async throws -> IdentityCredential {
        do {
            return try await signIn(nonce: Nonce.random())
        } catch let error as AppError {
            throw error
        } catch {
            if (error as NSError).domain == kGIDSignInErrorDomain,
               (error as NSError).code == GIDSignInError.canceled.rawValue {
                throw AppError.cancelled
            }
            Log.auth.error("Google girişi başarısız: \(String(describing: error))")
            throw AppError.authenticationFailed
        }
    }

    @MainActor
    private func signIn(nonce: Nonce) async throws -> IdentityCredential {
        guard let presenter = UIApplication.topViewController() else { throw AppError.authenticationFailed }
        let result = try await GIDSignIn.sharedInstance.signIn(
            withPresenting: presenter, hint: nil, additionalScopes: nil, nonce: nonce.hashed
        )
        guard let idToken = result.user.idToken?.tokenString else { throw AppError.authenticationFailed }
        let profile = result.user.profile
        return IdentityCredential(
            provider: .google, idToken: idToken, nonce: nonce.raw,
            accessToken: result.user.accessToken.tokenString,
            fullName: PersonName(given: profile?.givenName, family: profile?.familyName)
        )
    }
}

extension UIApplication {
    /// Sunum için en üstteki view controller.
    @MainActor
    static func topViewController() -> UIViewController? {
        let scene = shared.connectedScenes.compactMap { $0 as? UIWindowScene }.first { $0.activationState == .foregroundActive }
        var top = scene?.windows.first(where: \.isKeyWindow)?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
