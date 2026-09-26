import Foundation

/// Framework'ten bağımsız coğrafi koordinat (Domain, CoreLocation'a bağımlı olmasın diye).
struct Coordinate: Equatable, Hashable, Sendable {
    let latitude: Double
    let longitude: Double

    /// İki nokta arasındaki büyük daire mesafesi (haversine), metre.
    func distance(to other: Coordinate) -> Double {
        let earthRadius = 6_371_000.0
        let lat1 = latitude * .pi / 180, lat2 = other.latitude * .pi / 180
        let dLat = lat2 - lat1
        let dLon = (other.longitude - longitude) * .pi / 180
        let a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
