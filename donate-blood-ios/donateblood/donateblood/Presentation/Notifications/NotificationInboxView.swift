import SwiftUI

@MainActor
final class NotificationInboxViewModel: ObservableObject {
    @Published private(set) var items: [AppNotification] = []
    @Published private(set) var state: LoadState = .idle

    private let getInbox: GetNotificationInboxUseCase
    private let markRead: MarkNotificationsReadUseCase
    private let events: DataEvents

    init(
        getInbox: GetNotificationInboxUseCase,
        markRead: MarkNotificationsReadUseCase,
        events: DataEvents
    ) {
        self.getInbox = getInbox
        self.markRead = markRead
        self.events = events
    }

    func load() async {
        if items.isEmpty { state = .loading }
        do {
            items = try await getInbox()
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled, items.isEmpty { state = .failed(appError) }
        }
    }

    func open(_ item: AppNotification) async {
        guard let index = items.firstIndex(where: { $0.id == item.id }), !items[index].isRead else { return }
        items[index].isRead = true
        try? await markRead(id: item.id)
        events.notificationsChanged.send()
    }

    func markAllRead() async {
        for index in items.indices { items[index].isRead = true }
        try? await markRead.all()
        events.notificationsChanged.send()
    }
}

/// Bildirimler sekmesi (X'teki gibi): okunmamışlar hafif vurgulu, satıra dokununca ilgili ilana/sohbete gider.
struct NotificationInboxView: View {
    @StateObject var viewModel: NotificationInboxViewModel
    @EnvironmentObject private var session: SessionStore

    var body: some View {
        Group {
            if session.isGuest {
                EmptyStateView(
                    title: "notifications.guest.title", systemImage: "bell", message: "notifications.guest.message",
                    actionTitle: "auth.signIn", action: { session.isAuthSheetPresented = true }
                )
            } else {
                content
            }
        }
        .navigationTitle("notifications.title")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if !session.isGuest {
                ToolbarItem(placement: .primaryAction) {
                    Button("notifications.markAllRead") { Task { await viewModel.markAllRead() } }
                        .disabled(viewModel.items.allSatisfy(\.isRead))
                        .tint(.primary)
                }
            }
        }
        .task(id: "\(session.isGuest)-\(session.unreadNotifications)") { if !session.isGuest { await viewModel.load() } }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .idle, .loading: ProgressView().controlSize(.large)
        case .failed(let error): ErrorStateView(error: error) { Task { await viewModel.load() } }
        case .loaded:
            if viewModel.items.isEmpty {
                EmptyStateView(title: "notifications.empty.title", systemImage: "bell.slash", message: "notifications.empty.message")
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(viewModel.items) { item in
                            Button { Task { await open(item) } } label: { row(item) }
                                .buttonStyle(.plain)
                            HairlineDivider()
                        }
                    }
                }
                .refreshable { await viewModel.load() }
            }
        }
    }

    private func row(_ item: AppNotification) -> some View {
        HStack(alignment: .top, spacing: Theme.Spacing.m) {
            Image(systemName: icon(for: item.kind))
                .font(.title3).foregroundStyle(Theme.brand)
                .frame(width: Theme.Size.avatar, height: Theme.Size.avatar)
                .background(Theme.brand.opacity(0.12), in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                HStack {
                    Text(item.title).font(.subheadline.weight(.semibold)).lineLimit(2)
                    Spacer(minLength: Theme.Spacing.xs)
                    Text(item.createdAt.relativeDescription).font(.footnote).foregroundStyle(.secondary)
                }
                Text(item.body).font(.subheadline).foregroundStyle(.secondary).lineLimit(3)
            }
            if !item.isRead {
                Circle().fill(Theme.brand).frame(width: 8, height: 8).padding(.top, Theme.Spacing.s)
                    .accessibilityLabel(Text("notifications.unread"))
            }
        }
        .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
        .background(item.isRead ? Color.clear : Theme.brand.opacity(0.05))
        .contentShape(Rectangle())
        .accessibilityElement(children: .combine)
    }

    private func icon(for kind: AppNotification.Kind) -> String {
        switch kind {
        case .bloodRequest: return "drop.fill"
        case .donationPledge: return "heart.fill"
        case .chatMessage: return "bubble.left.fill"
        }
    }

    private func open(_ item: AppNotification) async {
        Haptics.selection()
        await viewModel.open(item)
        if let id = item.requestId { session.pendingLink = .request(id) }
        else if let id = item.roomId { session.pendingLink = .room(id) }
    }
}
