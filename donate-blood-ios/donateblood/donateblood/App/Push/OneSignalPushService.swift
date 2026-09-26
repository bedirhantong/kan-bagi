import UIKit
import UserNotifications
import OneSignalFramework

/// OneSignal implementasyonu. SDK'ya bağımlılık yalnızca bu dosyadadır.
///
/// - `OneSignal.login(externalId)`: kullanıcıyı Supabase kullanıcı id'siyle eşler; sunucu `external_id` ile hedefler.
/// - Bildirime dokunma: `additionalData` içindeki `request_id` / `room_id` derin bağlantıya çevrilir.
@MainActor
final class OneSignalPushService: NSObject, PushCoordinating, OSNotificationClickListener {
    /// Bağlanmadan önce gelen dokunuşlar (soğuk açılış) saklanır ve bağlanınca iletilir.
    var onOpen: ((DeepLink) -> Void)? {
        didSet {
            guard let onOpen, let link = pendingLink else { return }
            pendingLink = nil
            onOpen(link)
        }
    }
    private var pendingLink: DeepLink?
    private var identifiedUserId: UUID?

    init(appID: String, launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) {
        super.init()
        #if DEBUG
        OneSignal.Debug.setLogLevel(.LL_WARN)
        #endif
        OneSignal.initialize(appID, withLaunchOptions: launchOptions)
        OneSignal.Notifications.addClickListener(self)
    }

    func requestAuthorization() async {
        #if DEBUG
        // Simülatörde ekran görüntüsü/test sırasında sistem izin penceresini atlamak için: `-debugNoPush YES`.
        if UserDefaults.standard.bool(forKey: "debugNoPush") { return }
        #endif
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            OneSignal.Notifications.requestPermission({ _ in continuation.resume() }, fallbackToSettings: false)
        }
    }

    func identify(userId: UUID) {
        guard identifiedUserId != userId else { return }
        identifiedUserId = userId
        OneSignal.login(userId.uuidString.lowercased())
    }

    func clearIdentity() {
        guard identifiedUserId != nil else { return }
        identifiedUserId = nil
        OneSignal.logout()
    }

    func setBadge(_ count: Int) {
        UNUserNotificationCenter.current().setBadgeCount(count)
    }

    // MARK: OSNotificationClickListener

    nonisolated func onClick(event: OSNotificationClickEvent) {
        guard let data = event.notification.additionalData,
              let link = DeepLink(userInfo: data) else { return }
        Task { @MainActor in
            if let onOpen = self.onOpen { onOpen(link) } else { self.pendingLink = link }
        }
    }
}
