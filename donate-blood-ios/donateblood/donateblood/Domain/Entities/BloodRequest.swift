import Foundation

struct BloodRequest: Identifiable, Equatable, Hashable, Sendable {
    let id: UUID
    let ownerId: UUID
    let ownerName: String?
    var patientFullName: String
    var patientAge: Int?
    var title: String
    var description: String?
    var bloodType: BloodType
    var isActive: Bool
    var isVerified: Bool
    var isEmergency: Bool
    var hospital: Hospital?
    let createdAt: Date

    /// Kullanıcının konumundan ilanın hastanesine mesafe (metre). Konum ya da hastane koordinatı yoksa `nil`.
    func distance(from location: Coordinate?) -> Double? {
        guard let location, let target = hospital?.coordinate else { return nil }
        return location.distance(to: target)
    }
}

/// Yeni ilan / güncelleme girdisi.
/// Kullanıcı başına ilan sınırları (spam ve bildirim yağmuru koruması).
/// Asıl uygulama veritabanındadır (enforce_request_limits); değerler onunla aynı olmalı.
enum BloodRequestLimits {
    static let maxActive = 3
    static let maxPerDay = 5
}

struct BloodRequestDraft: Sendable, Equatable {
    var patientFullName = ""
    var patientAge: Int?
    var title = ""
    var description = ""
    /// Önceden seçili gelmez: yanlış gruba bildirim gitmesin diye kullanıcı bilerek seçer.
    var bloodType: BloodType?
    var hospitalId: Int?
    var phoneNumbers: [String] = [""]
    var isEmergency = false
    /// Yeni ilan için kan bağışı şartlarının kabulü zorunludur.
    var acceptedTerms = false
}

/// Bir hastaneye ait ilanları listelemek için (harita). Ana akış filtre almaz: `feed` kullanılır.
struct BloodRequestFilter: Equatable, Sendable {
    var hospital: Hospital?
    var onlyActive = true
}

struct PageRequest: Equatable, Sendable {
    var offset: Int
    var limit: Int
    static let first = PageRequest(offset: 0, limit: 20)
    func next() -> PageRequest { PageRequest(offset: offset + limit, limit: limit) }
}
