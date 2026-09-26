import Foundation

/// "Devam ederek Kullanım Koşulları ve Gizlilik Politikası'nı kabul edersin." — iki bağlantılı metin.
/// Bağlantı adresleri tek yerde (`AppLinks`) durur; çeviri metni yalnızca kelimeleri içerir.
enum LegalText {
    static func attributed(terms: URL = AppLinks.terms, privacy: URL = AppLinks.privacy) -> AttributedString {
        let termsTitle = L10n.string("settings.terms")
        let privacyTitle = L10n.string("settings.privacy")
        var text = AttributedString(L10n.format("login.legal %@ %@", termsTitle, privacyTitle))
        if let range = text.range(of: termsTitle) {
            text[range].link = terms
            text[range].underlineStyle = .single
        }
        if let range = text.range(of: privacyTitle) {
            text[range].link = privacy
            text[range].underlineStyle = .single
        }
        return text
    }
}
