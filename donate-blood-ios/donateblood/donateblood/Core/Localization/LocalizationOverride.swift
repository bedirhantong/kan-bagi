import Foundation
import ObjectiveC

/// Uygulama içi dil seçimi: ana paketin (`Bundle.main`) yerelleştirme aramasını seçilen dilin `.lproj` paketine yönlendirir.
///
/// Neden gerekli: SwiftUI metinleri ortam `locale`'ini izler, fakat `String(localized:)` ve UIKit, uygulama
/// açılırken belirlenen dili kullanır. Bu yönlendirme olmadan hata mesajları gibi koddan üretilen metinler
/// telefon dilinde kalırdı. Yeniden başlatma gerektirmez.
enum LocalizationOverride {
    private static let lock = NSLock()
    nonisolated(unsafe) private static var overrideBundle: Bundle?
    nonisolated(unsafe) private static var currentLanguage: AppLanguage = .system
    nonisolated(unsafe) private static var isInstalled = false

    /// Seçilen dil (formatlayıcılar için yerel ayar da buradan okunur).
    static var language: AppLanguage { lock.withLock { currentLanguage } }
    static var locale: Locale { language.locale }

    static func apply(_ language: AppLanguage) {
        lock.withLock {
            if !isInstalled {
                object_setClass(Bundle.main, OverridingBundle.self)
                isInstalled = true
            }
            currentLanguage = language
            overrideBundle = language.languageCode
                .flatMap { Bundle.main.path(forResource: $0, ofType: "lproj") }
                .flatMap(Bundle.init(path:))
        }
        // Sistem pencereleri (izin istekleri vb.) ve bir sonraki açılış için.
        if let code = language.languageCode {
            UserDefaults.standard.set([code], forKey: "AppleLanguages")
        } else {
            UserDefaults.standard.removeObject(forKey: "AppleLanguages")
        }
    }

    fileprivate static var bundle: Bundle? { lock.withLock { overrideBundle } }
}

/// `Bundle.main`'in sınıfı çalışma zamanında bununla değiştirilir; yalnızca yerelleştirme aramasını yönlendirir.
private final class OverridingBundle: Bundle, @unchecked Sendable {
    override func localizedString(forKey key: String, value: String?, table tableName: String?) -> String {
        if let bundle = LocalizationOverride.bundle {
            return bundle.localizedString(forKey: key, value: value, table: tableName)
        }
        return super.localizedString(forKey: key, value: value, table: tableName)
    }
}
