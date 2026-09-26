import Network
import Combine

/// Bağlantı durumunu yayınlar. Arayüz çevrimdışı banner'ı için kullanır.
@MainActor
final class NetworkMonitor: ObservableObject {
    @Published private(set) var isConnected = true

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "donateblood.network-monitor")

    init() {
        monitor.pathUpdateHandler = { [weak self] path in
            let connected = path.status == .satisfied
            Task { @MainActor in self?.isConnected = connected }
        }
        monitor.start(queue: queue)
    }

    deinit { monitor.cancel() }
}
