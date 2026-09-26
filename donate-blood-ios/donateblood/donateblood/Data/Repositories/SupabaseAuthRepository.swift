import Foundation
import Supabase

final class SupabaseAuthRepository: AuthRepository {
    private let client: SupabaseClient

    init(client: SupabaseClient) { self.client = client }

    var currentUser: AuthUser? { client.auth.currentUser.map(Self.map) }

    func authStateStream() -> AsyncStream<AuthState> {
        let auth = client.auth
        return AsyncStream { continuation in
            let task = Task {
                for await (event, session) in auth.authStateChanges {
                    // emitLocalSessionAsInitialSession=true iken yerel oturum süresi dolmuş olabilir:
                    // yenilemeyi dene, başarısızsa çıkış yapılmış say (aksi halde istekler anon gider).
                    if event == .initialSession, let session, session.isExpired {
                        do {
                            let fresh = try await auth.session
                            continuation.yield(.signedIn(Self.map(fresh.user)))
                        } catch {
                            Log.auth.notice("Süresi dolmuş oturum yenilenemedi: \(String(describing: error))")
                            continuation.yield(.signedOut)
                        }
                        continue
                    }
                    continuation.yield(session.map { .signedIn(Self.map($0.user)) } ?? .signedOut)
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }

    func signIn(with credential: IdentityCredential) async throws {
        let credentials = OpenIDConnectCredentials(
            provider: credential.provider == .apple ? .apple : .google,
            idToken: credential.idToken,
            accessToken: credential.accessToken,
            nonce: credential.nonce
        )
        do {
            // Misafir hesabını Apple/Google kimliğine bağla: ilanlar ve veriler korunur.
            if currentUser?.isGuest == true {
                do {
                    try await client.auth.linkIdentityWithIdToken(credentials: credentials)
                    return
                } catch {
                    // Kimlik zaten başka bir hesaba bağlıysa normal girişe düş.
                    Log.auth.notice("Kimlik bağlama başarısız, normal girişe düşülüyor: \(String(describing: error))")
                }
            }
            try await client.auth.signInWithIdToken(credentials: credentials)
        } catch {
            throw SupabaseErrorMapper.map(error)
        }
    }

    func signInAsGuest() async throws {
        do { try await client.auth.signInAnonymously() } catch { throw SupabaseErrorMapper.map(error) }
    }

    func signIn(email: String, password: String) async throws {
        do { try await client.auth.signIn(email: email, password: password) } catch { throw SupabaseErrorMapper.map(error) }
    }

    func signOut() async throws {
        do { try await client.auth.signOut() } catch { throw SupabaseErrorMapper.map(error) }
    }

    func deleteAccount() async throws {
        do {
            try await client.rpc("delete_my_account").execute()
            // Sunucuda kullanıcı silindi; yerel oturumu temizle.
            try? await client.auth.signOut(scope: .local)
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    private static func map(_ user: User) -> AuthUser { AuthUser(id: user.id, isGuest: user.isAnonymous) }
}
