import Foundation
import Combine

/// Kullanıcının dil tercihini saklar ve uygular. Kök görünüm `locale` ve `language` değişince yeniden kurulur.
@MainActor
final class LanguageStore: ObservableObject {
    @Published private(set) var language: AppLanguage

    private let defaults: UserDefaults
    private static let key = "appLanguage"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        language = AppLanguage(rawValue: defaults.string(forKey: Self.key) ?? "") ?? .system
        LocalizationOverride.apply(language)
    }

    var locale: Locale { language.locale }

    func select(_ language: AppLanguage) {
        guard language != self.language else { return }
        defaults.set(language.rawValue, forKey: Self.key)
        LocalizationOverride.apply(language)
        self.language = language
    }
}
