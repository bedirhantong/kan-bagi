import Foundation

/// Bildirim alınabilecek bölge: bir il (`district == nil`) ya da bir ilçe.
/// `key` sunucunun ürettiği kimliktir (ör. "Antalya", "Antalya/Muratpaşa"); uygulama biçimini bilmez, yalnızca saklar.
struct NotificationArea: Identifiable, Hashable, Sendable {
    let key: String
    let city: String
    let district: String?
    let hospitalCount: Int

    var id: String { key }
    var isCity: Bool { district == nil }
    /// "Muratpaşa, Antalya" ya da "Antalya".
    var displayName: String { district.map { "\($0), \(city)" } ?? city }
}

/// Bildirim kapsamı kuralları (sunucudaki `notification_scope_limit` ile aynı).
enum NotificationScope {
    /// İl/ilçe ve hastane seçimlerinin toplam üst sınırı. Tüm ülkeye yayını önler, bildirim yükünü sınırlar.
    static let maxSelections = 10
}

/// Bir il ve ilçeleri (seçim listesinde tek grup).
struct CityAreas: Identifiable, Equatable, Sendable {
    let city: NotificationArea
    let districts: [NotificationArea]
    var id: String { city.key }
}

/// Sunucudan gelen düz bölge listesini illere göre gruplar ve arar.
struct NotificationAreaCatalog: Equatable, Sendable {
    let cities: [CityAreas]
    private let byKey: [String: NotificationArea]

    init(_ areas: [NotificationArea]) {
        let citiesByName = Dictionary(areas.filter(\.isCity).map { ($0.city, $0) }, uniquingKeysWith: { first, _ in first })
        let districtsByCity = Dictionary(grouping: areas.filter { !$0.isCity }, by: \.city)
        let order = Self.collator
        cities = citiesByName.values
            .sorted { order($0.city, $1.city) }
            .map { city in
                CityAreas(city: city, districts: (districtsByCity[city.city] ?? []).sorted { order($0.district ?? "", $1.district ?? "") })
            }
        byKey = Dictionary(areas.map { ($0.key, $0) }, uniquingKeysWith: { first, _ in first })
    }

    func area(forKey key: String) -> NotificationArea? { byKey[key] }

    func city(named name: String) -> CityAreas? { cities.first { $0.city.city == name } }

    /// Arama: il adı eşleşirse ilin tamamı, yalnızca ilçe eşleşirse il + eşleşen ilçeler döner.
    /// Türkçe karakterlere ve büyük/küçük harfe duyarsızdır ("istanbul" -> "İstanbul", "cankaya" -> "Çankaya").
    func filtered(by query: String) -> [CityAreas] {
        let needle = Self.normalize(query)
        guard !needle.isEmpty else { return cities }
        return cities.compactMap { group in
            if Self.normalize(group.city.city).contains(needle) { return group }
            let districts = group.districts.filter { Self.normalize($0.district ?? "").contains(needle) }
            return districts.isEmpty ? nil : CityAreas(city: group.city, districts: districts)
        }
    }

    static func normalize(_ text: String) -> String {
        text.trimmingCharacters(in: .whitespacesAndNewlines)
            .folding(options: [.caseInsensitive, .diacriticInsensitive], locale: Locale(identifier: "tr_TR"))
            .replacingOccurrences(of: "ı", with: "i")
    }

    private static let collator: @Sendable (String, String) -> Bool = { lhs, rhs in
        lhs.compare(rhs, locale: Locale(identifier: "tr_TR")) == .orderedAscending
    }
}

/// Seçim kuralları (saf mantık; hem kayıt akışında hem ayarlarda kullanılır):
/// - İl seçilince o ilin ilçe seçimleri kaldırılır (il zaten hepsini kapsar).
/// - İli seçili olan ilçe ayrıca seçilemez.
/// - Toplam seçim (il/ilçe + hastane) `NotificationScope.maxSelections`'ı aşamaz.
struct NotificationAreaSelection: Equatable, Sendable {
    private(set) var keys: Set<String>
    /// Aynı sınırı paylaşan diğer seçimler (hastaneler).
    let otherSelections: Int

    init(keys: Set<String> = [], otherSelections: Int = 0) {
        self.keys = keys
        self.otherSelections = otherSelections
    }

    var count: Int { keys.count + otherSelections }
    var remaining: Int { max(0, NotificationScope.maxSelections - count) }
    var isAtLimit: Bool { remaining == 0 }

    func isSelected(_ area: NotificationArea) -> Bool { keys.contains(area.key) }

    /// İlçe, ili seçildiği için zaten kapsanıyor mu?
    func isCovered(_ area: NotificationArea, in catalog: NotificationAreaCatalog) -> Bool {
        guard !area.isCity, let city = catalog.city(named: area.city) else { return false }
        return keys.contains(city.city.key)
    }

    func canToggle(_ area: NotificationArea, in catalog: NotificationAreaCatalog) -> Bool {
        if isSelected(area) { return true }
        if isCovered(area, in: catalog) { return false }
        if area.isCity {
            // İl seçmek ilçelerini kaldırır: sınır, bırakılan yer kadar esner.
            let freed = catalog.city(named: area.city)?.districts.filter { keys.contains($0.key) }.count ?? 0
            return remaining + freed > 0
        }
        return !isAtLimit
    }

    /// Seçimi değiştirir; kurala aykırıysa hiçbir şey yapmaz ve `false` döner.
    @discardableResult
    mutating func toggle(_ area: NotificationArea, in catalog: NotificationAreaCatalog) -> Bool {
        guard canToggle(area, in: catalog) else { return false }
        if keys.remove(area.key) != nil { return true }
        if area.isCity, let group = catalog.city(named: area.city) {
            keys.subtract(group.districts.map(\.key))
        }
        keys.insert(area.key)
        return true
    }

    /// Seçili bölgeler: önce iller, sonra ilçeler. Katalogda olmayan anahtarlar listelenmez ama seçimden de silinmez.
    func selectedAreas(in catalog: NotificationAreaCatalog) -> [NotificationArea] {
        keys.compactMap(catalog.area(forKey:))
            .sorted { ($0.isCity ? 0 : 1, $0.displayName) < ($1.isCity ? 0 : 1, $1.displayName) }
    }
}
