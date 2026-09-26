import Foundation

// MARK: - Tarih yardımcıları

enum DateCoding {
    /// "yyyy-MM-dd" (Postgres `date`).
    static let dayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    static func iso8601String(_ date: Date) -> String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter.string(from: date)
    }
}

// MARK: - Profile

struct ProfileDTO: Decodable {
    let id: UUID
    let name: String?
    let surname: String?
    let email: String?
    let phoneNumber: String?
    let birthDate: String?
    let bloodType: String?
    let gender: String?
    let userType: String
    let isProfileCompleted: Bool
    let createdAt: Date?

    enum CodingKeys: String, CodingKey {
        case id, name, surname, email, gender
        case createdAt = "created_at"
        case phoneNumber = "phone_number"
        case birthDate = "birth_date"
        case bloodType = "blood_type"
        case userType = "user_type"
        case isProfileCompleted = "is_profile_completed"
    }

    func toDomain() -> Profile {
        Profile(
            id: id, name: name, surname: surname, email: email, phoneNumber: phoneNumber,
            birthDate: birthDate.flatMap(DateCoding.dayFormatter.date(from:)),
            bloodType: bloodType.flatMap(BloodType.init(rawValue:)),
            gender: gender.flatMap(Gender.init(rawValue:)),
            userType: UserType(rawValue: userType) ?? .regularUser,
            isProfileCompleted: isProfileCompleted, createdAt: createdAt
        )
    }
}

/// Opsiyonel alanlar da açıkça `null` olarak yazılır (alan temizlenebilsin diye).
struct ProfileUpdateDTO: Encodable {
    let name: String
    let surname: String
    let phoneNumber: String?
    let birthDate: String?
    let bloodType: String?
    let gender: String?
    let isProfileCompleted: Bool?

    enum CodingKeys: String, CodingKey {
        case name, surname, gender
        case phoneNumber = "phone_number"
        case birthDate = "birth_date"
        case bloodType = "blood_type"
        case isProfileCompleted = "is_profile_completed"
    }

    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(name, forKey: .name)
        try c.encode(surname, forKey: .surname)
        try c.encode(phoneNumber, forKey: .phoneNumber)
        try c.encode(birthDate, forKey: .birthDate)
        try c.encode(bloodType, forKey: .bloodType)
        try c.encode(gender, forKey: .gender)
        try c.encodeIfPresent(isProfileCompleted, forKey: .isProfileCompleted)
    }
}

struct NamePrefillDTO: Encodable {
    let name: String?
    let surname: String?
}

// MARK: - Hospital

struct HospitalDTO: Decodable {
    let id: Int
    let name: String
    let address: String?
    let phoneNumber: String?
    let email: String?
    let website: String?
    let lat: Double?
    let lon: Double?
    let cityName: String?
    let districtName: String?
    let hospitalIcon: String?

    enum CodingKeys: String, CodingKey {
        case id, name, address, email, website, lat, lon
        case phoneNumber = "phone_number"
        case cityName = "city_name"
        case districtName = "district_name"
        case hospitalIcon = "hospital_icon"
    }

    func toDomain() -> Hospital {
        Hospital(
            id: id, name: name, address: address, phoneNumber: phoneNumber, email: email,
            website: website.flatMap { URL(string: $0) },
            latitude: lat, longitude: lon, cityName: cityName, districtName: districtName,
            iconURL: hospitalIcon.flatMap { URL(string: $0) }
        )
    }
}

// MARK: - Blood donation point

struct BloodDonationPointDTO: Decodable {
    let id: Int
    let name: String
    let address: String?
    let neighborhood: String?
    let district: String?
    let provinceName: String?
    let phoneNumber: String?
    let lat: Double
    let lon: Double
    let kind: String

    enum CodingKeys: String, CodingKey {
        case id, name, address, neighborhood, district, lat, lon, kind
        case provinceName = "province_name"
        case phoneNumber = "phone_number"
    }

    func toDomain() -> BloodDonationPoint {
        BloodDonationPoint(
            id: id, name: name, address: address, neighborhood: neighborhood, district: district,
            provinceName: provinceName, phoneNumber: phoneNumber,
            coordinate: Coordinate(latitude: lat, longitude: lon),
            kind: BloodDonationPoint.Kind(rawValue: kind) ?? .fixed)
    }
}

// MARK: - Blood request

struct BloodRequestDTO: Decodable {
    let id: UUID
    let ownerId: UUID
    let ownerName: String?
    let patientFullName: String
    let patientAge: Int?
    let title: String
    let description: String?
    let bloodType: String
    let isActive: Bool
    let isVerified: Bool
    let isEmergency: Bool
    let createdAt: Date
    let hospital: HospitalDTO?

    enum CodingKeys: String, CodingKey {
        case id, title, description, hospital
        case ownerId = "owner_id"
        case ownerName = "owner_name"
        case patientFullName = "patient_full_name"
        case patientAge = "patient_age"
        case bloodType = "blood_type"
        case isActive = "is_active"
        case isVerified = "is_verified"
        case isEmergency = "is_emergency"
        case createdAt = "created_at"
    }

    func toDomain() -> BloodRequest? {
        guard let type = BloodType(rawValue: bloodType) else { return nil }
        return BloodRequest(
            id: id, ownerId: ownerId, ownerName: ownerName, patientFullName: patientFullName,
            patientAge: patientAge, title: title, description: description, bloodType: type,
            isActive: isActive, isVerified: isVerified, isEmergency: isEmergency, hospital: hospital?.toDomain(), createdAt: createdAt
        )
    }
}

struct SaveRequestParams: Encodable {
    let pId: UUID?
    let pPatientFullName: String
    let pPatientAge: Int?
    let pTitle: String
    let pDescription: String?
    /// Doğrulanmış taslakta her zaman doludur (SaveBloodRequestUseCase).
    let pBloodType: String?
    let pHospitalId: Int?
    let pPhoneNumbers: [String]
    let pIsEmergency: Bool

    enum CodingKeys: String, CodingKey {
        case pId = "p_id"
        case pPatientFullName = "p_patient_full_name"
        case pPatientAge = "p_patient_age"
        case pTitle = "p_title"
        case pDescription = "p_description"
        case pBloodType = "p_blood_type"
        case pHospitalId = "p_hospital_id"
        case pPhoneNumbers = "p_phone_numbers"
        case pIsEmergency = "p_is_emergency"
    }

    init(id: UUID?, draft: BloodRequestDraft) {
        pId = id
        pPatientFullName = draft.patientFullName
        pPatientAge = draft.patientAge
        pTitle = draft.title
        pDescription = draft.description.isEmpty ? nil : draft.description
        pBloodType = draft.bloodType?.rawValue
        pHospitalId = draft.hospitalId
        pPhoneNumbers = draft.phoneNumbers
        pIsEmergency = draft.isEmergency
    }

    /// `create` RPC'sinde `p_id` parametresi yoktur; nil değerler null olarak gönderilir.
    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        if let pId { try c.encode(pId, forKey: .pId) }
        try c.encode(pPatientFullName, forKey: .pPatientFullName)
        try c.encode(pPatientAge, forKey: .pPatientAge)
        try c.encode(pTitle, forKey: .pTitle)
        try c.encode(pDescription, forKey: .pDescription)
        try c.encode(pBloodType, forKey: .pBloodType)
        try c.encode(pHospitalId, forKey: .pHospitalId)
        try c.encode(pPhoneNumbers, forKey: .pPhoneNumbers)
        try c.encode(pIsEmergency, forKey: .pIsEmergency)
    }
}

/// `blood_request_feed` RPC parametreleri. Konum yoksa alanlar açıkça `null` gönderilir (sunucu tarihe göre sıralar).
struct FeedParams: Encodable {
    let userLat: Double?
    let userLon: Double?
    let pLimit: Int
    let pOffset: Int

    enum CodingKeys: String, CodingKey {
        case userLat = "user_lat", userLon = "user_lon", pLimit = "p_limit", pOffset = "p_offset"
    }

    func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(userLat, forKey: .userLat)
        try c.encode(userLon, forKey: .userLon)
        try c.encode(pLimit, forKey: .pLimit)
        try c.encode(pOffset, forKey: .pOffset)
    }
}

struct ContactsDTO: Decodable {
    let phoneNumbers: [String]
    enum CodingKeys: String, CodingKey { case phoneNumbers = "phone_numbers" }
}

struct IdOnlyDTO: Decodable { let id: UUID }

// MARK: - Chat

struct ChatRoomDTO: Decodable {
    let roomId: UUID
    let otherUserId: UUID
    let otherUserName: String?
    let lastMessage: String?
    let lastMessageAt: Date

    enum CodingKeys: String, CodingKey {
        case roomId = "room_id"
        case otherUserId = "other_user_id"
        case otherUserName = "other_user_name"
        case lastMessage = "last_message"
        case lastMessageAt = "last_message_at"
    }

    func toDomain() -> ChatRoom {
        ChatRoom(
            id: roomId, otherUserId: otherUserId,
            otherUserName: otherUserName ?? L10n.string("chat.unknownUser"),
            lastMessage: lastMessage, lastMessageAt: lastMessageAt
        )
    }
}

struct ChatMessageDTO: Decodable {
    let id: UUID
    let roomId: UUID
    let senderId: UUID
    let content: String
    let createdAt: Date

    enum CodingKeys: String, CodingKey {
        case id, content
        case roomId = "room_id"
        case senderId = "sender_id"
        case createdAt = "created_at"
    }

    func toDomain() -> ChatMessage {
        ChatMessage(id: id, roomId: roomId, senderId: senderId, content: content, createdAt: createdAt)
    }
}

struct NewMessageDTO: Encodable {
    let roomId: UUID
    let content: String
    enum CodingKeys: String, CodingKey { case roomId = "room_id", content }
}

// MARK: - Health form

struct HealthFormDTO: Codable {
    let donorId: UUID?
    let answers: [String: Bool]
    enum CodingKeys: String, CodingKey { case donorId = "donor_id", answers }
}

// MARK: - Notifications

struct NotificationPreferencesDTO: Codable {
    let pushEnabled: Bool
    let bloodTypeAlerts: Bool
    let chatNotifications: Bool
    let preferredBloodTypes: [String]
    let preferredAreas: [String]
    let preferredHospitalIds: [Int]

    enum CodingKeys: String, CodingKey {
        case pushEnabled = "push_enabled"
        case bloodTypeAlerts = "blood_type_alerts"
        case chatNotifications = "chat_notifications"
        case preferredBloodTypes = "preferred_blood_types"
        case preferredAreas = "preferred_areas"
        case preferredHospitalIds = "preferred_hospital_ids"
    }

    /// `preferred_areas` 12_notification_areas_patch.sql ile gelir; yama çalıştırılmamış veritabanında da okunabilsin.
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        pushEnabled = try container.decode(Bool.self, forKey: .pushEnabled)
        bloodTypeAlerts = try container.decode(Bool.self, forKey: .bloodTypeAlerts)
        chatNotifications = try container.decode(Bool.self, forKey: .chatNotifications)
        preferredBloodTypes = try container.decodeIfPresent([String].self, forKey: .preferredBloodTypes) ?? []
        preferredAreas = try container.decodeIfPresent([String].self, forKey: .preferredAreas) ?? []
        preferredHospitalIds = try container.decodeIfPresent([Int].self, forKey: .preferredHospitalIds) ?? []
    }

    init(_ domain: NotificationPreferences) {
        pushEnabled = domain.pushEnabled
        bloodTypeAlerts = domain.bloodTypeAlerts
        chatNotifications = domain.chatNotifications
        preferredBloodTypes = domain.preferredBloodTypes.map(\.rawValue).sorted()
        preferredAreas = domain.preferredAreaKeys.sorted()
        preferredHospitalIds = domain.preferredHospitalIds.sorted()
    }

    func toDomain() -> NotificationPreferences {
        NotificationPreferences(
            pushEnabled: pushEnabled, bloodTypeAlerts: bloodTypeAlerts, chatNotifications: chatNotifications,
            preferredBloodTypes: Set(preferredBloodTypes.compactMap(BloodType.init(rawValue:))),
            preferredAreaKeys: Set(preferredAreas),
            preferredHospitalIds: Set(preferredHospitalIds)
        )
    }
}

struct NotificationAreaDTO: Decodable {
    let areaKey: String
    let city: String
    let district: String?
    let hospitalCount: Int

    enum CodingKeys: String, CodingKey {
        case areaKey = "area_key", city, district, hospitalCount = "hospital_count"
    }

    func toDomain() -> NotificationArea {
        NotificationArea(key: areaKey, city: city, district: district, hospitalCount: hospitalCount)
    }
}

struct PledgeRowDTO: Decodable {
    let request: BloodRequestDTO?
}

struct AppNotificationDTO: Decodable {
    struct Payload: Decodable {
        let requestId: UUID?
        let roomId: UUID?
        enum CodingKeys: String, CodingKey { case requestId = "request_id", roomId = "room_id" }
    }

    let id: UUID
    let type: String
    let title: String
    let body: String
    let data: Payload?
    let isRead: Bool
    let createdAt: Date

    enum CodingKeys: String, CodingKey {
        case id, type, title, body, data
        case isRead = "is_read"
        case createdAt = "created_at"
    }

    func toDomain() -> AppNotification? {
        guard let kind = AppNotification.Kind(rawValue: type) else { return nil }
        return AppNotification(
            id: id, kind: kind, title: title, body: body,
            requestId: data?.requestId, roomId: data?.roomId, isRead: isRead, createdAt: createdAt
        )
    }
}

// MARK: - Story

struct StoryDTO: Decodable {
    let id: UUID
    let title: String
    let body: String?
    let imageUrl: String?
    let logoUrl: String?
    let linkUrl: String?

    enum CodingKeys: String, CodingKey {
        case id, title, body
        case imageUrl = "image_url"
        case logoUrl = "logo_url"
        case linkUrl = "link_url"
    }

    func toDomain() -> Story {
        Story(id: id, title: title, body: body, imageURL: imageUrl.flatMap { URL(string: $0) },
              logoURL: logoUrl.flatMap { URL(string: $0) }, linkURL: linkUrl.flatMap { URL(string: $0) })
    }
}

// MARK: - Safety

struct ReportDTO: Encodable {
    let targetType: String
    let targetId: String
    let reason: String
    enum CodingKeys: String, CodingKey { case targetType = "target_type", targetId = "target_id", reason }
}

struct BlockDTO: Encodable {
    let blockedId: UUID
    enum CodingKeys: String, CodingKey { case blockedId = "blocked_id" }
}
