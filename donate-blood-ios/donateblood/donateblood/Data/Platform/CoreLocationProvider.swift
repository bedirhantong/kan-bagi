import Foundation
import CoreLocation

/// `LocationProvider`'ın CoreLocation implementasyonu.
/// İzin sorulmadıysa ilk çağrıda ister; reddedildiyse veya süre dolarsa `nil` döner (akış konumsuz devam eder).
/// Son bilinen konum kısa süre önbelleğe alınır, böylece her yenilemede GPS beklenmez.
@MainActor
final class CoreLocationProvider: NSObject, LocationProvider, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    private let timeout: Duration
    private let cacheLifetime: TimeInterval

    private var cached: (coordinate: Coordinate, date: Date)?
    private var authorizationWaiters: [CheckedContinuation<Void, Never>] = []
    private var locationWaiters: [CheckedContinuation<Coordinate?, Never>] = []

    init(timeout: Duration = .seconds(4), cacheLifetime: TimeInterval = 300) {
        self.timeout = timeout
        self.cacheLifetime = cacheLifetime
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    func currentCoordinate() async -> Coordinate? {
        if let cached, Date().timeIntervalSince(cached.date) < cacheLifetime { return cached.coordinate }

        if manager.authorizationStatus == .notDetermined {
            await requestAuthorization()
        }
        guard manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways else {
            return cached?.coordinate
        }
        let fresh = await requestLocation()
        return fresh ?? cached?.coordinate
    }

    func authorization() async -> LocationAuthorization {
        switch manager.authorizationStatus {
        case .notDetermined: return .notDetermined
        case .authorizedAlways, .authorizedWhenInUse: return .authorized
        default: return .denied
        }
    }

    // MARK: Waiting helpers

    private func requestAuthorization() async {
        await withCheckedContinuation { continuation in
            authorizationWaiters.append(continuation)
            manager.requestWhenInUseAuthorization()
        }
    }

    private func requestLocation() async -> Coordinate? {
        let timeoutTask = Task { [weak self, timeout] in
            try? await Task.sleep(for: timeout)
            self?.finishLocationRequests(with: nil)
        }
        let result: Coordinate? = await withCheckedContinuation { continuation in
            locationWaiters.append(continuation)
            manager.requestLocation()
        }
        timeoutTask.cancel()
        return result
    }

    private func finishLocationRequests(with coordinate: Coordinate?) {
        let waiters = locationWaiters
        locationWaiters.removeAll()
        if let coordinate { cached = (coordinate, Date()) }
        waiters.forEach { $0.resume(returning: coordinate) }
    }

    // MARK: CLLocationManagerDelegate

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        Task { @MainActor in
            guard self.manager.authorizationStatus != .notDetermined else { return }
            let waiters = self.authorizationWaiters
            self.authorizationWaiters.removeAll()
            waiters.forEach { $0.resume() }
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let last = locations.last else { return }
        let coordinate = Coordinate(latitude: last.coordinate.latitude, longitude: last.coordinate.longitude)
        Task { @MainActor in self.finishLocationRequests(with: coordinate) }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        Task { @MainActor in self.finishLocationRequests(with: nil) }
    }
}
