import Foundation

/// Profil alanları (alan bazlı hata göstermek için).
enum ProfileField: Hashable, Sendable {
    case name, surname, phone, birthDate
}

enum ProfileValidationError: Equatable, Sendable {
    case required
    case invalidName
    case invalidPhone
    case underage
}

/// Profil doğrulamasının tek kaynağı: use case'ler kayıt öncesi, arayüz alan altında hata göstermek için kullanır.
/// Saf ve deterministiktir (tarih ve takvim dışarıdan verilebilir).
enum ProfileValidator {
    /// - Parameter isRegistration: Kayıtta doğum tarihi zorunlu ve 18 yaş şartı var; profil düzenlemede isteğe bağlı.
    static func validate(
        _ update: ProfileUpdate, isRegistration: Bool, now: Date = .now, calendar: Calendar = .current
    ) -> [ProfileField: ProfileValidationError] {
        var errors: [ProfileField: ProfileValidationError] = [:]
        errors[.name] = nameError(update.name)
        errors[.surname] = nameError(update.surname)
        if let phone = update.phoneNumber, !phone.trimmingCharacters(in: .whitespaces).isEmpty, !Validators.isValidPhone(phone) {
            errors[.phone] = .invalidPhone
        }
        if let birthDate = update.birthDate {
            if isRegistration, !Validators.isAdult(birthDate: birthDate, now: now, calendar: calendar) { errors[.birthDate] = .underage }
        } else if isRegistration {
            errors[.birthDate] = .required
        }
        return errors
    }

    private static func nameError(_ value: String) -> ProfileValidationError? {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty { return .required }
        return Validators.isValidName(trimmed) ? nil : .invalidName
    }
}
