import Foundation
import Combine
@testable import donateblood

// MARK: - Auth

final class FakeAuthRepository: AuthRepository, @unchecked Sendable {
    var currentUser: AuthUser?
    var signInError: Error?
    private(set) var guestSignIns = 0
    private(set) var credentials: [IdentityCredential] = []
    private(set) var signOuts = 0
    private var continuation: AsyncStream<AuthState>.Continuation?

    func authStateStream() -> AsyncStream<AuthState> {
        AsyncStream { self.continuation = $0 }
    }
    /// Test içinden yeni bir kimlik durumu yayınlar.
    func emit(_ state: AuthState) { continuation?.yield(state) }

    func signIn(with credential: IdentityCredential) async throws {
        if let signInError { throw signInError }
        credentials.append(credential)
    }
    func signInAsGuest() async throws {
        guestSignIns += 1
        if let signInError { throw signInError }
    }
    private(set) var emailSignIns: [(String, String)] = []
    func signIn(email: String, password: String) async throws {
        if let signInError { throw signInError }
        emailSignIns.append((email, password))
    }
    func signOut() async throws { signOuts += 1 }
    func deleteAccount() async throws {}
}

final class FakeProfileRepository: ProfileRepository, @unchecked Sendable {
    var profile: Profile?
    var error: Error?
    private(set) var lastMarkCompleted: Bool?
    private(set) var prefilledNames: [PersonName] = []

    func myProfile() async throws -> Profile {
        if let error { throw error }
        guard let profile else { throw AppError.notFound }
        return profile
    }
    func updateMyProfile(_ update: ProfileUpdate, markCompleted: Bool) async throws -> Profile {
        lastMarkCompleted = markCompleted
        return Profile(id: UUID(), name: update.name, surname: update.surname, email: nil, phoneNumber: update.phoneNumber,
                       birthDate: update.birthDate, bloodType: update.bloodType, gender: update.gender,
                       userType: .regularUser, isProfileCompleted: markCompleted, createdAt: nil)
    }
    func prefillNameIfEmpty(_ name: PersonName) async throws { prefilledNames.append(name) }
}

// MARK: - Blood requests

final class FakeBloodRequestRepository: BloodRequestRepository, @unchecked Sendable {
    var listHandler: (BloodRequestFilter, PageRequest) throws -> [BloodRequest] = { _, _ in [] }
    var feedHandler: (Coordinate?, PageRequest) throws -> [BloodRequest] = { _, _ in [] }
    private(set) var feedCalls: [(Coordinate?, PageRequest)] = []
    var detailResult: Result<BloodRequest, Error> = .failure(AppError.notFound)
    var contacts: [String] = []
    var pledgeCountValue = 0
    var hasPledgedValue = false
    var mutationError: Error?

    private(set) var created: [BloodRequestDraft] = []
    private(set) var updated: [(UUID, BloodRequestDraft)] = []
    private(set) var deleted: [UUID] = []
    private(set) var activeChanges: [(UUID, Bool)] = []
    private(set) var pledgeChanges: [(UUID, Bool)] = []
    private(set) var listCalls: [(BloodRequestFilter, PageRequest)] = []
    private(set) var contactRequests = 0
    let newId = UUID()

    func feed(near location: Coordinate?, page: PageRequest) async throws -> [BloodRequest] {
        feedCalls.append((location, page))
        return try feedHandler(location, page)
    }
    func requests(filter: BloodRequestFilter, page: PageRequest) async throws -> [BloodRequest] {
        listCalls.append((filter, page))
        return try listHandler(filter, page)
    }
    func myRequests() async throws -> [BloodRequest] { [] }
    func request(id: UUID) async throws -> BloodRequest { try detailResult.get() }
    func contactNumbers(requestId: UUID) async throws -> [String] { contactRequests += 1; return contacts }
    func create(_ draft: BloodRequestDraft) async throws -> UUID {
        if let mutationError { throw mutationError }
        created.append(draft); return newId
    }
    func update(id: UUID, draft: BloodRequestDraft) async throws {
        if let mutationError { throw mutationError }
        updated.append((id, draft))
    }
    func setActive(id: UUID, isActive: Bool) async throws {
        if let mutationError { throw mutationError }
        activeChanges.append((id, isActive))
    }
    func delete(id: UUID) async throws {
        if let mutationError { throw mutationError }
        deleted.append(id)
    }
    func pledgeCount(requestId: UUID) async throws -> Int { pledgeCountValue }
    func hasPledged(requestId: UUID) async throws -> Bool { hasPledgedValue }
    func setPledge(requestId: UUID, pledged: Bool) async throws {
        if let mutationError { throw mutationError }
        pledgeChanges.append((requestId, pledged))
    }
    func pledgedRequests() async throws -> [BloodRequest] { [] }
}

final class FakeStoryRepository: StoryRepository, @unchecked Sendable {
    var result: [Story] = []
    func stories() async throws -> [Story] { result }
}

final class FakeHospitalRepository: HospitalRepository, @unchecked Sendable {
    var all: [Hospital] = []
    func hospitals(search: String?) async throws -> [Hospital] { all }
    func hospitals(ids: [Int]) async throws -> [Hospital] { all.filter { ids.contains($0.id) } }
    var nearbyError: Error?
    /// Belirli bir enlem için gecikme ve sonuç (bayat yanıt testleri için).
    var delayByLatitude: [Double: Duration] = [:]
    var resultByLatitude: [Double: [Hospital]] = [:]
    private(set) var nearbyCalls: [(latitude: Double, radiusKm: Double)] = []
    func nearby(latitude: Double, longitude: Double, radiusKm: Double) async throws -> [Hospital] {
        nearbyCalls.append((latitude, radiusKm))
        if let delay = delayByLatitude[latitude] { try? await Task.sleep(for: delay) }
        if let nearbyError { throw nearbyError }
        return resultByLatitude[latitude] ?? all
    }
}

// MARK: - Chat

final class FakeChatRepository: ChatRepository, @unchecked Sendable {
    var roomId = UUID()
    var history: [ChatMessage] = []
    var sendError: Error?
    private(set) var openedWith: [UUID] = []
    private(set) var sent: [String] = []
    private var continuation: AsyncStream<ChatMessage>.Continuation?

    func rooms() async throws -> [ChatRoom] { [] }
    func room(with userId: UUID) async throws -> UUID { openedWith.append(userId); return roomId }
    func messages(roomId: UUID, before: Date?, limit: Int) async throws -> [ChatMessage] {
        Array(history.sorted { $0.createdAt > $1.createdAt }.prefix(limit)) // repo en yeni önce döner
    }
    func send(roomId: UUID, content: String) async throws {
        if let sendError { throw sendError }
        sent.append(content)
    }
    func incomingMessages(roomId: UUID) -> AsyncStream<ChatMessage> {
        AsyncStream { self.continuation = $0 }
    }
    func push(_ message: ChatMessage) { continuation?.yield(message) }
}

// MARK: - Notifications, safety, health

final class FakeNotificationRepository: NotificationRepository, @unchecked Sendable {
    var stored = NotificationPreferences.default
    var updateError: Error?
    var unread = 0
    var areaList: [NotificationArea] = TestData.areas
    var areasError: Error?
    private(set) var updateCount = 0

    func areas() async throws -> [NotificationArea] {
        if let areasError { throw areasError }
        return areaList
    }

    func preferences() async throws -> NotificationPreferences { stored }
    func updatePreferences(_ preferences: NotificationPreferences) async throws {
        updateCount += 1
        if let updateError { throw updateError }
        stored = preferences
    }
    func inbox(limit: Int) async throws -> [AppNotification] { [] }
    func markRead(id: UUID) async throws {}
    func markAllRead() async throws {}
    func unreadCount() async throws -> Int { unread }
}

final class FakeSafetyRepository: SafetyRepository, @unchecked Sendable {
    private(set) var reports: [(ReportTarget, String, String)] = []
    private(set) var blocked: [UUID] = []
    func report(target: ReportTarget, targetId: String, reason: String) async throws { reports.append((target, targetId, reason)) }
    func block(userId: UUID) async throws { blocked.append(userId) }
    func unblock(userId: UUID) async throws {}
}

final class FakeHealthFormRepository: HealthFormRepository, @unchecked Sendable {
    var saved: HealthAnswers?
    func myAnswers() async throws -> HealthAnswers? { saved }
    func save(_ answers: HealthAnswers) async throws { saved = answers }
}

// MARK: - Platform

@MainActor
final class FakePush: PushCoordinating {
    var onOpen: ((DeepLink) -> Void)?
    private(set) var permissionRequests = 0
    private(set) var identified: [UUID] = []
    private(set) var clearCount = 0
    private(set) var badge: Int?
    func requestAuthorization() async { permissionRequests += 1 }
    func identify(userId: UUID) { identified.append(userId) }
    func clearIdentity() { clearCount += 1 }
    func setBadge(_ count: Int) { badge = count }
}

struct FakeCredentialProvider: IdentityCredentialProvider {
    var result: Result<IdentityCredential, Error>
    func credential() async throws -> IdentityCredential { try result.get() }
}

final class FakeLocationProvider: LocationProvider, @unchecked Sendable {
    var coordinate: Coordinate?
    var authorizationState: LocationAuthorization
    private(set) var requests = 0
    init(_ coordinate: Coordinate? = nil, authorization: LocationAuthorization? = nil) {
        self.coordinate = coordinate
        authorizationState = authorization ?? (coordinate == nil ? .denied : .authorized)
    }
    func currentCoordinate() async -> Coordinate? { requests += 1; return coordinate }
    func authorization() async -> LocationAuthorization { authorizationState }
}

final class FakeBloodDonationPointRepository: BloodDonationPointRepository, @unchecked Sendable {
    var points: [BloodDonationPoint] = []
    var error: Error?
    func nearby(latitude: Double, longitude: Double, radiusKm: Double) async throws -> [BloodDonationPoint] {
        if let error { throw error }
        return points
    }
}
