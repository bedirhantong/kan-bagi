import Foundation

/// Mesafeyi bölgeye uygun birimle ("850 m", "3,2 km" / "0.5 mi") biçimlendirir.
enum DistanceFormatting {
    static func string(meters: Double, locale: Locale = LocalizationOverride.locale) -> String {
        let measurement = Measurement(value: meters, unit: UnitLength.meters)
        return measurement.formatted(
            .measurement(width: .abbreviated, usage: .road, numberFormatStyle: .number.precision(.fractionLength(0...1)))
                .locale(locale))
    }
}
