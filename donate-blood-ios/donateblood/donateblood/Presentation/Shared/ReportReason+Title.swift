import SwiftUI

extension ReportReason {
    /// Açık (literal) anahtarlar: dinamik `LocalizedStringKey("...\(x)")` biçimi kataloğa bulunamaz.
    var title: LocalizedStringKey {
        switch self {
        case .spam: return "safety.reason.spam"
        case .misleading: return "safety.reason.misleading"
        case .inappropriate: return "safety.reason.inappropriate"
        case .harassment: return "safety.reason.harassment"
        case .other: return "safety.reason.other"
        }
    }
}
