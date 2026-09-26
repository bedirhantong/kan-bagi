import Foundation

/// Sohbet hedefi: oda biliniyorsa `roomId`, yoksa `userId` ile oda oluşturulur/bulunur.
struct ChatTarget: Hashable {
    let roomId: UUID?
    let userId: UUID?
    let name: String

    static func room(id: UUID, userId: UUID?, name: String) -> ChatTarget { .init(roomId: id, userId: userId, name: name) }
    static func user(id: UUID, name: String) -> ChatTarget { .init(roomId: nil, userId: id, name: name) }
}

/// Tüm sekmelerde ortak kullanılan navigasyon hedefleri.
enum Route: Hashable {
    case request(UUID)
    case editRequest(BloodRequest)
    case hospital(Hospital)
    case chat(ChatTarget)
    case healthForm
    case settings
    case notificationSettings
    case notifications
    case languageSettings
    case faq
    case about
}
