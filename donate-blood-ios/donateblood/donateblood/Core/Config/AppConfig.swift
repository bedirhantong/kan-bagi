import Foundation

/// Build yapılandırmasından (xcconfig -> Info.plist) okunan değerler.
struct AppConfig: Sendable {
    let supabaseURL: URL
    let supabaseAnonKey: String
    /// E-posta/şifre ile test girişi (Debug'da açık, Release'de kapalı; xcconfig ile değiştirilebilir).
    var emailLoginEnabled = false
    /// OneSignal uygulama kimliği. `nil` ise push bildirimleri devre dışıdır (uygulama yine çalışır).
    var oneSignalAppID: String?

    /// Secrets.xcconfig henüz doldurulmadıysa `true`.
    var isPlaceholder: Bool { supabaseURL.host?.contains("YOUR-PROJECT-REF") == true }

    /// Saf ayrıştırma (test edilebilir): boş, genişletilmemiş (`$(...)`) veya yer tutucu (`YOUR_...`) değerler `nil` döner.
    static func oneSignalAppID(from lookup: (String) -> String?) -> String? {
        guard let raw = lookup("ONESIGNAL_APP_ID")?.trimmingCharacters(in: .whitespaces),
              !raw.isEmpty, !raw.hasPrefix("$("), !raw.uppercased().hasPrefix("YOUR_") else { return nil }
        return raw
    }

    static func load(from bundle: Bundle = .main) -> AppConfig {
        guard
            let urlString = bundle.object(forInfoDictionaryKey: "SUPABASE_URL") as? String,
            let url = URL(string: urlString), url.host != nil,
            let key = bundle.object(forInfoDictionaryKey: "SUPABASE_ANON_KEY") as? String, !key.isEmpty
        else {
            // Eksik/bozuk yapılandırmada çökmek yerine uygulama "yapılandırma eksik" ekranı gösterir.
            Log.data.fault("SUPABASE_URL / SUPABASE_ANON_KEY eksik veya geçersiz.")
            return AppConfig(supabaseURL: URL(string: "https://YOUR-PROJECT-REF.supabase.co")!, supabaseAnonKey: "")
        }
        let emailLogin = (bundle.object(forInfoDictionaryKey: "EMAIL_LOGIN_ENABLED") as? String)?.uppercased() == "YES"
        return AppConfig(supabaseURL: url, supabaseAnonKey: key, emailLoginEnabled: emailLogin, oneSignalAppID: Self.oneSignalAppID(from: { bundle.object(forInfoDictionaryKey: $0) as? String }))
    }
}
