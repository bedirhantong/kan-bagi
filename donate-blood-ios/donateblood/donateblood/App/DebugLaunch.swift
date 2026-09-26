#if DEBUG
import Foundation

/// Yalnızca Debug derlemesinde: simülatörde ekranları hızlı açmak/görüntü almak için başlatma parametreleri.
/// Kullanım: `xcrun simctl launch <cihaz> <bundle-id> -debugSignIn donor -debugTab messages`
/// (`-anahtar değer` çiftleri UserDefaults üzerinden okunur.) Release derlemesine dahil edilmez.
enum DebugLaunch {
    /// donor | donor2 | requester
    static var signIn: TestAccount? {
        guard let name = UserDefaults.standard.string(forKey: "debugSignIn") else { return nil }
        switch name {
        case "donor": return TestAccount.all.first { $0.role == .donor }
        case "donor2": return TestAccount.all.first { $0.role == .secondDonor }
        case "requester": return TestAccount.all.first { $0.role == .requester }
        default: return nil
        }
    }

    /// home | map | messages | profile  (bildirimler için: `-debugRoute notifications`)
    static var tab: MainTabView.Tab? {
        switch UserDefaults.standard.string(forKey: "debugTab") {
        case "home": return .home
        case "map": return .map
        case "messages": return .messages
        case "profile": return .profile
        default: return nil
        }
    }

    /// Harita açılınca ilk yeri seçer: hospital | point
    static var mapSelection: String? { UserDefaults.standard.string(forKey: "debugMapSelect") }

    /// Kayıt ekranını boş bir profille, verilen adımdan açar: personal | donor | areas | notifications.
    /// Oturum açıksa kaydet düğmeleri o hesaba yazar; yalnızca tasarım denemesi içindir.
    static var registrationPreview: RegistrationViewModel.Step? {
        switch UserDefaults.standard.string(forKey: "debugRegistration") {
        case "personal": return .personal
        case "donor": return .donor
        case "areas": return .areas
        case "notifications": return .notifications
        default: return nil
        }
    }

    /// healthForm | settings | notificationSettings | faq | about | request:<uuid>
    static var route: Route? {
        guard let value = UserDefaults.standard.string(forKey: "debugRoute") else { return nil }
        switch value {
        case "healthForm": return .healthForm
        case "settings": return .settings
        case "notificationSettings": return .notificationSettings
        case "notifications": return .notifications
        case "languageSettings": return .languageSettings
        case "faq": return .faq
        case "about": return .about
        default:
            if value.hasPrefix("chat:"), let id = UUID(uuidString: String(value.dropFirst("chat:".count))) {
                return .chat(.room(id: id, userId: nil, name: "Chat"))
            }
            if value.hasPrefix("request:"), let id = UUID(uuidString: String(value.dropFirst("request:".count))) { return .request(id) }
            return nil
        }
    }
}
#endif
