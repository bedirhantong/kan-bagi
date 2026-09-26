import Foundation

struct GetHealthFormUseCase: Sendable {
    let forms: HealthFormRepository
    func callAsFunction() async throws -> HealthAnswers? { try await forms.myAnswers() }
}

struct SubmitHealthFormUseCase: Sendable {
    let forms: HealthFormRepository
    func callAsFunction(_ answers: HealthAnswers) async throws {
        guard HealthFormEvaluator.isComplete(answers) else { throw AppError.invalidInput(L10n.string("health.title")) }
        try await forms.save(answers)
    }
}

struct GetNotificationPreferencesUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction() async throws -> NotificationPreferences { try await notifications.preferences() }
}

struct UpdateNotificationPreferencesUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction(_ preferences: NotificationPreferences) async throws {
        guard preferences.scopeCount <= NotificationScope.maxSelections else {
            throw AppError.selectionLimit(NotificationScope.maxSelections)
        }
        try await notifications.updatePreferences(preferences)
    }
}

struct GetNotificationAreasUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction() async throws -> NotificationAreaCatalog { NotificationAreaCatalog(try await notifications.areas()) }
}

/// Yalnızca bildirim bölgelerini değiştirir (diğer tercihler korunur). Kayıt akışında kullanılır.
struct SetNotificationAreasUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction(_ keys: Set<String>) async throws {
        var preferences = try await notifications.preferences()
        guard preferences.preferredAreaKeys != keys else { return }
        preferences.preferredAreaKeys = keys
        try await UpdateNotificationPreferencesUseCase(notifications: notifications)(preferences)
    }
}

/// Konuma göre bölge önerisi: en yakın hastanenin ilçesi (yoksa ili).
/// Hastane yoksa bölge de listede olmaz; bu yüzden öneri her zaman seçilebilir bir bölgedir.
struct SuggestNotificationAreaUseCase: Sendable {
    enum Result: Equatable, Sendable {
        case area(NotificationArea)
        /// Konum izni yok ya da konum alınamadı.
        case locationUnavailable
        /// Yakında (yarıçap içinde) hastane yok.
        case noneNearby
    }

    let location: LocationProvider
    let hospitals: HospitalRepository
    var radiusKm: Double = 50

    func callAsFunction(in catalog: NotificationAreaCatalog) async -> Result {
        guard let coordinate = await location.currentCoordinate() else { return .locationUnavailable }
        let nearby = (try? await hospitals.nearby(latitude: coordinate.latitude, longitude: coordinate.longitude, radiusKm: radiusKm)) ?? []
        // Sonuçlar yakından uzağa sıralıdır; bölgesi listede olan ilk hastane.
        for hospital in nearby {
            guard let cityName = hospital.cityName, let group = catalog.city(named: cityName) else { continue }
            if let district = group.districts.first(where: { $0.district == hospital.districtName }) { return .area(district) }
            return .area(group.city)
        }
        return .noneNearby
    }
}

struct GetNotificationInboxUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction(limit: Int = 50) async throws -> [AppNotification] { try await notifications.inbox(limit: limit) }
}

struct MarkNotificationsReadUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction(id: UUID) async throws { try await notifications.markRead(id: id) }
    func all() async throws { try await notifications.markAllRead() }
}

struct GetUnreadNotificationCountUseCase: Sendable {
    let notifications: NotificationRepository
    func callAsFunction() async throws -> Int { try await notifications.unreadCount() }
}

struct ReportContentUseCase: Sendable {
    let safety: SafetyRepository
    func callAsFunction(target: ReportTarget, targetId: String, reason: String) async throws {
        let reason = reason.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !reason.isEmpty else { throw AppError.invalidInput(L10n.string("safety.reason")) }
        try await safety.report(target: target, targetId: targetId, reason: String(reason.prefix(500)))
    }
}

struct BlockUserUseCase: Sendable {
    let safety: SafetyRepository
    func callAsFunction(userId: UUID) async throws { try await safety.block(userId: userId) }
}
