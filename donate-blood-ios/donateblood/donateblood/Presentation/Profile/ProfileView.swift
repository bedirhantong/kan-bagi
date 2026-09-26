import SwiftUI
import Combine


@MainActor
final class ProfileViewModel: ObservableObject {
    enum Tab: Hashable { case active, mine, pledged }

    @Published private(set) var myRequests: [BloodRequest] = []
    @Published private(set) var pledged: [BloodRequest] = []
    @Published private(set) var state: LoadState = .idle
    @Published var tab: Tab = .active
    @Published var error: AppError?

    private let getMyRequests: GetMyBloodRequestsUseCase
    private let getPledged: GetMyPledgedRequestsUseCase
    private let setActive: SetBloodRequestActiveUseCase
    private let events: DataEvents
    private var cancellables = Set<AnyCancellable>()

    init(
        getMyRequests: GetMyBloodRequestsUseCase,
        getPledged: GetMyPledgedRequestsUseCase,
        setActive: SetBloodRequestActiveUseCase,
        events: DataEvents
    ) {
        self.getMyRequests = getMyRequests
        self.getPledged = getPledged
        self.setActive = setActive
        self.events = events
        events.requestsChanged.sink { [weak self] in Task { await self?.load() } }.store(in: &cancellables)
    }

    var activeRequests: [BloodRequest] { myRequests.filter(\.isActive) }
    var pastRequests: [BloodRequest] { myRequests.filter { !$0.isActive } }

    func load() async {
        if state != .loaded { state = .loading }
        do {
            async let mine = getMyRequests()
            async let supported = getPledged()
            (myRequests, pledged) = try await (mine, supported)
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            guard appError != .cancelled else { return }
            if state == .loaded { self.error = appError } else { state = .failed(appError) }
        }
    }

    /// Kapanmış ilanı yeniden yayınlar.
    func republish(_ request: BloodRequest) async {
        do {
            try await setActive(id: request.id, isActive: true)
            events.requestsChanged.send()
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }
}

/// Profil (Instagram düzeni): büyük avatar + yan yana istatistikler, ad/bilgi, iki düğme, ikonlu sekmeler, ilan satırları.
struct ProfileView: View {
    let container: AppContainer
    @StateObject private var viewModel: ProfileViewModel
    @EnvironmentObject private var session: SessionStore
    @State private var isEditing = false

    init(container: AppContainer) {
        self.container = container
        _viewModel = StateObject(wrappedValue: container.makeProfileViewModel())
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                if session.isGuest {
                    guestHeader
                } else if let profile = session.profile {
                    header(profile)
                    tabs
                    HairlineDivider()
                    content
                }
            }
        }
        .refreshable { if !session.isGuest { await viewModel.load() } }
        .navigationTitle(session.profile?.fullName ?? L10n.string("tab.profile"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink(value: Route.settings) { Image(systemName: "gearshape") }
                    .tint(.primary)
                    .accessibilityLabel(Text("settings.title"))
                    .accessibilityIdentifier("profile.settings")
            }
        }
        .sheet(isPresented: $isEditing) {
            if let profile = session.profile {
                ProfileEditView(viewModel: container.makeProfileEditViewModel(profile: profile, onSaved: { session.profileDidChange($0) }))
            }
        }
        .task(id: session.isGuest) { if !session.isGuest { await viewModel.load() } }
        .errorAlert($viewModel.error)
    }

    // MARK: Başlık

    private func header(_ profile: Profile) -> some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.m) {
            HStack(spacing: Theme.Spacing.xl) {
                AvatarView(name: profile.fullName.isEmpty ? "?" : profile.fullName, seed: profile.id, size: Theme.Size.profileAvatar)
                    .overlay(alignment: .bottomTrailing) {
                        if let bloodType = profile.bloodType {
                            Text(bloodType.displayName)
                                .font(.caption2.weight(.heavy)).foregroundStyle(.white)
                                .padding(.horizontal, 7).padding(.vertical, 3)
                                .background(Theme.brand, in: Capsule())
                                .overlay(Capsule().stroke(Theme.Palette.background, lineWidth: 2))
                                .offset(x: 6, y: 4)
                        }
                    }
                Spacer(minLength: 0)
                stat(value: viewModel.myRequests.count, label: "profile.myRequests")
                stat(value: viewModel.pledged.count, label: "profile.pledgeCount")
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(profile.fullName).font(.headline)
                if let phone = profile.phoneNumber, !phone.isEmpty {
                    Text(phone).font(.subheadline).foregroundStyle(.secondary)
                }
                HStack(spacing: Theme.Spacing.m) {
                    Label("profile.country", systemImage: "mappin.and.ellipse")
                    if let createdAt = profile.createdAt {
                        Label("profile.memberSince \(createdAt.formatted(.dateTime.month(.wide).year()))", systemImage: "calendar")
                    }
                }
                .font(.footnote).foregroundStyle(.secondary)
            }

            HStack(spacing: Theme.Spacing.s) {
                Button("common.edit") { isEditing = true }.buttonStyle(SecondaryPillButtonStyle())
                NavigationLink(value: Route.healthForm) { Text("profile.healthForm") }.buttonStyle(SecondaryPillButtonStyle())
            }
        }
        .padding(Theme.Spacing.l)
    }

    private func stat(value: Int, label: LocalizedStringKey) -> some View {
        VStack(spacing: 2) {
            Text(verbatim: "\(value)").font(.title3.weight(.bold))
            Text(label).font(.footnote).foregroundStyle(.secondary).multilineTextAlignment(.center)
        }
        .frame(minWidth: 72)
        .accessibilityElement(children: .combine)
    }

    private var guestHeader: some View {
        VStack(spacing: Theme.Spacing.m) {
            AvatarView(name: "?", seed: UUID(), size: Theme.Size.profileAvatar)
            Text("profile.guest.title").font(.headline)
            Text("profile.guest.message").font(.subheadline).foregroundStyle(.secondary).multilineTextAlignment(.center)
            Button("auth.signIn") { session.isAuthSheetPresented = true }
                .buttonStyle(PrimaryPillButtonStyle(fullWidth: false))
        }
        .frame(maxWidth: .infinity)
        .padding(Theme.Spacing.xl)
    }

    // MARK: Sekmeler ve liste

    private var tabs: some View {
        IconTabStrip(
            items: [
                .init(tab: ProfileViewModel.Tab.active, systemImage: "bolt", label: "profile.tab.active"),
                .init(tab: .mine, systemImage: "tray.full", label: "profile.tab.mine"),
                .init(tab: .pledged, systemImage: "heart", label: "profile.tab.pledged"),
            ],
            selection: $viewModel.tab)
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .idle, .loading:
            ProgressView().controlSize(.large).padding(.top, 60)
        case .failed(let error):
            ErrorStateView(error: error) { Task { await viewModel.load() } }.frame(minHeight: 260)
        case .loaded:
            switch viewModel.tab {
            case .active: list(viewModel.activeRequests, empty: "profile.empty.active")
            case .mine: list(viewModel.pastRequests, empty: "profile.empty.mine", showsRepublish: true)
            case .pledged: list(viewModel.pledged, empty: "profile.empty.pledged")
            }
        }
    }

    @ViewBuilder
    private func list(_ requests: [BloodRequest], empty: LocalizedStringKey, showsRepublish: Bool = false) -> some View {
        if requests.isEmpty {
            Text(empty).font(.subheadline).foregroundStyle(.secondary).multilineTextAlignment(.center)
                .frame(maxWidth: .infinity, minHeight: 200).padding(.horizontal, Theme.Spacing.xl)
        } else {
            ForEach(requests) { request in
                NavigationLink(value: Route.request(request.id)) {
                    RequestRow(
                        request: request,
                        showsTitle: true,
                        trailingAction: showsRepublish ? ("profile.republish", "arrow.clockwise", { Task { await viewModel.republish(request) } }) : nil)
                }
                .buttonStyle(.plain)
                .padding(.horizontal, Theme.Spacing.l)
                .padding(.vertical, Theme.Spacing.xs + 2)
            }
        }
    }
}
