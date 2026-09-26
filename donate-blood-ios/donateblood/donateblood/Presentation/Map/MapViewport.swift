import Foundation

/// Haritanın görünür alanı: merkez + yarıçap (görünür dikdörtgenin yarı köşegeni, km).
/// Sunucudan "bu yarıçaptaki yerler" istenir; yarıçap performans için sınırlıdır.
struct MapViewport: Equatable, Sendable {
    static let minRadiusKm = 3.0
    static let maxRadiusKm = 150.0

    let center: Coordinate
    let radiusKm: Double

    init(center: Coordinate, radiusKm: Double) {
        self.center = center
        self.radiusKm = min(max(radiusKm, Self.minRadiusKm), Self.maxRadiusKm)
    }

    /// Kullanıcı haritayı yeterince oynattıysa "Bu bölgede ara" önerilir (her küçük kaydırmada istek atılmaz):
    /// merkez, yüklenen yarıçapın %35'inden fazla kaydıysa ya da görünür alan %40'tan fazla büyüdüyse.
    func needsSearch(comparedTo loaded: MapViewport?) -> Bool {
        guard let loaded else { return true }
        let moved = center.distance(to: loaded.center) / 1000
        return moved > loaded.radiusKm * 0.35 || radiusKm > loaded.radiusKm * 1.4
    }
}
