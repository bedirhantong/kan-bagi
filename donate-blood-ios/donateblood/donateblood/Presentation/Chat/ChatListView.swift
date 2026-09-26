import SwiftUI

@MainActor
final class ChatListViewModel: ObservableObject {
    @Published private(set) var rooms: [ChatRoom] = []
    @Published private(set) var state: LoadState = .idle

    private let getRooms: GetChatRoomsUseCase

    init(
        getRooms: GetChatRoomsUseCase
    ) {
        self.getRooms = getRooms
    }

    func load() async {
        if rooms.isEmpty { state = .loading }
        do {
            rooms = try await getRooms()
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            guard appError != .cancelled else { return }
            if rooms.isEmpty { state = .failed(appError) }
        }
    }
}

struct ChatListView: View {
    @StateObject private var viewModel: ChatListViewModel
    @EnvironmentObject private var session: SessionStore

    init(container: AppContainer) {
        _viewModel = StateObject(wrappedValue: container.makeChatListViewModel())
    }

    var body: some View {
        Group {
            if session.isGuest {
                EmptyStateView(
                    title: "chat.guest.title", systemImage: "bubble.left.and.bubble.right", message: "chat.guest.message",
                    actionTitle: "auth.signIn", action: { session.isAuthSheetPresented = true }
                )
            } else {
                content
            }
        }
        .navigationTitle("chat.title")
        .navigationBarTitleDisplayMode(.inline)
        .task(id: session.isGuest) { if !session.isGuest { await viewModel.load() } }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.state {
        case .idle, .loading: ProgressView().controlSize(.large)
        case .failed(let error): ErrorStateView(error: error) { Task { await viewModel.load() } }
        case .loaded:
            if viewModel.rooms.isEmpty {
                EmptyStateView(title: "chat.empty.title", systemImage: "bubble.left", message: "chat.empty.message")
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(viewModel.rooms) { room in
                            NavigationLink(value: Route.chat(.room(id: room.id, userId: room.otherUserId, name: room.otherUserName))) {
                                HStack(spacing: Theme.Spacing.m) {
                                    AvatarView(name: room.otherUserName, seed: room.otherUserId)
                                    VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                                        HStack {
                                            Text(room.otherUserName).font(.subheadline.weight(.semibold)).lineLimit(1)
                                            Spacer(minLength: Theme.Spacing.xs)
                                            Text(room.lastMessageAt.relativeDescription).font(.footnote).foregroundStyle(.secondary)
                                        }
                                        Text(room.lastMessage ?? L10n.string("chat.noMessages"))
                                            .font(.subheadline).foregroundStyle(.secondary).lineLimit(2)
                                    }
                                }
                                .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
                                .contentShape(Rectangle())
                                .accessibilityElement(children: .combine)
                            }
                            .buttonStyle(.plain)
                            HairlineDivider()
                        }
                    }
                }
                .refreshable { await viewModel.load() }
            }
        }
    }
}
