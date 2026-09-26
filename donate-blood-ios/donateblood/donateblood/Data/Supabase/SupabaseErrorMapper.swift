import Foundation
import Supabase

/// SDK hatalarını domain'in `AppError`'ına çevirir.
enum SupabaseErrorMapper {
    static func map(_ error: Error) -> AppError {
        if let appError = error as? AppError { return appError }
        if error is CancellationError { return .cancelled }
        if let urlError = error as? URLError {
            return urlError.code == .cancelled ? .cancelled : .network
        }
        if let postgrest = error as? PostgrestError {
            switch postgrest.code {
            case "42501":
                return postgrest.message.contains("guest_not_allowed") ? .guestNotAllowed : .forbidden
            case "PGRST116": return .notFound
            // İlan sınırları (13_request_limits_patch.sql, enforce_request_limits tetikleyicisi).
            case "P0001" where postgrest.message.contains("active_request_limit"): return .activeRequestLimit
            case "P0001" where postgrest.message.contains("daily_request_limit"): return .dailyRequestLimit
            case "PGRST301", "PGRST302": return .authenticationFailed
            default:
                Log.data.error("Postgrest hatası: \(postgrest.code ?? "-") \(postgrest.message)")
                return .server(postgrest.message)
            }
        }
        if let authError = error as? AuthError {
            return authError.errorCode == .invalidCredentials ? .invalidCredentials : .authenticationFailed
        }
        Log.data.error("Beklenmeyen hata: \(String(describing: error))")
        return .unknown
    }
}

extension SupabaseClient {
    /// Oturum açmış kullanıcının kimliği; yoksa `authenticationFailed`.
    func requireUserId() async throws -> UUID {
        do { return try await auth.session.user.id } catch { throw AppError.authenticationFailed }
    }
}
