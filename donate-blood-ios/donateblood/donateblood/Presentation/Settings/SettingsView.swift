import SwiftUI
import UserNotifications

/// Android ayarlar ekranının bölümleri: Hesap Ayarları, Uygulama Ayarları, Yasal ve Destek.
struct SettingsView: View {
    @StateObject var viewModel: SettingsViewModel
    let container: AppContainer
    @EnvironmentObject private var session: SessionStore
    @AppStorage("appearance") private var appearance = AppearanceSetting.system.rawValue
    @State private var isConfirmingSignOut = false
    @State private var isConfirmingDelete = false
    @State private var isEditingProfile = false

    var body: some View {
        Form {
            Section("settings.accountSettings") {
                if session.isGuest {
                    Button { session.isAuthSheetPresented = true } label: {
                        row("auth.signIn", subtitle: "settings.signIn.subtitle", systemImage: "person.badge.plus")
                    }
                    .buttonStyle(.plain)
                } else {
                    Button { isEditingProfile = true } label: {
                        row("settings.profileInfo", subtitle: "settings.profileInfo.subtitle", systemImage: "person")
                    }
                    .buttonStyle(.plain)
                    NavigationLink(value: Route.healthForm) {
                        row("settings.viewForm", subtitle: "settings.viewForm.subtitle", systemImage: "heart.text.square")
                    }
                    NavigationLink(value: Route.notificationSettings) {
                        row("settings.notifications", subtitle: "settings.notifications.subtitle", systemImage: "bell")
                    }
                }
                Button(role: .destructive) { isConfirmingSignOut = true } label: {
                    row("auth.signOut", subtitle: "settings.signOut.subtitle", systemImage: "rectangle.portrait.and.arrow.right", destructive: true)
                }
                .buttonStyle(.plain)
                if !session.isGuest {
                    Button(role: .destructive) { isConfirmingDelete = true } label: {
                        row("settings.deleteAccount", subtitle: "settings.deleteAccount.subtitle", systemImage: "trash", destructive: true)
                    }
                    .buttonStyle(.plain)
                }
            }

            Section("settings.appSettings") {
                NavigationLink(value: Route.languageSettings) {
                    row("settings.language", subtitle: "settings.language.subtitle", systemImage: "globe")
                }

                Picker(selection: $appearance) {
                    ForEach(AppearanceSetting.allCases) { Text($0.localizedName).tag($0.rawValue) }
                } label: { row("settings.theme", subtitle: "settings.theme.subtitle", systemImage: "circle.lefthalf.filled") }
            }

            Section("settings.legalSupport") {
                Link(destination: AppLinks.privacy) { row("settings.privacy", subtitle: "settings.privacy.subtitle", systemImage: "hand.raised") }
                    .tint(.primary)
                Link(destination: AppLinks.terms) { row("settings.terms", subtitle: "settings.terms.subtitle", systemImage: "doc.text") }
                    .tint(.primary)
                NavigationLink(value: Route.faq) { row("settings.faq", subtitle: "settings.faq.subtitle", systemImage: "questionmark.circle") }
                NavigationLink(value: Route.about) { row("settings.about", subtitle: "settings.about.subtitle", systemImage: "info.circle") }
            }
        }
        .navigationTitle("settings.title")
        .disabled(viewModel.isWorking)
        .overlay { if viewModel.isWorking { ProgressView().controlSize(.large) } }
        .sheet(isPresented: $isEditingProfile) {
            if let profile = session.profile {
                ProfileEditView(viewModel: container.makeProfileEditViewModel(profile: profile, onSaved: { session.profileDidChange($0) }))
            }
        }
        .confirmationDialog("settings.signOut.confirm", isPresented: $isConfirmingSignOut, titleVisibility: .visible) {
            Button("auth.signOut", role: .destructive) { Task { await viewModel.signOut(session: session) } }
        }
        .confirmationDialog("settings.deleteAccount.confirm", isPresented: $isConfirmingDelete, titleVisibility: .visible) {
            Button("settings.deleteAccount", role: .destructive) { Task { await viewModel.deleteMyAccount() } }
        } message: { Text("settings.deleteAccount.message") }
        .errorAlert($viewModel.error)
    }

    /// Başlık + açıklama satırı (Android'deki gibi).
    private func row(_ title: LocalizedStringKey, subtitle: LocalizedStringKey, systemImage: String, destructive: Bool = false) -> some View {
        Label {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                Text(subtitle).font(.caption).foregroundStyle(.secondary)
            }
        } icon: { Image(systemName: systemImage) }
        .foregroundStyle(destructive ? Color.red : Color.primary)
        .frame(maxWidth: .infinity, alignment: .leading)
        .contentShape(Rectangle())
    }
}

struct FAQView: View {
    @State private var query = ""

    private var filtered: [FAQItem] {
        let term = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !term.isEmpty else { return FAQCatalog.items }
        return FAQCatalog.items.filter {
            $0.question.localizedCaseInsensitiveContains(term) || $0.answer.localizedCaseInsensitiveContains(term)
        }
    }

    var body: some View {
        List {
            ForEach(filtered) { item in
                DisclosureGroup {
                    Text(item.answer).font(.subheadline).foregroundStyle(.secondary).padding(.vertical, 4)
                } label: {
                    Text(item.question).font(.headline)
                }
            }
        }
        .overlay { if filtered.isEmpty { EmptyStateView(title: "faq.noResults", systemImage: "magnifyingglass") } }
        .navigationTitle("settings.faq")
        .searchable(text: $query, prompt: Text("faq.searchPrompt"))
    }
}

struct AboutView: View {
    private var version: String {
        let info = Bundle.main.infoDictionary
        return "\(info?["CFBundleShortVersionString"] as? String ?? "1.0") (\(info?["CFBundleVersion"] as? String ?? "1"))"
    }

    var body: some View {
        List {
            Section {
                VStack(spacing: 8) {
                    BloodDropShape().fill(Theme.brand).frame(width: 44, height: 66).accessibilityHidden(true)
                    Text(verbatim: "BloodApp").font(.system(.title, design: .serif).bold())
                    Text("about.tagline").font(.subheadline).foregroundStyle(.secondary).multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical)
            }
            Section { LabeledContent("about.version", value: version) }
            Section {
                Text("about.disclaimer").font(.footnote).foregroundStyle(.secondary)
                if let sourceCode = AppLinks.sourceCode {
                    Link(destination: sourceCode) { Label("about.openSource", systemImage: "chevron.left.forwardslash.chevron.right") }
                }
            } footer: { Text("about.license").font(.caption) }
        }
        .navigationTitle("settings.about")
    }
}
