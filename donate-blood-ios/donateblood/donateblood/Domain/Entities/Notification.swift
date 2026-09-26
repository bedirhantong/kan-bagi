import Foundation

struct NotificationPreferences: Equatable, Sendable {
    /// Ana anahtar: kapalıysa hiçbir push gönderilmez.
    var pushEnabled: Bool
    var bloodTypeAlerts: Bool
    var chatNotifications: Bool
    /// Boşsa profildeki kan grubuna uyumlu ilanlar bildirilir.
    var preferredBloodTypes: Set<BloodType>
    /// Bildirim bölgesi: seçilen il/ilçeler (`NotificationArea.key`) ve hastaneler.
    /// İkisi de boşsa ilan bildirimi gelmez (tüm ülkeye yayın yoktur).
    var preferredAreaKeys: Set<String>
    var preferredHospitalIds: Set<Int>

    var scopeCount: Int { preferredAreaKeys.count + preferredHospitalIds.count }
    var hasScope: Bool { scopeCount > 0 }

    static let `default` = NotificationPreferences(
        pushEnabled: true, bloodTypeAlerts: true, chatNotifications: true,
        preferredBloodTypes: [], preferredAreaKeys: [], preferredHospitalIds: []
    )
}

struct AppNotification: Identifiable, Equatable, Sendable {
    enum Kind: String, Sendable { case bloodRequest = "BLOOD_REQUEST", donationPledge = "DONATION_PLEDGE", chatMessage = "CHAT_MESSAGE" }

    let id: UUID
    let kind: Kind
    let title: String
    let body: String
    let requestId: UUID?
    let roomId: UUID?
    var isRead: Bool
    let createdAt: Date
}

/// Şikayet nedeni; `rawValue` sunucuya gönderilir.
enum ReportReason: String, CaseIterable, Identifiable, Sendable {
    case spam, misleading, inappropriate, harassment, other
    var id: String { rawValue }

    /// İlan şikayetinde sunulan nedenler.
    static let forRequests: [ReportReason] = [.spam, .misleading, .inappropriate, .other]
    /// Kullanıcı (sohbet) şikayetinde sunulan nedenler.
    static let forUsers: [ReportReason] = [.spam, .harassment, .inappropriate, .other]
}

enum ReportTarget: String, Sendable {
    case bloodRequest = "BLOOD_REQUEST", user = "USER", chatMessage = "CHAT_MESSAGE"
}
