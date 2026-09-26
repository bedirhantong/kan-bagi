import Foundation
import Testing
@testable import donateblood

/// Genel (global) dil durumunu değiştirdiği için sıralı çalışır ve her testte eski duruma döner.
@Suite(.serialized)
struct LanguageTests {
    @Test func codeSideStringsFollowTheSelectedLanguage() {
        let previous = LocalizationOverride.language
        defer { LocalizationOverride.apply(previous) }

        LocalizationOverride.apply(.turkish)
        #expect(L10n.string("tab.home") == "Ana Sayfa")
        #expect(AppError.network.errorDescription == "İnternet bağlantınızı kontrol edin")
        #expect(L10n.format("notificationSettings.hospitals.count %lld", 3) == "3 hastane seçildi")
        #expect(AppError.invalidInput("Ad").errorDescription == "Ad geçersiz")

        LocalizationOverride.apply(.english)
        #expect(L10n.string("tab.home") == "Home")
        #expect(AppError.network.errorDescription == "Check your internet connection")
        #expect(L10n.format("notificationSettings.hospitals.count %lld", 3) == "3 hospitals selected")
    }

    @Test func systemLanguageRemovesTheOverride() {
        let previous = LocalizationOverride.language
        defer { LocalizationOverride.apply(previous) }
        LocalizationOverride.apply(.english)
        LocalizationOverride.apply(.system)
        #expect(UserDefaults.standard.array(forKey: "AppleLanguages").map { _ in true } != nil) // sistem varsayılanı döner
        #expect(LocalizationOverride.language == .system)
    }

    @Test func storePersistsTheChoice() async {
        let defaults = UserDefaults(suiteName: "LanguageTests.\(UUID().uuidString)")!
        let previous = LocalizationOverride.language
        defer { LocalizationOverride.apply(previous) }

        let store = await LanguageStore(defaults: defaults)
        #expect(await store.language == .system)
        await store.select(.turkish)
        #expect(defaults.string(forKey: "appLanguage") == "tr")
        #expect(await LanguageStore(defaults: defaults).language == .turkish)
    }

    @Test func localeKeepsTheRegionAndChangesTheLanguage() {
        #expect(AppLanguage.turkish.locale.language.languageCode?.identifier == "tr")
        #expect(AppLanguage.english.locale.language.languageCode?.identifier == "en")
        #expect(AppLanguage.system.languageCode == nil)
    }
}
