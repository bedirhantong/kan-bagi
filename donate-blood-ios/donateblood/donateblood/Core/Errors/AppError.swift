import Foundation

/// Uygulama genelinde kullanılan, kullanıcıya gösterilebilir hata türü.
enum AppError: LocalizedError, Equatable {
    case network
    case authenticationFailed
    case invalidCredentials
    case cancelled
    case guestNotAllowed
    case notFound
    case forbidden
    case invalidInput(String)
    /// Bildirim bölgesi/hastane seçim sınırı aşıldı.
    case selectionLimit(Int)
    /// Aynı anda açık tutulabilecek ilan sınırı (`BloodRequestLimits.maxActive`).
    case activeRequestLimit
    /// 24 saatte açılabilecek ilan sınırı (`BloodRequestLimits.maxPerDay`).
    case dailyRequestLimit
    case server(String)
    case unknown

    var errorDescription: String? {
        switch self {
        case .network: return L10n.string("error.network")
        case .authenticationFailed: return L10n.string("error.authFailed")
        case .invalidCredentials: return L10n.string("error.invalidCredentials")
        case .cancelled: return nil
        case .guestNotAllowed: return L10n.string("error.guestNotAllowed")
        case .notFound: return L10n.string("error.notFound")
        case .forbidden: return L10n.string("error.forbidden")
        case .invalidInput(let field): return L10n.format("error.invalidInput %@", field)
        case .selectionLimit(let max): return L10n.format("error.selectionLimit %lld", max)
        case .activeRequestLimit: return L10n.format("error.activeRequestLimit %lld", BloodRequestLimits.maxActive)
        case .dailyRequestLimit: return L10n.format("error.dailyRequestLimit %lld", BloodRequestLimits.maxPerDay)
        case .server(let message): return L10n.format("error.server %@", message)
        case .unknown: return L10n.string("error.unknown")
        }
    }

    /// Herhangi bir hatayı `AppError`'a çevirir.
    static func from(_ error: Error) -> AppError {
        if let appError = error as? AppError { return appError }
        if error is CancellationError { return .cancelled }
        if let urlError = error as? URLError {
            return urlError.code == .cancelled ? .cancelled : .network
        }
        return .unknown
    }
}
