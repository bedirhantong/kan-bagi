import UIKit
import MapKit

/// Haritadan çıkan dış eylemler (yol tarifi, arama). Görünümler bunları doğrudan kullanır.
enum MapActions {
    static func openDirections(to coordinate: Coordinate, name: String) {
        let item = MKMapItem(placemark: MKPlacemark(coordinate: coordinate.clCoordinate))
        item.name = name
        item.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving])
    }

    /// "+90 322 454 61 31" -> tel:+903224546131
    static func phoneURL(_ phone: String) -> URL? {
        let digits = phone.filter { $0.isNumber || $0 == "+" }
        return digits.isEmpty ? nil : URL(string: "tel:\(digits)")
    }

    static func openSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
    }
}
