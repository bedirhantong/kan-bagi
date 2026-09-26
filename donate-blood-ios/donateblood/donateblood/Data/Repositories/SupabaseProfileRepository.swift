import Foundation
import Supabase

final class SupabaseProfileRepository: ProfileRepository {
    private let client: SupabaseClient

    init(client: SupabaseClient) { self.client = client }

    func myProfile() async throws -> Profile {
        do {
            let id = try await client.requireUserId()
            let dto: ProfileDTO = try await client.from("profiles").select().eq("id", value: id).single().execute().value
            return dto.toDomain()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func updateMyProfile(_ update: ProfileUpdate, markCompleted: Bool) async throws -> Profile {
        do {
            let id = try await client.requireUserId()
            let payload = ProfileUpdateDTO(
                name: update.name.trimmingCharacters(in: .whitespacesAndNewlines),
                surname: update.surname.trimmingCharacters(in: .whitespacesAndNewlines),
                phoneNumber: update.phoneNumber.flatMap { $0.isEmpty ? nil : $0 },
                birthDate: update.birthDate.map(DateCoding.dayFormatter.string(from:)),
                bloodType: update.bloodType?.rawValue,
                gender: update.gender?.rawValue,
                isProfileCompleted: markCompleted ? true : nil
            )
            let dto: ProfileDTO = try await client.from("profiles").update(payload).eq("id", value: id)
                .select().single().execute().value
            return dto.toDomain()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func prefillNameIfEmpty(_ name: PersonName) async throws {
        do {
            let id = try await client.requireUserId()
            let current: ProfileDTO = try await client.from("profiles").select().eq("id", value: id).single().execute().value
            guard (current.name ?? "").isEmpty else { return }
            try await client.from("profiles")
                .update(NamePrefillDTO(name: name.given, surname: name.family))
                .eq("id", value: id).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}
