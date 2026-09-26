import Foundation

/// Proje ve yasal bağlantılar. Adresler xcconfig'den (`SOURCE_CODE_URL`, `TERMS_URL`, `PRIVACY_URL`) okunur;
/// kendi kopyanızı yayınlıyorsanız bunları kendi sayfalarınızla değiştirin.
enum AppLinks {
    /// Geçici varsayılan: gizlilik politikası ve kullanım koşulları yayınlanana kadar proje deposu.
    static let projectHome = URL(string: "https://github.com/bedirhantong/kan-bagi")!

    /// Projenin açık kaynak deposu (Hakkında ekranında gösterilir). `SOURCE_CODE_URL` xcconfig değerinden okunur;
    /// boşsa bağlantı gösterilmez. Kendi kopyanızı yayınlıyorsanız kendi adresinizi yazın ve ana projeyi ayrıca anın.
    static var sourceCode: URL? { infoURL("SOURCE_CODE_URL") }

    /// Boş, çözülmemiş (`$(...)`) veya https olmayan değerleri reddeder.
    static func parse(_ raw: String?) -> URL? {
        guard let value = raw?.trimmingCharacters(in: .whitespacesAndNewlines),
              !value.isEmpty, !value.hasPrefix("$("),
              let url = URL(string: value), url.scheme == "https", url.host != nil else { return nil }
        return url
    }

    static var terms: URL { infoURL("TERMS_URL") ?? projectHome }
    static var privacy: URL { infoURL("PRIVACY_URL") ?? projectHome }

    private static func infoURL(_ key: String) -> URL? {
        parse(Bundle.main.object(forInfoDictionaryKey: key) as? String)
    }
}
