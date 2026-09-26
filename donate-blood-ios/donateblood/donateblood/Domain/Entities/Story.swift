import Foundation

/// Ana sayfadaki story şeridinde gösterilen bilgilendirme içeriği.
struct Story: Identifiable, Equatable, Hashable, Sendable {
    let id: UUID
    let title: String
    let body: String?
    let imageURL: URL?
    let logoURL: URL?
    let linkURL: URL?

    var initial: String { String(title.first ?? "?").uppercased() }
}
