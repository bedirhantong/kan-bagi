import Foundation

@MainActor
final class ChatDetailViewModel: ObservableObject {
    @Published private(set) var messages: [ChatMessage] = []
    @Published private(set) var state: LoadState = .idle
    @Published private(set) var isSending = false
    @Published private(set) var canLoadOlder = true
    @Published private(set) var isLive = false
    @Published var draft = ""
    @Published var error: AppError?
    @Published var didBlock = false
    @Published var infoMessage: InfoMessage?

    let target: ChatTarget
    private(set) var roomId: UUID?
    private let pageSize = 30

    private let openRoom: OpenChatRoomUseCase
    private let getMessages: GetMessagesUseCase
    private let sendMessage: SendMessageUseCase
    private let observeMessages: ObserveMessagesUseCase
    private let reportContent: ReportContentUseCase
    private let blockUser: BlockUserUseCase
    private var observeTask: Task<Void, Never>?

    init(
        openRoom: OpenChatRoomUseCase,
        getMessages: GetMessagesUseCase,
        sendMessage: SendMessageUseCase,
        observeMessages: ObserveMessagesUseCase,
        reportContent: ReportContentUseCase,
        blockUser: BlockUserUseCase,
        target: ChatTarget
    ) {
        self.openRoom = openRoom
        self.getMessages = getMessages
        self.sendMessage = sendMessage
        self.observeMessages = observeMessages
        self.reportContent = reportContent
        self.blockUser = blockUser
        self.target = target
        roomId = target.roomId
    }

    var otherUserId: UUID? { target.userId }

    func start() async {
        guard state != .loaded else { return }
        state = .loading
        do {
            let id = try await resolveRoom()
            let page = try await getMessages(roomId: id, before: nil, limit: pageSize)
            messages = page
            canLoadOlder = page.count == pageSize
            state = .loaded
            observe(roomId: id)
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { state = .failed(appError) }
        }
    }

    func stop() {
        observeTask?.cancel()
        observeTask = nil
    }

    func loadOlder() async {
        guard let roomId, canLoadOlder, let oldest = messages.first else { return }
        do {
            let page = try await getMessages(roomId: roomId, before: oldest.createdAt, limit: pageSize)
            let known = Set(messages.map(\.id))
            let fresh = page.filter { !known.contains($0.id) }
            messages.insert(contentsOf: fresh, at: 0)
            canLoadOlder = !fresh.isEmpty && page.count == pageSize
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }

    func send() async {
        let text = draft
        guard let roomId, !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty, !isSending else { return }
        isSending = true
        draft = ""
        defer { isSending = false }
        do {
            try await sendMessage(roomId: roomId, content: text)
            // Realtime gecikirse/kopmuşsa kendi mesajımız yine görünsün.
            await refreshLatest()
        } catch {
            draft = text
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }

    func report(reason: String) async {
        guard let userId = otherUserId else { return }
        do {
            try await reportContent(target: .user, targetId: userId.uuidString, reason: reason)
            infoMessage = .reported
        } catch { self.error = AppError.from(error) }
    }

    func block() async {
        guard let userId = otherUserId else { return }
        do {
            try await blockUser(userId: userId)
            didBlock = true
        } catch { self.error = AppError.from(error) }
    }

    // MARK: Private

    private func resolveRoom() async throws -> UUID {
        if let roomId { return roomId }
        guard let userId = otherUserId else { throw AppError.notFound }
        let id = try await openRoom(with: userId)
        roomId = id
        return id
    }

    private func merge(_ incoming: [ChatMessage]) {
        let known = Set(messages.map(\.id))
        let fresh = incoming.filter { !known.contains($0.id) }
        guard !fresh.isEmpty else { return }
        messages = (messages + fresh).sorted { $0.createdAt < $1.createdAt }
    }

    private func refreshLatest() async {
        guard let roomId, let page = try? await getMessages(roomId: roomId, before: nil, limit: pageSize) else { return }
        merge(page)
    }

    /// Bağlantı koparsa üstel geri çekilmeyle yeniden abone olur ve kaçırılan mesajları çeker.
    private func observe(roomId: UUID) {
        observeTask?.cancel()
        observeTask = Task { [weak self] in
            var attempt = 0
            while !Task.isCancelled {
                guard let stream = self?.observeMessages(roomId: roomId) else { return }
                self?.isLive = true
                for await message in stream {
                    attempt = 0
                    self?.merge([message])
                }
                self?.isLive = false
                if Task.isCancelled { return }
                attempt += 1
                let delay = min(30, 1 << min(attempt, 5))
                try? await Task.sleep(nanoseconds: UInt64(delay) * 1_000_000_000)
                await self?.refreshLatest()
            }
        }
    }
}
