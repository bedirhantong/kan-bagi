import Foundation
import Combine
import SwiftUI

enum DeepLink: Equatable, Sendable {
    case request(UUID)
    case room(UUID)

    init?(userInfo: [AnyHashable: Any]) {
        if let value = userInfo["request_id"] as? String, let id = UUID(uuidString: value) { self = .request(id); return }
        if let value = userInfo["room_id"] as? String, let id = UUID(uuidString: value) { self = .room(id); return }
        return nil
    }
}

/// Oturum, profil ve "hesap gerekli" kapısı için tek doğruluk kaynağı.
@MainActor
final class SessionStore: ObservableObject {
    enum Phase: Equatable { case loading, signedOut, signedIn }

    @Published private(set) var phase: Phase = .loading
    @Published private(set) var user: AuthUser?
    @Published private(set) var profile: Profile?
    @Published private(set) var profileLoadFailed = false
    @Published private(set) var unreadNotifications = 0
    @Published var isAuthSheetPresented = false
    @Published var pendingLink: DeepLink?

    var isGuest: Bool { user?.isGuest ?? true }

    private let observeAuth: ObserveAuthStateUseCase
    private let getProfile: GetProfileUseCase
    private let getUnreadCount: GetUnreadNotificationCountUseCase
    private let signOutUseCase: SignOutUseCase
    private let push: any PushCoordinating
    private var authTask: Task<Void, Never>?
    private var cancellables = Set<AnyCancellable>()

    init(
        observeAuth: ObserveAuthStateUseCase, getProfile: GetProfileUseCase,
        getUnreadCount: GetUnreadNotificationCountUseCase,
        signOut: SignOutUseCase,
        push: any PushCoordinating, events: DataEvents
    ) {
        self.observeAuth = observeAuth
        self.getProfile = getProfile
        self.getUnreadCount = getUnreadCount
        self.signOutUseCase = signOut
        self.push = push

        events.notificationsChanged
            .sink { [weak self] in Task { await self?.refreshUnreadCount() } }
            .store(in: &cancellables)
    }

    func start() {
        guard authTask == nil else { return }
        push.onOpen = { [weak self] link in self?.pendingLink = link }

        let stream = observeAuth()
        authTask = Task { [weak self] in
            for await state in stream {
                await self?.handle(state)
            }
        }
    }

    func reloadProfile() async {
        guard let user, !user.isGuest else { return }
        profileLoadFailed = false
        do {
            profile = try await getProfile()
        } catch {
            Log.auth.error("Profil yüklenemedi: \(String(describing: error))")
            profileLoadFailed = true
        }
    }

    func signOut() async throws { try await signOutUseCase() }

    func profileDidChange(_ profile: Profile) { self.profile = profile }

    /// Misafir kullanıcı kısıtlı bir aksiyon denediğinde giriş sayfasını açar.
    /// Kayıtlı kullanıcıysa aksiyonu çalıştırır.
    func requireAccount(_ action: () -> Void) {
        if isGuest { isAuthSheetPresented = true } else { action() }
    }

    func refreshUnreadCount() async {
        guard let user, !user.isGuest else { unreadNotifications = 0; push.setBadge(0); return }
        let count = (try? await getUnreadCount()) ?? 0
        unreadNotifications = count
        push.setBadge(count)
    }

    // MARK: Private

    private func handle(_ state: AuthState) async {
        switch state {
        case .signedOut:
            user = nil
            profile = nil
            profileLoadFailed = false
            unreadNotifications = 0
            isAuthSheetPresented = false
            push.clearIdentity()
            phase = .signedOut
        case .signedIn(let authUser):
            let wasGuest = user?.isGuest ?? true
            user = authUser
            phase = .signedIn
            if authUser.isGuest {
                profile = nil
                push.clearIdentity() // misafirler bildirim almaz
                return
            }
            isAuthSheetPresented = false
            push.identify(userId: authUser.id) // kimlik hemen bağlanır; izin kayıt akışında ya da ayarlarda istenir
            if profile == nil || wasGuest { await reloadProfile() }
            await refreshUnreadCount()
        }
    }

    /// Kayıt akışı bitince çağrılır. Bildirim izni burada istenmez: kayıt akışının son adımında,
    /// kullanıcıya neden gerektiği açıklandıktan sonra ve yalnızca kabul ederse istenir.
    func profileCompleted(_ profile: Profile) async {
        self.profile = profile
    }
}
