import Foundation
@testable import donateblood

enum TestData {
    static let hospital = Hospital(
        id: 1, name: "Test Hastanesi", address: "Test Mah. 1", phoneNumber: nil, email: nil, website: nil,
        latitude: 36.9, longitude: 30.7, cityName: "Antalya", districtName: "Muratpaşa", iconURL: nil)

    /// Bildirim bölgeleri (sunucudaki notification_areas() biçiminde, düz liste).
    static let areas: [NotificationArea] = [
        NotificationArea(key: "İstanbul", city: "İstanbul", district: nil, hospitalCount: 2),
        NotificationArea(key: "İstanbul/Pendik", city: "İstanbul", district: "Pendik", hospitalCount: 1),
        NotificationArea(key: "İstanbul/Fatih", city: "İstanbul", district: "Fatih", hospitalCount: 1),
        NotificationArea(key: "Antalya", city: "Antalya", district: nil, hospitalCount: 3),
        NotificationArea(key: "Antalya/Muratpaşa", city: "Antalya", district: "Muratpaşa", hospitalCount: 2),
        NotificationArea(key: "Antalya/Konyaaltı", city: "Antalya", district: "Konyaaltı", hospitalCount: 1),
        NotificationArea(key: "Ankara", city: "Ankara", district: nil, hospitalCount: 1),
        NotificationArea(key: "Ankara/Çankaya", city: "Ankara", district: "Çankaya", hospitalCount: 1),
    ]

    static func request(
        id: UUID = UUID(), ownerId: UUID = UUID(), title: String = "Acil kan", bloodType: BloodType = .aPositive,
        isActive: Bool = true, createdAt: Date = .now
    ) -> BloodRequest {
        BloodRequest(
            id: id, ownerId: ownerId, ownerName: "Test K.", patientFullName: "Hasta Adı", patientAge: 30, title: title,
            description: nil, bloodType: bloodType, isActive: isActive, isVerified: false, isEmergency: false,
            hospital: hospital, createdAt: createdAt)
    }

    static func requests(count: Int, startingAt offset: Int = 0) -> [BloodRequest] {
        (0..<count).map { request(title: "İlan \(offset + $0)", createdAt: Date(timeIntervalSince1970: Double(1_800_000_000 - offset - $0))) }
    }

    static func message(roomId: UUID = UUID(), senderId: UUID = UUID(), content: String = "merhaba", at seconds: TimeInterval = 0) -> ChatMessage {
        ChatMessage(id: UUID(), roomId: roomId, senderId: senderId, content: content, createdAt: Date(timeIntervalSince1970: 1_800_000_000 + seconds))
    }

    static func profile(id: UUID = UUID(), completed: Bool = true, type: UserType = .regularUser) -> Profile {
        Profile(id: id, name: "Ali", surname: "Veli", email: nil, phoneNumber: nil, birthDate: nil, bloodType: .oPositive,
                gender: .male, userType: type, isProfileCompleted: completed, createdAt: nil)
    }

    static func bloodPoint(id: Int = 1, kind: BloodDonationPoint.Kind = .fixed) -> BloodDonationPoint {
        BloodDonationPoint(
            id: id, name: "Kızılay Kan Alma Birimi \(id)", address: "Test Sok. No:1", neighborhood: "Kışla",
            district: "Yüreğir", provinceName: "Adana", phoneNumber: "+90 322 000 00 00",
            coordinate: Coordinate(latitude: 37.0, longitude: 35.3), kind: kind)
    }
}
