import Foundation
import Supabase

final class SupabaseBloodDonationPointRepository: BloodDonationPointRepository {
    private let client: SupabaseClient

    init(client: SupabaseClient) { self.client = client }

    func nearby(latitude: Double, longitude: Double, radiusKm: Double) async throws -> [BloodDonationPoint] {
        do {
            let dtos: [BloodDonationPointDTO] = try await client.rpc(
                "blood_donation_points_nearby",
                params: ["user_lat": latitude, "user_lon": longitude, "max_km": radiusKm]
            ).execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}

final class SupabaseHospitalRepository: HospitalRepository {
    private let client: SupabaseClient

    init(client: SupabaseClient) { self.client = client }

    func hospitals(search: String?) async throws -> [Hospital] {
        do {
            var query = client.from("hospitals").select()
            let term = (search ?? "").components(separatedBy: CharacterSet(charactersIn: ",()*%\\\"")).joined()
                .trimmingCharacters(in: .whitespacesAndNewlines)
            if !term.isEmpty {
                query = query.or("name.ilike.*\(term)*,city_name.ilike.*\(term)*,district_name.ilike.*\(term)*")
            }
            let dtos: [HospitalDTO] = try await query.order("name").limit(100).execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func hospitals(ids: [Int]) async throws -> [Hospital] {
        do {
            let dtos: [HospitalDTO] = try await client.from("hospitals").select().in("id", values: ids).order("name").execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func nearby(latitude: Double, longitude: Double, radiusKm: Double) async throws -> [Hospital] {
        do {
            let dtos: [HospitalDTO] = try await client.rpc(
                "hospitals_nearby",
                params: ["user_lat": latitude, "user_lon": longitude, "max_km": radiusKm]
            ).execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}
