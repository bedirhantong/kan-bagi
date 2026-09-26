import Foundation

struct Hospital: Identifiable, Equatable, Hashable, Sendable {
    let id: Int
    let name: String
    let address: String?
    let phoneNumber: String?
    let email: String?
    let website: URL?
    let latitude: Double?
    let longitude: Double?
    let cityName: String?
    let districtName: String?
    let iconURL: URL?

    var coordinate: Coordinate? {
        guard let latitude, let longitude else { return nil }
        return Coordinate(latitude: latitude, longitude: longitude)
    }

    var locationDescription: String {
        [districtName, cityName].compactMap { $0 }.joined(separator: ", ")
    }
}
