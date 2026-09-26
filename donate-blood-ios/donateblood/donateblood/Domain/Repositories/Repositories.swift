import Foundation

/// Domain katmanının dış dünyayla sözleşmeleri. Implementasyonlar Data katmanındadır.

protocol AuthRepository: Sendable {
    var currentUser: AuthUser? { get }
    /// İlk değer mevcut oturumdur; sonrasında her değişiklikte yeni değer yayınlanır.
    func authStateStream() -> AsyncStream<AuthState>
    /// Misafir ise kimliği mevcut hesaba bağlar (veri korunur), değilse yeni oturum açar.
    func signIn(with credential: IdentityCredential) async throws
    func signInAsGuest() async throws
    /// Test/geliştirme hesapları için e-posta + şifre girişi (Apple/Google hesabı gerektirmez).
    func signIn(email: String, password: String) async throws
    func signOut() async throws
    func deleteAccount() async throws
}

/// Apple/Google gibi platform kimlik sağlayıcılarından kimlik bilgisi alır.
protocol IdentityCredentialProvider: Sendable {
    func credential() async throws -> IdentityCredential
}

protocol ProfileRepository: Sendable {
    func myProfile() async throws -> Profile
    func updateMyProfile(_ update: ProfileUpdate, markCompleted: Bool) async throws -> Profile
    /// Ad/soyad boşsa (ör. Apple'dan gelen isimle) doldurur; doluysa dokunmaz.
    func prefillNameIfEmpty(_ name: PersonName) async throws
}

protocol BloodRequestRepository: Sendable {
    /// Ana akış: tüm kan grupları, kullanıcıya en yakın hastaneden en uzağa; aynı yerde en yeni önce.
    /// `location` yoksa yalnızca en yeniden eskiye.
    func feed(near location: Coordinate?, page: PageRequest) async throws -> [BloodRequest]
    func requests(filter: BloodRequestFilter, page: PageRequest) async throws -> [BloodRequest]
    func myRequests() async throws -> [BloodRequest]
    func request(id: UUID) async throws -> BloodRequest
    func contactNumbers(requestId: UUID) async throws -> [String]
    func create(_ draft: BloodRequestDraft) async throws -> UUID
    func update(id: UUID, draft: BloodRequestDraft) async throws
    func setActive(id: UUID, isActive: Bool) async throws
    func delete(id: UUID) async throws
    func pledgeCount(requestId: UUID) async throws -> Int
    func hasPledged(requestId: UUID) async throws -> Bool
    func setPledge(requestId: UUID, pledged: Bool) async throws
    /// Kullanıcının bağış niyeti bildirdiği ilanlar (yeniden eskiye).
    func pledgedRequests() async throws -> [BloodRequest]
}

/// Konum izninin durumu (platformdan bağımsız).
enum LocationAuthorization: Sendable, Equatable {
    case notDetermined, authorized, denied
}

/// Kullanıcının o anki konumunu verir (izin yoksa/alınamazsa `nil`). Platform detayı Data katmanındadır.
protocol LocationProvider: Sendable {
    /// Gerekirse izin ister; izin yoksa veya zaman aşımında `nil`.
    func currentCoordinate() async -> Coordinate?
    func authorization() async -> LocationAuthorization
}

protocol BloodDonationPointRepository: Sendable {
    func nearby(latitude: Double, longitude: Double, radiusKm: Double) async throws -> [BloodDonationPoint]
}

protocol HospitalRepository: Sendable {
    func hospitals(search: String?) async throws -> [Hospital]
    func hospitals(ids: [Int]) async throws -> [Hospital]
    func nearby(latitude: Double, longitude: Double, radiusKm: Double) async throws -> [Hospital]
}

protocol ChatRepository: Sendable {
    func rooms() async throws -> [ChatRoom]
    func room(with userId: UUID) async throws -> UUID
    /// En yeni önce; `before` verilirse ondan eski mesajlar.
    func messages(roomId: UUID, before: Date?, limit: Int) async throws -> [ChatMessage]
    func send(roomId: UUID, content: String) async throws
    func incomingMessages(roomId: UUID) -> AsyncStream<ChatMessage>
}

protocol HealthFormRepository: Sendable {
    func myAnswers() async throws -> HealthAnswers?
    func save(_ answers: HealthAnswers) async throws
}

protocol NotificationRepository: Sendable {
    func preferences() async throws -> NotificationPreferences
    func updatePreferences(_ preferences: NotificationPreferences) async throws
    /// Bildirim için seçilebilir il ve ilçeler (hastanesi olanlar).
    func areas() async throws -> [NotificationArea]
    func inbox(limit: Int) async throws -> [AppNotification]
    func markRead(id: UUID) async throws
    func markAllRead() async throws
    func unreadCount() async throws -> Int
}

protocol StoryRepository: Sendable {
    func stories() async throws -> [Story]
}

protocol SafetyRepository: Sendable {
    func report(target: ReportTarget, targetId: String, reason: String) async throws
    func block(userId: UUID) async throws
    func unblock(userId: UUID) async throws
}
