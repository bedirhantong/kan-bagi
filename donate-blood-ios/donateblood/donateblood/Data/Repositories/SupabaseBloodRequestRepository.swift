import Foundation
import Supabase

final class SupabaseBloodRequestRepository: BloodRequestRepository {
    private static let selectClause = "*, hospital:hospitals(*)"
    private let client: SupabaseClient

    init(client: SupabaseClient) { self.client = client }

    func feed(near location: Coordinate?, page: PageRequest) async throws -> [BloodRequest] {
        do {
            let params = FeedParams(userLat: location?.latitude, userLon: location?.longitude,
                                    pLimit: page.limit, pOffset: page.offset)
            let dtos: [BloodRequestDTO] = try await client.rpc("blood_request_feed", params: params)
                .select(Self.selectClause)
                .execute().value
            return dtos.compactMap { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func requests(filter: BloodRequestFilter, page: PageRequest) async throws -> [BloodRequest] {
        do {
            var query = client.from("blood_requests").select(Self.selectClause)
            if filter.onlyActive { query = query.eq("is_active", value: true) }
            if let hospital = filter.hospital { query = query.eq("hospital_id", value: hospital.id) }
            let dtos: [BloodRequestDTO] = try await query
                .order("created_at", ascending: false)
                .range(from: page.offset, to: page.offset + page.limit - 1)
                .execute().value
            return dtos.compactMap { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func myRequests() async throws -> [BloodRequest] {
        do {
            let id = try await client.requireUserId()
            let dtos: [BloodRequestDTO] = try await client.from("blood_requests").select(Self.selectClause)
                .eq("owner_id", value: id)
                .order("created_at", ascending: false)
                .execute().value
            return dtos.compactMap { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func request(id: UUID) async throws -> BloodRequest {
        do {
            let dto: BloodRequestDTO = try await client.from("blood_requests").select(Self.selectClause)
                .eq("id", value: id).single().execute().value
            guard let request = dto.toDomain() else { throw AppError.notFound }
            return request
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func contactNumbers(requestId: UUID) async throws -> [String] {
        do {
            let rows: [ContactsDTO] = try await client.from("blood_request_contacts").select()
                .eq("request_id", value: requestId).limit(1).execute().value
            return rows.first?.phoneNumbers ?? []
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func create(_ draft: BloodRequestDraft) async throws -> UUID {
        do {
            return try await client.rpc("create_blood_request", params: SaveRequestParams(id: nil, draft: draft))
                .execute().value
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func update(id: UUID, draft: BloodRequestDraft) async throws {
        do {
            try await client.rpc("update_blood_request", params: SaveRequestParams(id: id, draft: draft)).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func setActive(id: UUID, isActive: Bool) async throws {
        do {
            try await client.from("blood_requests").update(["is_active": isActive]).eq("id", value: id).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func delete(id: UUID) async throws {
        do { try await client.from("blood_requests").delete().eq("id", value: id).execute() }
        catch { throw SupabaseErrorMapper.map(error) }
    }

    func pledgeCount(requestId: UUID) async throws -> Int {
        do {
            return try await client.rpc("pledge_count", params: ["p_request_id": requestId]).execute().value
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func hasPledged(requestId: UUID) async throws -> Bool {
        do {
            let me = try await client.requireUserId()
            let rows: [IdOnlyDTO] = try await client.from("donation_pledges").select("id")
                .eq("request_id", value: requestId).eq("donor_id", value: me).limit(1).execute().value
            return !rows.isEmpty
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func setPledge(requestId: UUID, pledged: Bool) async throws {
        do {
            if pledged {
                do {
                    try await client.from("donation_pledges").insert(["request_id": requestId]).execute()
                } catch let error as PostgrestError where error.code == "23505" {
                    return // zaten kayıtlı
                }
            } else {
                let me = try await client.requireUserId()
                try await client.from("donation_pledges").delete()
                    .eq("request_id", value: requestId).eq("donor_id", value: me).execute()
            }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func pledgedRequests() async throws -> [BloodRequest] {
        do {
            let me = try await client.requireUserId()
            let rows: [PledgeRowDTO] = try await client.from("donation_pledges")
                .select("request:blood_requests(*, hospital:hospitals(*))")
                .eq("donor_id", value: me)
                .order("created_at", ascending: false)
                .execute().value
            return rows.compactMap { $0.request?.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}
