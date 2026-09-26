import SwiftUI

/// Ana sayfa (X/Instagram akışı): üstte hikâyeler ve X'teki gibi "ilan ver" alanı, altında ilan kartları.
/// Sağ üstte okunmamış sayılı bildirim zili. İlan formu burada değil, sekme görünümünde açılır (tek sunum noktası).
struct HomeView: View {
    let container: AppContainer
    /// Yeni ilan (misafirde giriş kararı çağırandadır).
    let onCreate: (RequestPrefill) -> Void
    @StateObject private var viewModel: HomeViewModel
    @EnvironmentObject private var session: SessionStore
    @State private var openedStory: Story?

    init(container: AppContainer, onCreate: @escaping (RequestPrefill) -> Void) {
        self.container = container
        self.onCreate = onCreate
        _viewModel = StateObject(wrappedValue: container.makeHomeViewModel())
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: Theme.Spacing.m) {
                if !viewModel.stories.isEmpty {
                    StoryStrip(stories: viewModel.stories) { openedStory = $0 }
                        .padding(.top, Theme.Spacing.s)
                }
                RequestComposerCard(profile: session.profile, onCreate: onCreate)
                    .padding(.horizontal, Theme.Spacing.l) // ilan kartlarıyla aynı hiza
                    .padding(.top, viewModel.stories.isEmpty ? Theme.Spacing.s : 0)
                    .accessibilityIdentifier("home.composer")
                content
            }
        }
        .background(Theme.Palette.feedBackground.ignoresSafeArea())
        .refreshable { await viewModel.reload() }
        .navigationTitle(Text(verbatim: "BloodApp"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink(value: Route.notifications) {
                    NotificationBellIcon(unread: session.isGuest ? 0 : session.unreadNotifications)
                }
                .tint(.primary)
                .accessibilityIdentifier("home.notifications")
            }
        }
        .task { await viewModel.loadStories() }
        .task { await viewModel.reload() }
        .fullScreenCover(item: $openedStory) { StoryViewer(stories: viewModel.stories, start: $0) }
        .errorAlert($viewModel.error)
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .idle, .loading:
            ProgressView().controlSize(.large).padding(.top, 80)
        case .failed(let error):
            ErrorStateView(error: error) { Task { await viewModel.reload() } }.frame(minHeight: 320)
        case .loaded:
            if viewModel.requests.isEmpty {
                EmptyStateView(title: "home.empty.title", systemImage: "drop", message: "home.empty.message").frame(minHeight: 320)
            } else {
                ForEach(viewModel.requests) { request in
                    NavigationLink(value: Route.request(request.id)) {
                        RequestRow(
                            request: request,
                            distance: request.distance(from: viewModel.userLocation),
                            viewerBloodType: session.profile?.bloodType)
                    }
                    .buttonStyle(.plain)
                    .padding(.horizontal, Theme.Spacing.l)
                    .accessibilityIdentifier("home.requestCard")
                    .task { await viewModel.loadMoreIfNeeded(current: request) }
                }
                if viewModel.isLoadingMore { ProgressView().padding() }
            }
        }
    }
}

/// Üst çubuktaki bildirim zili: okunmamış varsa sağ üstte sayı rozeti (99+ kısaltılır).
struct NotificationBellIcon: View {
    let unread: Int

    var body: some View {
        Image(systemName: unread > 0 ? "bell.fill" : "bell")
            .font(.system(size: 17, weight: .semibold))
            .overlay(alignment: .topTrailing) {
                if unread > 0 {
                    Text(verbatim: unread > 99 ? "99+" : "\(unread)")
                        .font(.caption2.weight(.bold).monospacedDigit()).foregroundStyle(.white)
                        .padding(.horizontal, 4).frame(minWidth: 16, minHeight: 16)
                        .background(Theme.brand, in: Capsule())
                        .offset(x: 9, y: -7)
                }
            }
            .frame(minWidth: Theme.Size.minTap, minHeight: Theme.Size.minTap)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(unread > 0 ? L10n.format("notifications.unread %lld", unread) : L10n.string("tab.notifications")))
    }
}
