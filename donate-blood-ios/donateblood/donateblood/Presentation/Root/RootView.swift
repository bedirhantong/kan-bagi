import SwiftUI

struct RootView: View {
    let container: AppContainer
    @EnvironmentObject private var session: SessionStore
    @AppStorage("hasSeenOnboarding") private var hasSeenOnboarding = false
    @State private var isSplashVisible = true

    var body: some View {
        Group {
            if isSplashVisible {
                SplashView()
            } else {
                content
            }
        }
        .task {
            try? await Task.sleep(nanoseconds: 1_600_000_000)
            withAnimation { isSplashVisible = false }
        }
        .sheet(isPresented: $session.isAuthSheetPresented) {
            LoginView(viewModel: container.makeLoginViewModel(), mode: .upgrade)
        }
    }

    private var content: some View {
        #if DEBUG
        if let step = DebugLaunch.registrationPreview {
            return AnyView(RegistrationView(viewModel: container.makeRegistrationViewModel(
                profile: Profile(id: UUID(), name: nil, surname: nil, email: nil, phoneNumber: nil, birthDate: nil,
                                 bloodType: nil, gender: nil, userType: .regularUser, isProfileCompleted: false, createdAt: nil),
                initialStep: step, onCompleted: { _ in }, onSignOut: {})))
        }
        #endif
        return AnyView(signedOutOrIn)
    }

    private var signedOutOrIn: some View {
        Group {
            switch session.phase {
            case .loading:
                ProgressView().controlSize(.large)
            case .signedOut:
                if hasSeenOnboarding {
                    LoginView(viewModel: container.makeLoginViewModel(), mode: .entry)
                } else {
                    OnboardingView { hasSeenOnboarding = true }
                }
            case .signedIn:
                signedInContent
            }
        }
        .animation(.default, value: session.phase)
    }

    @ViewBuilder
    private var signedInContent: some View {
        if session.isGuest {
            MainTabView(container: container)
        } else if let profile = session.profile {
            if profile.isProfileCompleted {
                MainTabView(container: container)
            } else {
                RegistrationView(viewModel: container.makeRegistrationViewModel(
                    profile: profile,
                    onCompleted: { await session.profileCompleted($0) },
                    onSignOut: { try? await session.signOut() }))
            }
        } else if session.profileLoadFailed {
            ErrorStateView(error: .network) { Task { await session.reloadProfile() } }
        } else {
            ProgressView().controlSize(.large)
        }
    }
}
