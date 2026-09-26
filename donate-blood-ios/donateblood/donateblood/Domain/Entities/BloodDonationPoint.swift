import Foundation

/// Kızılay kan bağış noktası: sabit kan alma birimi ya da gezici ekip. Hastane değildir; haritada ayrı bir katmandır.
struct BloodDonationPoint: Identifiable, Equatable, Hashable, Sendable {
    enum Kind: String, Sendable {
        case fixed = "FIXED"
        case mobile = "MOBILE"
    }

    let id: Int
    let name: String
    let address: String?
    let neighborhood: String?
    let district: String?
    let provinceName: String?
    let phoneNumber: String?
    let coordinate: Coordinate
    let kind: Kind

    /// "Yüreğir, Adana" gibi ilçe + il metni.
    var areaDescription: String {
        [district, provinceName].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: ", ")
    }
}
