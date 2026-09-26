import CoreLocation

/// Domain `Coordinate` ile MapKit/CoreLocation arasındaki dönüşüm (yalnızca Presentation'da).
extension Coordinate {
    var clCoordinate: CLLocationCoordinate2D { CLLocationCoordinate2D(latitude: latitude, longitude: longitude) }
}

extension CLLocationCoordinate2D {
    var domain: Coordinate { Coordinate(latitude: latitude, longitude: longitude) }
}
