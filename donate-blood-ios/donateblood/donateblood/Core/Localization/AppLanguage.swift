import Foundation

/// Uygulama dili. `system` telefonun dilini izler; diğerleri telefondan bağımsız olarak zorlanır.
enum AppLanguage: String, CaseIterable, Identifiable, Sendable {
    case system
    case turkish = "tr"
    case english = "en"

    var id: String { rawValue }

    /// Zorlanan dil kodu; `system` için `nil`.
    var languageCode: String? { self == .system ? nil : rawValue }

    /// Dil adı her zaman kendi dilinde gösterilir (Türkçe / English), seçili dilden bağımsız.
    var nativeName: String? {
        switch self {
        case .system: return nil
        case .turkish: return "Türkçe"
        case .english: return "English"
        }
    }

    /// Biçimlendiriciler (tarih, mesafe) ve SwiftUI ortamı için yerel ayar.
    var locale: Locale {
        guard let languageCode else { return .autoupdatingCurrent }
        // Bölge (ör. ölçü birimi, tarih sırası) telefondan korunur, yalnızca dil değişir.
        let region = Locale.current.region?.identifier
        return Locale(identifier: region.map { "\(languageCode)_\($0)" } ?? languageCode)
    }
}
