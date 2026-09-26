import SwiftUI

/// Ana gezinme (Instagram/X düzeni): Ana Sayfa · Harita · İlan ver (ortada) · Mesajlar · Profil.
/// Ortadaki sekme bir ekran değildir: seçilince seçim değişmez, yeni ilan formu açılır.
/// Bildirimler ana sayfanın üst çubuğundadır (zil). Alt çubuk yalnızca kök ekranlarda görünür.
struct MainTabView: View {
    let container: AppContainer
    @EnvironmentObject private var session: SessionStore
    @Environment(\.displayScale) private var displayScale
    @Environment(\.colorScheme) private var colorScheme

    enum Tab: Hashable { case home, map, create, messages, profile }

    @State private var selection: Tab = .home
    @State private var homePath: [Route] = []
    @State private var messagesPath: [Route] = []
    @State private var profilePath: [Route] = []
    /// Açık olan yeni ilan formu (kısayoldan geldiyse önceden doldurulmuş).
    @State private var createRequest: RequestPrefill?

    /// "İlan ver" sekmesi seçimi değiştirmez; formu açar (sekme görünümü titremez, önceki sekme korunur).
    private var tabSelection: Binding<Tab> {
        Binding(
            get: { selection },
            set: { tab in
                if tab == .create { startCreate(.empty) } else { selection = tab }
            })
    }

    var body: some View {
        TabView(selection: tabSelection) {
            NavigationStack(path: $homePath) {
                HomeView(container: container, onCreate: startCreate).routeDestinations(container: container)
            }
            .tabItem { Label("tab.home", systemImage: "house") }
            .tag(Tab.home)

            NavigationStack {
                HospitalMapView(container: container).routeDestinations(container: container)
            }
            .tabItem { Label("tab.map", systemImage: "map") }
            .tag(Tab.map)

            // İçerik hiç gösterilmez (seçilemez); yalnızca sekme düğmesi.
            Color.clear
                .tabItem {
                    Label { Text("tab.create") } icon: { CreateTabIcon.image(scale: displayScale, colorScheme: colorScheme) }
                        .accessibilityLabel(Text("bloodRequest.create"))
                }
                .tag(Tab.create)

            NavigationStack(path: $messagesPath) {
                ChatListView(container: container).routeDestinations(container: container)
            }
            .tabItem { Label("tab.messages", systemImage: "bubble.left.and.bubble.right") }
            .tag(Tab.messages)

            NavigationStack(path: $profilePath) {
                ProfileView(container: container).routeDestinations(container: container)
            }
            .tabItem { Label("tab.profile", systemImage: "person") }
            .tag(Tab.profile)
        }
        .tint(Theme.brand)
        .offlineBanner()
        .sheet(item: $createRequest) { prefill in
            NavigationStack { RequestFormView(viewModel: container.makeRequestFormViewModel(editing: nil, prefill: prefill)) }
        }
        .onChange(of: session.pendingLink) { link in handle(link) }
        .task {
            handle(session.pendingLink)
            #if DEBUG
            if let tab = DebugLaunch.tab { selection = tab }
            if let route = DebugLaunch.route { selection = .home; homePath = [route] }
            #endif
        }
    }

    /// Yeni ilan: misafirde önce giriş istenir.
    private func startCreate(_ prefill: RequestPrefill) {
        Haptics.impact()
        session.requireAccount { createRequest = prefill }
    }

    /// Bildirim/derin bağlantı: ilgili sekmeye geçip hedef ekranı açar.
    private func handle(_ link: DeepLink?) {
        guard let link else { return }
        switch link {
        case .request(let id):
            selection = .home
            homePath = [.request(id)]
        case .room(let id):
            selection = .messages
            messagesPath = [.chat(.room(id: id, userId: nil, name: L10n.string("chat.title")))]
        }
        session.pendingLink = nil
    }
}

extension View {
    /// Tüm `Route` hedeflerini tek yerde çözer. İtilen her ekranda alt sekme çubuğu gizlenir.
    func routeDestinations(container: AppContainer) -> some View {
        navigationDestination(for: Route.self) { route in
            Group {
                switch route {
                case .request(let id):
                    RequestDetailView(viewModel: container.makeRequestDetailViewModel(requestId: id), container: container)
                case .editRequest(let request):
                    RequestFormView(viewModel: container.makeRequestFormViewModel(editing: request))
                case .hospital(let hospital):
                    HospitalDetailView(hospital: hospital)
                case .chat(let target):
                    ChatDetailView(viewModel: container.makeChatDetailViewModel(target: target))
                case .healthForm:
                    HealthFormView(viewModel: container.makeHealthFormViewModel())
                case .settings:
                    SettingsView(viewModel: container.makeSettingsViewModel(), container: container)
                case .notificationSettings:
                    NotificationSettingsView(viewModel: container.makeNotificationSettingsViewModel())
                case .notifications:
                    NotificationInboxView(viewModel: container.makeNotificationInboxViewModel())
                case .languageSettings:
                    LanguageSettingsView()
                case .faq:
                    FAQView()
                case .about:
                    AboutView()
                }
            }
            .toolbar(.hidden, for: .tabBar)
        }
    }
}
