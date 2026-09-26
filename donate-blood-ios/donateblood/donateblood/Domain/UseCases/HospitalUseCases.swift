import Foundation

struct GetHospitalsUseCase: Sendable {
    let hospitals: HospitalRepository
    func callAsFunction(search: String? = nil) async throws -> [Hospital] { try await hospitals.hospitals(search: search) }
}

/// Kullanıcının konumu ve izin durumu (harita ve akış için).
struct GetCurrentLocationUseCase: Sendable {
    let location: LocationProvider
    func callAsFunction() async -> Coordinate? { await location.currentCoordinate() }
    func authorization() async -> LocationAuthorization { await location.authorization() }
}

struct GetNearbyBloodPointsUseCase: Sendable {
    let points: BloodDonationPointRepository
    func callAsFunction(latitude: Double, longitude: Double, radiusKm: Double = 60) async throws -> [BloodDonationPoint] {
        try await points.nearby(latitude: latitude, longitude: longitude, radiusKm: radiusKm)
    }
}

struct GetHospitalsByIdsUseCase: Sendable {
    let hospitals: HospitalRepository
    func callAsFunction(ids: [Int]) async throws -> [Hospital] {
        guard !ids.isEmpty else { return [] }
        return try await hospitals.hospitals(ids: ids)
    }
}

struct GetStoriesUseCase: Sendable {
    let stories: StoryRepository
    func callAsFunction() async throws -> [Story] { try await stories.stories() }
}

struct GetNearbyHospitalsUseCase: Sendable {
    let hospitals: HospitalRepository
    func callAsFunction(latitude: Double, longitude: Double, radiusKm: Double = 50) async throws -> [Hospital] {
        try await hospitals.nearby(latitude: latitude, longitude: longitude, radiusKm: radiusKm)
    }
}
