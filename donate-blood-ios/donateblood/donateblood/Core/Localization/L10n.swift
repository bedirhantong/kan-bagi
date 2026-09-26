import Foundation

/// Koddan üretilen (SwiftUI `Text` dışındaki) yerelleştirilmiş metinler için tek giriş noktası.
///
/// `String(localized:)` yerine bunu kullanın: Swift'in `String(localized:)`'i paketi kendi içinde çözdüğü için
/// uygulama içi dil seçimini (`LocalizationOverride`) izlemez; `Bundle.localizedString` izler.
/// Parametreli anahtarlar katalogdaki biçimdedir: `"error.server %@"`, `"notificationSettings.hospitals.count %lld"`.
enum L10n {
    static func string(_ key: String) -> String {
        Bundle.main.localizedString(forKey: key, value: nil, table: nil)
    }

    static func format(_ key: String, _ arguments: CVarArg...) -> String {
        String(format: string(key), locale: LocalizationOverride.locale, arguments: arguments)
    }
}
