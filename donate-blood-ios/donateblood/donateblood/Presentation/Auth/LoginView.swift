import SwiftUI
import AuthenticationServices

/// Giriş ekranı (X/Instagram düzeni): sade zemin, marka logosu, kısa fayda listesi, tam genişlik hap düğmeler.
/// - `entry`: açılışta; misafir girişi de vardır.
/// - `upgrade`: misafir hesaba geçer (sayfa olarak açılır); misafir verisi korunur.
struct LoginView: View {
    enum Mode { case entry, upgrade }

    @StateObject var viewModel: LoginViewModel
    let mode: Mode
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: Theme.Spacing.xl) {
                    header
                    if mode == .entry { features }
                    buttons
                    Text(LegalText.attributed())
                        .font(.footnote).foregroundStyle(.secondary).tint(Theme.brand)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    if viewModel.isEmailLoginEnabled {
                        DeveloperSignInSection(viewModel: viewModel)
                    }
                }
                .padding(.horizontal, Theme.Spacing.xl)
                .padding(.top, mode == .entry ? Theme.Spacing.xl * 2 : Theme.Spacing.l)
                .padding(.bottom, Theme.Spacing.xl)
                .frame(maxWidth: 520) // iPad ve yatayda okunabilir genişlik
                .frame(maxWidth: .infinity)
            }
            .toolbar {
                if mode == .upgrade {
                    ToolbarItem(placement: .cancellationAction) { Button("common.cancel") { dismiss() }.tint(.primary) }
                }
            }
            .navigationBarTitleDisplayMode(.inline)
        }
        .interactiveDismissDisabled(viewModel.isLoading)
        .errorAlert($viewModel.error)
    }

    // MARK: Bölümler

    private var header: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.m) {
            BloodDropShape().fill(Theme.brand).frame(width: 44, height: 66).accessibilityHidden(true)
            Text(mode == .entry ? "login.headline" : "login.upgrade.title")
                .font(.largeTitle.weight(.bold)).fixedSize(horizontal: false, vertical: true)
            Text(mode == .entry ? "login.subtitle" : "login.upgradeSubtitle")
                .font(.body).foregroundStyle(.secondary).fixedSize(horizontal: false, vertical: true)
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isHeader)
    }

    private var features: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.m) {
            FeatureRow(systemImage: "bell.badge.fill", text: "login.feature.alerts")
            FeatureRow(systemImage: "map.fill", text: "login.feature.map")
            FeatureRow(systemImage: "bubble.left.and.bubble.right.fill", text: "login.feature.chat")
        }
    }

    private var buttons: some View {
        VStack(spacing: Theme.Spacing.m) {
            AppleSignInButton(isLoading: viewModel.activeProvider == .apple,
                              onRequest: viewModel.prepareApple,
                              onCompletion: { result in Task { await viewModel.handleApple(result) } })

            Button { Task { await viewModel.google() } } label: {
                HStack(spacing: Theme.Spacing.s) {
                    if viewModel.activeProvider == .google {
                        ProgressView()
                    } else {
                        Image("GoogleG").resizable().scaledToFit().frame(width: 20, height: 20).accessibilityHidden(true)
                    }
                    Text("auth.signInWithGoogle")
                }
            }
            .buttonStyle(OutlinePillButtonStyle())

            if mode == .entry {
                HStack(spacing: Theme.Spacing.m) {
                    Rectangle().fill(Theme.Palette.separator).frame(height: 1)
                    Text("login.or").font(.footnote).foregroundStyle(.secondary)
                    Rectangle().fill(Theme.Palette.separator).frame(height: 1)
                }
                .accessibilityHidden(true)

                Button { Task { await viewModel.guest() } } label: {
                    HStack(spacing: Theme.Spacing.s) {
                        if viewModel.activeProvider == .guest { ProgressView() }
                        Text("auth.continueAsGuest")
                    }
                }
                .buttonStyle(SecondaryPillButtonStyle())
            }
        }
        .disabled(viewModel.isLoading)
    }
}

/// Yerel "Apple ile Giriş" düğmesi: açık temada siyah, koyu temada beyaz (Apple HIG); hap biçimli.
private struct AppleSignInButton: View {
    let isLoading: Bool
    let onRequest: (ASAuthorizationAppleIDRequest) -> Void
    let onCompletion: (Result<ASAuthorization, Error>) -> Void
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        SignInWithAppleButton(.continue, onRequest: onRequest, onCompletion: onCompletion)
            .signInWithAppleButtonStyle(colorScheme == .dark ? .white : .black)
            .frame(height: 50)
            .clipShape(Capsule())
            .overlay { if isLoading { Capsule().fill(.black.opacity(0.35)); ProgressView().tint(.white) } }
            .id(colorScheme) // stil değişince düğme yeniden çizilsin
    }
}

/// Geliştirici girişi (yalnızca `EMAIL_LOGIN_ENABLED`): hazır test hesapları ve e-posta/şifre.
private struct DeveloperSignInSection: View {
    @ObservedObject var viewModel: LoginViewModel
    @State private var isExpanded = true

    var body: some View {
        DisclosureGroup(isExpanded: $isExpanded) {
            VStack(alignment: .leading, spacing: Theme.Spacing.m) {
                LazyVGrid(columns: [GridItem(.flexible(), spacing: Theme.Spacing.s), GridItem(.flexible())],
                          spacing: Theme.Spacing.s) {
                    ForEach(TestAccount.all) { account in
                        Button { Task { await viewModel.signIn(as: account) } } label: {
                            Text(account.role.title).font(.footnote.weight(.semibold)).multilineTextAlignment(.center)
                                .padding(.vertical, Theme.Spacing.s)
                        }
                        .buttonStyle(SecondaryPillButtonStyle())
                    }
                }
                FormField(label: "login.email") {
                    TextField("login.email.placeholder", text: $viewModel.email)
                        .textContentType(.username).keyboardType(.emailAddress)
                        .textInputAutocapitalization(.never).autocorrectionDisabled()
                }
                FormField(label: "login.password") {
                    SecureField("login.password", text: $viewModel.password).textContentType(.password)
                }
                Button { Task { await viewModel.emailSignIn() } } label: {
                    HStack(spacing: Theme.Spacing.s) {
                        if viewModel.activeProvider == .email { ProgressView().tint(.white) }
                        Text("login.emailSignIn")
                    }
                }
                .buttonStyle(PrimaryPillButtonStyle())
                .disabled(!viewModel.canSubmitEmail)
            }
            .padding(.top, Theme.Spacing.s)
        } label: {
            Label("login.testAccount", systemImage: "hammer").font(.subheadline.weight(.semibold))
        }
        .tint(.primary)
        .padding(Theme.Spacing.l)
        // Dolgu yerine çerçeve: içerideki düğme ve alanlar (surface zeminli) ayırt edilebilsin.
        .overlay(RoundedRectangle(cornerRadius: Theme.Radius.card, style: .continuous)
            .strokeBorder(Theme.Palette.separator.opacity(0.6), lineWidth: 1))
        .disabled(viewModel.isLoading)
    }
}

private extension TestAccount.Role {
    var title: LocalizedStringKey {
        switch self {
        case .donor: return "login.test.donor"
        case .secondDonor: return "login.test.donor2"
        case .requester: return "login.test.requester"
        case .newUser: return "login.test.newUser"
        }
    }
}
