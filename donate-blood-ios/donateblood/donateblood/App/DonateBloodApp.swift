import SwiftUI
import GoogleSignIn

@main
struct DonateBloodApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @StateObject private var session: SessionStore
    @StateObject private var languageStore = LanguageStore()
    private let container: AppContainer
    private let isConfigured: Bool
    @AppStorage("appearance") private var appearance = AppearanceSetting.system.rawValue

    init() {
        let config = AppConfig.load()
        let container = AppContainer(config: config)
        self.container = container
        isConfigured = !config.isPlaceholder
        _session = StateObject(wrappedValue: container.makeSessionStore())
    }

    var body: some Scene {
        WindowGroup {
            if isConfigured {
                RootView(container: container)
                    // Dil değişince kök yeniden kurulur: tüm metinler ve biçimler yeni dille üretilir.
                    .id(languageStore.language)
                    .environment(\.locale, languageStore.locale)
                    .environmentObject(languageStore)
                    .environmentObject(session)
                    .environmentObject(container.networkMonitor)
                    .preferredColorScheme(AppearanceSetting(rawValue: appearance)?.colorScheme)
                    .task {
                        session.start()
                        #if DEBUG
                        // Simülatörde hızlı deneme: `-debugSignIn donor` ile test hesabıyla otomatik giriş.
                        if let account = DebugLaunch.signIn, container.config.emailLoginEnabled {
                            try? await container.useCases.signInWithEmail(email: account.email, password: account.password)
                        }
                        #endif
                    }
                    .onOpenURL { url in GIDSignIn.sharedInstance.handle(url) }
            } else {
                MissingConfigurationView()
            }
        }
    }
}

/// Secrets.xcconfig doldurulmadıysa "internet yok" yerine gerçek sebebi gösterir.
struct MissingConfigurationView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "gearshape.2.fill").font(.system(size: 56)).foregroundStyle(.orange)
            Text("config.missing.title").font(.title2.bold())
            Text("config.missing.message")
                .multilineTextAlignment(.center).foregroundStyle(.secondary)
        }
        .padding(32)
    }
}

enum AppearanceSetting: String, CaseIterable, Identifiable {
    case system, light, dark
    var id: String { rawValue }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        }
    }

    var localizedName: LocalizedStringKey {
        switch self {
        case .system: return "theme.system"
        case .light: return "theme.light"
        case .dark: return "theme.dark"
        }
    }
}
