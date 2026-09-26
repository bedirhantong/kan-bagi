import UIKit
import Combine
import Supabase

/// Uygulamanın kompozisyon kökü: bağımlılıkları bir kez oluşturur, ViewModel'lere enjekte eder.
@MainActor
final class AppContainer {
    struct UseCases {
        let observeAuth: ObserveAuthStateUseCase
        let signInWithApple: SignInWithAppleUseCase
        let signInWithGoogle: SignInWithGoogleUseCase
        let signInWithEmail: SignInWithEmailUseCase
        let signInAsGuest: SignInAsGuestUseCase
        let signOut: SignOutUseCase
        let deleteAccount: DeleteAccountUseCase
        let getProfile: GetProfileUseCase
        let updateProfile: UpdateProfileUseCase
        let completeProfile: CompleteProfileUseCase
        let getRequests: GetBloodRequestsUseCase
        let getFeed: GetBloodRequestFeedUseCase
        let getMyRequests: GetMyBloodRequestsUseCase
        let getRequestDetail: GetBloodRequestDetailUseCase
        let saveRequest: SaveBloodRequestUseCase
        let setRequestActive: SetBloodRequestActiveUseCase
        let deleteRequest: DeleteBloodRequestUseCase
        let togglePledge: TogglePledgeUseCase
        let getHospitals: GetHospitalsUseCase
        let getHospitalsByIds: GetHospitalsByIdsUseCase
        let getPledgedRequests: GetMyPledgedRequestsUseCase
        let getStories: GetStoriesUseCase
        let getNearbyHospitals: GetNearbyHospitalsUseCase
        let getNearbyBloodPoints: GetNearbyBloodPointsUseCase
        let getCurrentLocation: GetCurrentLocationUseCase
        let getRooms: GetChatRoomsUseCase
        let openRoom: OpenChatRoomUseCase
        let getMessages: GetMessagesUseCase
        let sendMessage: SendMessageUseCase
        let observeMessages: ObserveMessagesUseCase
        let getHealthForm: GetHealthFormUseCase
        let submitHealthForm: SubmitHealthFormUseCase
        let getNotificationPreferences: GetNotificationPreferencesUseCase
        let updateNotificationPreferences: UpdateNotificationPreferencesUseCase
        let getNotificationAreas: GetNotificationAreasUseCase
        let setNotificationAreas: SetNotificationAreasUseCase
        let suggestNotificationArea: SuggestNotificationAreaUseCase
        let getInbox: GetNotificationInboxUseCase
        let markRead: MarkNotificationsReadUseCase
        let getUnreadCount: GetUnreadNotificationCountUseCase
        let reportContent: ReportContentUseCase
        let blockUser: BlockUserUseCase
    }

    let useCases: UseCases
    let config: AppConfig
    let push: any PushCoordinating
    let events = DataEvents()
    let networkMonitor = NetworkMonitor()

    init(config: AppConfig = .load(), launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) {
        let client = SupabaseClientProvider.make(config: config)
        let auth = SupabaseAuthRepository(client: client)
        let profiles = SupabaseProfileRepository(client: client)
        let requests = SupabaseBloodRequestRepository(client: client)
        let hospitals = SupabaseHospitalRepository(client: client)
        let bloodPoints = SupabaseBloodDonationPointRepository(client: client)
        let chat = SupabaseChatRepository(client: client)
        let forms = SupabaseHealthFormRepository(client: client)
        let notifications = SupabaseNotificationRepository(client: client)
        let safety = SupabaseSafetyRepository(client: client)
        let stories = SupabaseStoryRepository(client: client)
        let google = GoogleCredentialProvider()
        let locationProvider = CoreLocationProvider()

        self.config = config
        // OneSignal yapılandırılmışsa gerçek push, değilse devre dışı (uygulama yine çalışır).
        push = config.oneSignalAppID.map { OneSignalPushService(appID: $0, launchOptions: launchOptions) as any PushCoordinating }
            ?? NoPushService()
        useCases = UseCases(
            observeAuth: .init(auth: auth),
            signInWithApple: .init(auth: auth, profiles: profiles),
            signInWithGoogle: .init(auth: auth, profiles: profiles, googleProvider: google),
            signInWithEmail: .init(auth: auth),
            signInAsGuest: .init(auth: auth),
            signOut: .init(auth: auth),
            deleteAccount: .init(auth: auth),
            getProfile: .init(profiles: profiles),
            updateProfile: .init(profiles: profiles),
            completeProfile: .init(profiles: profiles),
            getRequests: .init(requests: requests),
            getFeed: .init(requests: requests, location: locationProvider),
            getMyRequests: .init(requests: requests),
            getRequestDetail: .init(requests: requests),
            saveRequest: .init(requests: requests),
            setRequestActive: .init(requests: requests),
            deleteRequest: .init(requests: requests),
            togglePledge: .init(requests: requests),
            getHospitals: .init(hospitals: hospitals),
            getHospitalsByIds: .init(hospitals: hospitals),
            getPledgedRequests: .init(requests: requests),
            getStories: .init(stories: stories),
            getNearbyHospitals: .init(hospitals: hospitals),
            getNearbyBloodPoints: .init(points: bloodPoints),
            getCurrentLocation: .init(location: locationProvider),
            getRooms: .init(chat: chat),
            openRoom: .init(chat: chat),
            getMessages: .init(chat: chat),
            sendMessage: .init(chat: chat),
            observeMessages: .init(chat: chat),
            getHealthForm: .init(forms: forms),
            submitHealthForm: .init(forms: forms),
            getNotificationPreferences: .init(notifications: notifications),
            updateNotificationPreferences: .init(notifications: notifications),
            getNotificationAreas: .init(notifications: notifications),
            setNotificationAreas: .init(notifications: notifications),
            suggestNotificationArea: .init(location: locationProvider, hospitals: hospitals),
            getInbox: .init(notifications: notifications),
            markRead: .init(notifications: notifications),
            getUnreadCount: .init(notifications: notifications),
            reportContent: .init(safety: safety),
            blockUser: .init(safety: safety)
        )
    }

    func makeSessionStore() -> SessionStore {
        SessionStore(
            observeAuth: useCases.observeAuth, getProfile: useCases.getProfile,
            getUnreadCount: useCases.getUnreadCount, signOut: useCases.signOut,
            push: push, events: events
        )
    }
}

/// Ekranlar arası veri değişikliği olayları (ör. ilan oluşturuldu -> liste yenilensin).
final class DataEvents: @unchecked Sendable {
    let requestsChanged = PassthroughSubject<Void, Never>()
    let notificationsChanged = PassthroughSubject<Void, Never>()
}
