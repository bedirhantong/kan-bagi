import Foundation
import Supabase

final class SupabaseHealthFormRepository: HealthFormRepository {
    private let client: SupabaseClient
    init(client: SupabaseClient) { self.client = client }

    func myAnswers() async throws -> HealthAnswers? {
        do {
            let id = try await client.requireUserId()
            let rows: [HealthFormDTO] = try await client.from("health_forms").select()
                .eq("donor_id", value: id).limit(1).execute().value
            return rows.first?.answers
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func save(_ answers: HealthAnswers) async throws {
        do {
            let id = try await client.requireUserId()
            try await client.from("health_forms")
                .upsert(HealthFormDTO(donorId: id, answers: answers), onConflict: "donor_id").execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}

final class SupabaseNotificationRepository: NotificationRepository {
    private let client: SupabaseClient
    init(client: SupabaseClient) { self.client = client }

    func preferences() async throws -> NotificationPreferences {
        do {
            let id = try await client.requireUserId()
            let rows: [NotificationPreferencesDTO] = try await client.from("notification_preferences").select()
                .eq("user_id", value: id).limit(1).execute().value
            return rows.first?.toDomain() ?? .default
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func updatePreferences(_ preferences: NotificationPreferences) async throws {
        do {
            let id = try await client.requireUserId()
            try await client.from("notification_preferences")
                .update(NotificationPreferencesDTO(preferences)).eq("user_id", value: id).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func areas() async throws -> [NotificationArea] {
        do {
            let dtos: [NotificationAreaDTO] = try await client.rpc("notification_areas").execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func inbox(limit: Int) async throws -> [AppNotification] {
        do {
            let dtos: [AppNotificationDTO] = try await client.from("notifications").select()
                .order("created_at", ascending: false).limit(limit).execute().value
            return dtos.compactMap { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func markRead(id: UUID) async throws {
        do { try await client.from("notifications").update(["is_read": true]).eq("id", value: id).execute() }
        catch { throw SupabaseErrorMapper.map(error) }
    }

    func markAllRead() async throws {
        do { try await client.from("notifications").update(["is_read": true]).eq("is_read", value: false).execute() }
        catch { throw SupabaseErrorMapper.map(error) }
    }

    func unreadCount() async throws -> Int {
        do {
            return try await client.from("notifications").select("id", head: true, count: .exact)
                .eq("is_read", value: false).execute().count ?? 0
        } catch { throw SupabaseErrorMapper.map(error) }
    }

}

final class SupabaseStoryRepository: StoryRepository {
    private let client: SupabaseClient
    init(client: SupabaseClient) { self.client = client }

    func stories() async throws -> [Story] {
        do {
            let dtos: [StoryDTO] = try await client.from("stories").select()
                .order("sort_order").order("created_at", ascending: false).execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}

final class SupabaseSafetyRepository: SafetyRepository {
    private let client: SupabaseClient
    init(client: SupabaseClient) { self.client = client }

    func report(target: ReportTarget, targetId: String, reason: String) async throws {
        do {
            try await client.from("reports")
                .insert(ReportDTO(targetType: target.rawValue, targetId: targetId, reason: reason)).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func block(userId: UUID) async throws {
        do { try await client.from("blocks").insert(BlockDTO(blockedId: userId)).execute() }
        catch let error as PostgrestError where error.code == "23505" { return }
        catch { throw SupabaseErrorMapper.map(error) }
    }

    func unblock(userId: UUID) async throws {
        do {
            let me = try await client.requireUserId()
            try await client.from("blocks").delete().eq("blocker_id", value: me).eq("blocked_id", value: userId).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }
}
