import UIKit

/// OneSignal, APNs kaydını ve jeton yönetimini kendisi yapar; burada özel bir işlem gerekmez.
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        Log.push.error("APNs kaydı başarısız: \(String(describing: error))")
    }
}
