import Foundation

struct GetChatRoomsUseCase: Sendable {
    let chat: ChatRepository
    func callAsFunction() async throws -> [ChatRoom] { try await chat.rooms() }
}

struct OpenChatRoomUseCase: Sendable {
    let chat: ChatRepository
    func callAsFunction(with userId: UUID) async throws -> UUID { try await chat.room(with: userId) }
}

struct GetMessagesUseCase: Sendable {
    let chat: ChatRepository
    /// Sonuç kronolojik (eskiden yeniye) sıralıdır.
    func callAsFunction(roomId: UUID, before: Date?, limit: Int = 30) async throws -> [ChatMessage] {
        try await chat.messages(roomId: roomId, before: before, limit: limit).sorted { $0.createdAt < $1.createdAt }
    }
}

struct SendMessageUseCase: Sendable {
    let chat: ChatRepository
    func callAsFunction(roomId: UUID, content: String) async throws {
        let text = content.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, text.count <= 2000 else { throw AppError.invalidInput(L10n.string("chat.message")) }
        try await chat.send(roomId: roomId, content: text)
    }
}

struct ObserveMessagesUseCase: Sendable {
    let chat: ChatRepository
    func callAsFunction(roomId: UUID) -> AsyncStream<ChatMessage> { chat.incomingMessages(roomId: roomId) }
}
