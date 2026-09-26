import Foundation

struct ChatRoom: Identifiable, Equatable, Hashable, Sendable {
    let id: UUID
    let otherUserId: UUID
    let otherUserName: String
    let lastMessage: String?
    let lastMessageAt: Date
}

struct ChatMessage: Identifiable, Equatable, Hashable, Sendable {
    let id: UUID
    let roomId: UUID
    let senderId: UUID
    let content: String
    let createdAt: Date
}
