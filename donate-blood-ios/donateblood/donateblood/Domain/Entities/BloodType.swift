import Foundation

enum BloodType: String, CaseIterable, Identifiable, Sendable, Hashable {
    case aPositive = "A+", aNegative = "A-"
    case bPositive = "B+", bNegative = "B-"
    case abPositive = "AB+", abNegative = "AB-"
    case oPositive = "0+", oNegative = "0-"

    var id: String { rawValue }
    var displayName: String { rawValue }

    private enum ABO { case a, b, ab, o }

    private var abo: ABO {
        switch self {
        case .aPositive, .aNegative: return .a
        case .bPositive, .bNegative: return .b
        case .abPositive, .abNegative: return .ab
        case .oPositive, .oNegative: return .o
        }
    }

    private var isRhPositive: Bool { rawValue.hasSuffix("+") }

    /// Bu kan grubundaki bir bağışçı, `recipient` kan grubundaki hastaya kırmızı hücre bağışlayabilir mi (ABO + Rh uyumu)?
    /// Sunucudaki `compatible_donor_types` fonksiyonuyla aynı kuraldır.
    func canDonate(to recipient: BloodType) -> Bool {
        let aboMatches: Bool
        switch abo {
        case .o: aboMatches = true
        case .a: aboMatches = recipient.abo == .a || recipient.abo == .ab
        case .b: aboMatches = recipient.abo == .b || recipient.abo == .ab
        case .ab: aboMatches = recipient.abo == .ab
        }
        // Rh-negatif bağışçı herkese verebilir; Rh-pozitif yalnızca Rh-pozitif alıcıya.
        return aboMatches && (!isRhPositive || recipient.isRhPositive)
    }
}

enum Gender: String, CaseIterable, Identifiable, Sendable, Hashable {
    case male = "MALE", female = "FEMALE", other = "OTHER"

    var id: String { rawValue }
    var localizedName: String {
        switch self {
        case .male: return L10n.string("gender.male")
        case .female: return L10n.string("gender.female")
        case .other: return L10n.string("gender.other")
        }
    }
}
