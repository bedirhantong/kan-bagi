import Foundation

/// Saf doğrulama yardımcıları (UI'dan bağımsız, test edilebilir).
enum Validators {
    static func isValidName(_ value: String) -> Bool {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.count >= 2 && trimmed.count <= 60
    }

    /// Türkiye ve uluslararası numaralar için gevşek doğrulama (10–15 rakam).
    static func isValidPhone(_ value: String) -> Bool {
        let digits = value.filter(\.isNumber)
        return (10...15).contains(digits.count)
    }

    static func isValidEmail(_ value: String) -> Bool {
        let pattern = #"^[A-Z0-9a-z._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$"#
        return value.range(of: pattern, options: .regularExpression) != nil
    }

    static func isValidAge(_ value: Int) -> Bool { (0...120).contains(value) }

    /// Bağış için asgari yaş 18, azami 65 (bilgilendirme amaçlı).
    static func isAdult(birthDate: Date, now: Date = .now, calendar: Calendar = .current) -> Bool {
        (calendar.dateComponents([.year], from: birthDate, to: now).year ?? 0) >= 18
    }
}
