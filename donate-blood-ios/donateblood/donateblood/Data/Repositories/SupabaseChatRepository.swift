import Foundation
import Supabase

final class SupabaseChatRepository: ChatRepository {
    private let client: SupabaseClient

    init(client: SupabaseClient) { self.client = client }

    func rooms() async throws -> [ChatRoom] {
        do {
            let dtos: [ChatRoomDTO] = try await client.rpc("get_chat_rooms").execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func room(with userId: UUID) async throws -> UUID {
        do {
            return try await client.rpc("get_or_create_chat_room", params: ["other_user": userId]).execute().value
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func messages(roomId: UUID, before: Date?, limit: Int) async throws -> [ChatMessage] {
        do {
            var query = client.from("chat_messages").select().eq("room_id", value: roomId)
            if let before {
                // created_at mikrosaniye hassasiyetinde; ms'ye yuvarlamayla sınırdaki mesaj kaybolmasın diye
                // 1 ms pay bırakılır, çağıran taraf id ile tekilleştirir.
                query = query.lt("created_at", value: DateCoding.iso8601String(before.addingTimeInterval(0.001)))
            }
            let dtos: [ChatMessageDTO] = try await query
                .order("created_at", ascending: false).limit(limit).execute().value
            return dtos.map { $0.toDomain() }
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func send(roomId: UUID, content: String) async throws {
        do {
            try await client.from("chat_messages").insert(NewMessageDTO(roomId: roomId, content: content)).execute()
        } catch { throw SupabaseErrorMapper.map(error) }
    }

    func incomingMessages(roomId: UUID) -> AsyncStream<ChatMessage> {
        let channel = client.realtimeV2.channel("chat-room-\(roomId.uuidString)")
        let inserts = channel.postgresChange(
            InsertAction.self, schema: "public", table: "chat_messages",
            filter: .eq("room_id", value: roomId.uuidString)
        )
        return AsyncStream { continuation in
            let task = Task {
                do {
                    try await channel.subscribeWithError()
                } catch {
                    Log.realtime.error("Kanal aboneliği başarısız: \(String(describing: error))")
                    continuation.finish()
                    return
                }
                for await insert in inserts {
                    do {
                        let dto: ChatMessageDTO = try insert.decodeRecord(decoder: JSONDecoder.supabaseRealtime)
                        continuation.yield(dto.toDomain())
                    } catch {
                        Log.realtime.error("Mesaj çözülemedi: \(String(describing: error))")
                    }
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in
                task.cancel()
                Task { await channel.unsubscribe() }
            }
        }
    }
}

extension JSONDecoder {
    /// Realtime kayıtlarındaki Postgres zaman damgalarını çözer.
    static let supabaseRealtime: JSONDecoder = {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .custom { decoder in
            let string = try decoder.singleValueContainer().decode(String.self)
            let withFraction = ISO8601DateFormatter()
            withFraction.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
            if let date = withFraction.date(from: string) { return date }
            let plain = ISO8601DateFormatter()
            if let date = plain.date(from: string) { return date }
            throw DecodingError.dataCorrupted(.init(codingPath: decoder.codingPath, debugDescription: "Geçersiz tarih: \(string)"))
        }
        return decoder
    }()
}
