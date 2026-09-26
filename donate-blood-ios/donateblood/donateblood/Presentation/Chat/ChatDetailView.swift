import SwiftUI

struct ChatDetailView: View {
    @StateObject var viewModel: ChatDetailViewModel
    @EnvironmentObject private var session: SessionStore
    @Environment(\.dismiss) private var dismiss
    @State private var isChoosingReportReason = false
    @State private var isConfirmingBlock = false

    var body: some View {
        Group {
            switch viewModel.state {
            case .idle, .loading: ProgressView().controlSize(.large)
            case .failed(let error): ErrorStateView(error: error) { Task { await viewModel.start() } }
            case .loaded: messageList
            }
        }
        .navigationTitle(viewModel.target.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                VStack(spacing: 1) {
                    Text(viewModel.target.name).font(.headline)
                    if viewModel.isLive {
                        HStack(spacing: 4) {
                            Circle().fill(Color.green).frame(width: 7, height: 7)
                            Text("chat.connected").font(.caption2).foregroundStyle(.secondary)
                        }
                    }
                }
                .accessibilityElement(children: .combine)
            }
            if viewModel.otherUserId != nil {
                ToolbarItem(placement: .primaryAction) {
                    Menu {
                        Button { isChoosingReportReason = true } label: { Label("safety.report", systemImage: "flag") }
                        Button(role: .destructive) { isConfirmingBlock = true } label: { Label("safety.block", systemImage: "hand.raised") }
                    } label: { Image(systemName: "ellipsis.circle") }
                }
            }
        }
        .task { await viewModel.start() }
        .onDisappear { viewModel.stop() }
        .onChange(of: viewModel.didBlock) { if $0 { dismiss() } }
        .errorAlert($viewModel.error)
        .alert(item: $viewModel.infoMessage) { Alert(title: Text($0.text), dismissButton: .default(Text("common.ok"))) }
        .confirmationDialog("safety.reportReason", isPresented: $isChoosingReportReason, titleVisibility: .visible) {
            ForEach(ReportReason.forUsers) { reason in
                Button(reason.title) { Task { await viewModel.report(reason: reason.rawValue) } }
            }
        }
        .confirmationDialog("safety.block.confirm", isPresented: $isConfirmingBlock, titleVisibility: .visible) {
            Button("safety.block", role: .destructive) { Task { await viewModel.block() } }
        } message: { Text("safety.block.message") }
    }

    private var messageList: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(spacing: 6) {
                    if viewModel.canLoadOlder {
                        Button("chat.loadOlder") { Task { await viewModel.loadOlder() } }
                            .font(.footnote).padding(.vertical, 8)
                    }
                    ForEach(viewModel.messages) { message in
                        MessageBubble(message: message, isMine: message.senderId == session.user?.id)
                            .id(message.id)
                    }
                }
                .padding(.horizontal)
            }
            .onChange(of: viewModel.messages.last?.id) { id in
                guard let id else { return }
                withAnimation { proxy.scrollTo(id, anchor: .bottom) }
            }
            .onAppear {
                if let id = viewModel.messages.last?.id { proxy.scrollTo(id, anchor: .bottom) }
            }
        }
        .safeAreaInset(edge: .bottom) { inputBar }
        .overlay {
            if viewModel.messages.isEmpty {
                EmptyStateView(title: "chat.startConversation", systemImage: "bubble.left")
            }
        }
    }

    /// Instagram/X DM girişi: kapsül alan, yazı varken beliren gönder butonu.
    private var inputBar: some View {
        let canSend = !viewModel.draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !viewModel.isSending
        return HStack(alignment: .bottom, spacing: Theme.Spacing.s) {
            TextField("chat.messagePlaceholder", text: $viewModel.draft, axis: .vertical)
                .lineLimit(1...5)
                .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.s + 2)
                .background(Theme.Palette.surface, in: RoundedRectangle(cornerRadius: 22, style: .continuous))
            if canSend {
                Button {
                    Haptics.impact()
                    Task { await viewModel.send() }
                } label: {
                    Image(systemName: "arrow.up.circle.fill").font(.system(size: 34)).foregroundStyle(Theme.brand)
                }
                .transition(.scale.combined(with: .opacity))
                .accessibilityLabel(Text("chat.send"))
            }
        }
        .animation(.easeInOut(duration: 0.15), value: canSend)
        .padding(.horizontal, Theme.Spacing.m).padding(.vertical, Theme.Spacing.s)
        .background(.bar)
    }
}

struct MessageBubble: View {
    let message: ChatMessage
    let isMine: Bool

    var body: some View {
        HStack {
            if isMine { Spacer(minLength: 56) }
            VStack(alignment: isMine ? .trailing : .leading, spacing: 2) {
                Text(message.content)
                    .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.s + 2)
                    .foregroundStyle(isMine ? Color.white : Color.primary)
                    .background(isMine ? Theme.brand : Theme.Palette.surface, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
                Text(message.createdAt.formatted(.dateTime.hour().minute()))
                    .font(.caption2).foregroundStyle(.secondary).padding(.horizontal, Theme.Spacing.xs)
            }
            if !isMine { Spacer(minLength: 56) }
        }
        .accessibilityElement(children: .combine)
    }
}
