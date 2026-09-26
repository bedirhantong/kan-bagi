import Foundation

/// Bağışçı ön değerlendirme sorusu. Amaç kesin karar vermek değil, bilinçlendirmektir.
struct HealthQuestion: Identifiable, Hashable, Sendable {
    enum Flag: Sendable { case never, whenYes, whenNo }

    let key: String
    /// Bu cevap verildiğinde uyarı gösterilir.
    let flag: Flag
    var id: String { key }
    var localizedText: String { Bundle.main.localizedString(forKey: "health.q.\(key)", value: nil, table: nil) }
}

typealias HealthAnswers = [String: Bool]

enum HealthCatalog {
    static let questions: [HealthQuestion] = [
        .init(key: "ONAM_FORMU_OKUNDUMU", flag: .whenNo),
        .init(key: "SAGLIKLI_MI", flag: .whenNo),
        .init(key: "ILAC_KULLANIYOR_MUSUNUZ", flag: .whenYes),
        .init(key: "ENFEKSIYON_ILAC_ALIMI", flag: .whenYes),
        .init(key: "AGRI_KESECI_ALIMI", flag: .whenYes),
        .init(key: "ASI_OLUNDU_MU", flag: .whenYes),
        .init(key: "KRONIK_HASTALIK", flag: .whenYes),
        .init(key: "AIDS_HASTALIGI", flag: .whenYes),
        .init(key: "UYUSTURUCU_KULLANIMI", flag: .whenYes),
        .init(key: "HEPATIT_B", flag: .whenYes),
        .init(key: "HEPATIT_C", flag: .whenYes),
        .init(key: "SARI_HUMMA", flag: .whenYes),
        .init(key: "BOGMACA", flag: .whenYes),
        .init(key: "TIFO", flag: .whenYes),
        .init(key: "SITMA", flag: .whenYes),
        .init(key: "BRUSELLA", flag: .whenYes),
        .init(key: "VEREM", flag: .whenYes),
        .init(key: "KANSER", flag: .whenYes),
        .init(key: "KALP_HASTALIGI", flag: .whenYes),
        .init(key: "TANSIYON", flag: .whenYes),
        .init(key: "SEKER_HASTALIGI", flag: .whenYes),
        .init(key: "TIROID", flag: .whenYes),
        .init(key: "EPILEPSI", flag: .whenYes),
        .init(key: "ASTIM", flag: .whenYes),
        .init(key: "BOBREK_HASTALIGI", flag: .whenYes),
        .init(key: "KARACIGER_HASTALIGI", flag: .whenYes),
        .init(key: "ROMATIZMA", flag: .whenYes),
        .init(key: "GUT_HASTALIGI", flag: .whenYes),
        .init(key: "KANAMA_BOZUKLUGU", flag: .whenYes),
        .init(key: "AMELIYAT", flag: .whenYes),
        .init(key: "DIS_CEKIMI", flag: .whenYes),
        .init(key: "KAN_NAKLI", flag: .whenYes),
        .init(key: "ORGAN_NAKLI", flag: .whenYes),
        .init(key: "GEBELIK", flag: .whenYes),
        .init(key: "EMZIRME", flag: .whenYes),
        .init(key: "ADET_DONEMI", flag: .never),
        .init(key: "CINSEL_ILISKI", flag: .whenYes),
        .init(key: "DOVME", flag: .whenYes),
        .init(key: "PIERCING", flag: .whenYes),
        .init(key: "AKAPUNKTUR", flag: .whenYes),
        .init(key: "ALKOL", flag: .whenYes),
        .init(key: "SIGARA", flag: .never),
        .init(key: "UYKU", flag: .whenNo),
        .init(key: "KAHVALTI", flag: .whenNo),
        .init(key: "HASTALIK_BELIRTISI", flag: .whenYes),
    ]
}

/// Cevaplara göre dikkat edilmesi gereken soruları hesaplar (saf fonksiyon).
enum HealthFormEvaluator {
    static func flaggedQuestions(for answers: HealthAnswers) -> [HealthQuestion] {
        HealthCatalog.questions.filter { question in
            guard let answer = answers[question.key] else { return false }
            switch question.flag {
            case .never: return false
            case .whenYes: return answer
            case .whenNo: return !answer
            }
        }
    }

    static func isComplete(_ answers: HealthAnswers) -> Bool {
        HealthCatalog.questions.allSatisfy { answers[$0.key] != nil }
    }
}
