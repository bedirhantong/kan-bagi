import Foundation

/// Bildirim izni isteme yeteneği: ViewModel'ler somut sınıfa değil buna bağımlıdır.
@MainActor
protocol PushPermissionRequesting: AnyObject {
    /// Sistem bildirim iznini ister (daha önce sorulmadıysa). Push yapılandırılmamışsa hiçbir şey yapmaz.
    func requestAuthorization() async
}

/// `SessionStore`'un ihtiyaç duyduğu push koordinasyonu (test edilebilirlik için soyutlanmıştır).
///
/// Cihaz jetonlarını uygulama tutmaz: kullanıcı, push sağlayıcısına (OneSignal) **Supabase kullanıcı id'siyle**
/// bağlanır ve sunucu bu id'ye bildirim gönderir.
@MainActor
protocol PushCoordinating: PushPermissionRequesting {
    /// Kullanıcı bir bildirime dokunduğunda çağrılır.
    var onOpen: ((DeepLink) -> Void)? { get set }
    /// Giriş yapmış (misafir olmayan) kullanıcıyı push sağlayıcısına bağlar.
    func identify(userId: UUID)
    /// Çıkışta/misafirlikte bağlantıyı keser: bu cihaz artık önceki kullanıcının bildirimlerini almaz.
    func clearIdentity()
    /// Uygulama simgesi rozeti.
    func setBadge(_ count: Int)
}

/// OneSignal yapılandırılmadığında kullanılır: push devre dışı, uygulama normal çalışır.
@MainActor
final class NoPushService: PushCoordinating {
    var onOpen: ((DeepLink) -> Void)?
    func requestAuthorization() async {}
    func identify(userId: UUID) {}
    func clearIdentity() {}
    func setBadge(_ count: Int) {}
}
