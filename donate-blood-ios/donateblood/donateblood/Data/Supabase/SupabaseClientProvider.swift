import Foundation
import Supabase

/// Uygulama genelinde tek bir `SupabaseClient` örneği üretir.
enum SupabaseClientProvider {
    static func make(config: AppConfig) -> SupabaseClient {
        SupabaseClient(
            supabaseURL: config.supabaseURL,
            supabaseKey: config.supabaseAnonKey,
            options: SupabaseClientOptions(
                auth: .init(emitLocalSessionAsInitialSession: true)
            )
        )
    }
}
