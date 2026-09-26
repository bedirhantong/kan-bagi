import Foundation

/// Bildirim bölgesi seçici: il/ilçe listesi, arama, konumdan öneri ve seçim sınırı.
/// Hem kayıt akışında hem Bildirim Ayarları'nda kullanılır; seçim kuralları `NotificationAreaSelection`'dadır.
///
/// `onChange` verilirse her değişiklik hemen kaydedilir (ayarlar); başarısız olursa seçim geri alınır.
/// Verilmezse seçim yalnızca bellekte tutulur ve sahibi (kayıt akışı) istediğinde okur.
@MainActor
final class NotificationAreaPickerModel: ObservableObject {
    enum Suggestion: Equatable {
        case idle, locating
        case added(NotificationArea)
        case alreadySelected(NotificationArea)
        case locationUnavailable, noneNearby, limitReached
    }

    typealias Persist = @MainActor (Set<String>) async -> Bool

    @Published private(set) var state: LoadState = .idle
    @Published private(set) var catalog = NotificationAreaCatalog([])
    @Published private(set) var selection: NotificationAreaSelection
    @Published var query = ""
    @Published private(set) var expandedCities: Set<String> = []
    @Published private(set) var suggestion: Suggestion = .idle

    private let getAreas: GetNotificationAreasUseCase
    private let suggestArea: SuggestNotificationAreaUseCase
    private let persist: Persist?

    init(
        getAreas: GetNotificationAreasUseCase,
        suggestArea: SuggestNotificationAreaUseCase,
        selectedKeys: Set<String> = [],
        otherSelections: Int = 0,
        persist: Persist? = nil
    ) {
        self.getAreas = getAreas
        self.suggestArea = suggestArea
        self.persist = persist
        selection = NotificationAreaSelection(keys: selectedKeys, otherSelections: otherSelections)
    }

    // MARK: Durum

    var selectedKeys: Set<String> { selection.keys }
    var selectedAreas: [NotificationArea] { selection.selectedAreas(in: catalog) }
    var visibleCities: [CityAreas] { catalog.filtered(by: query) }
    var isSearching: Bool { !NotificationAreaCatalog.normalize(query).isEmpty }

    /// İlçeler açık mı: kullanıcı açtıysa ya da arama yalnızca ilçelerle eşleştiyse.
    func isExpanded(_ group: CityAreas) -> Bool {
        if expandedCities.contains(group.id) { return true }
        guard isSearching else { return false }
        return !NotificationAreaCatalog.normalize(group.city.city).contains(NotificationAreaCatalog.normalize(query))
    }

    func isSelected(_ area: NotificationArea) -> Bool { selection.isSelected(area) }
    func isCovered(_ area: NotificationArea) -> Bool { selection.isCovered(area, in: catalog) }
    func canToggle(_ area: NotificationArea) -> Bool { selection.canToggle(area, in: catalog) }

    // MARK: Eylemler

    func load() async {
        guard state != .loaded, state != .loading else { return }
        state = .loading
        do {
            catalog = try await getAreas()
            // Seçili ilçelerin illeri açık gelsin: kullanıcı neyi seçtiğini listede de görsün.
            expandedCities.formUnion(selection.selectedAreas(in: catalog).filter { !$0.isCity }.compactMap { catalog.city(named: $0.city)?.id })
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            state = appError == .cancelled ? .idle : .failed(appError)
        }
    }

    func retry() async {
        state = .idle
        await load()
    }

    func toggle(_ area: NotificationArea) async {
        let previous = selection
        guard selection.toggle(area, in: catalog) else {
            Haptics.warning()
            return
        }
        Haptics.selection()
        if suggestion != .locating { suggestion = .idle }
        await save(reverting: previous)
    }

    func toggleExpanded(_ group: CityAreas) {
        if isExpanded(group) { expandedCities.remove(group.id) } else { expandedCities.insert(group.id) }
    }

    /// Konuma en yakın hastanenin ilçesini seçer (izin gerekirse sistem sorar).
    func useMyLocation() async {
        guard suggestion != .locating else { return }
        suggestion = .locating
        let result = await suggestArea(in: catalog)
        switch result {
        case .locationUnavailable: suggestion = .locationUnavailable
        case .noneNearby: suggestion = .noneNearby
        case .area(let area):
            if let city = catalog.city(named: area.city) { expandedCities.insert(city.id) }
            query = ""
            if selection.isSelected(area) || selection.isCovered(area, in: catalog) {
                suggestion = .alreadySelected(area)
                return
            }
            let previous = selection
            guard selection.toggle(area, in: catalog) else {
                suggestion = .limitReached
                Haptics.warning()
                return
            }
            Haptics.success()
            suggestion = .added(area)
            await save(reverting: previous)
        }
    }

    private func save(reverting previous: NotificationAreaSelection) async {
        guard let persist else { return }
        let keys = selection.keys
        // Geri alma yalnızca bu arada başka bir değişiklik olmadıysa (hızlı art arda dokunmalar).
        if await !persist(keys), selection.keys == keys { selection = previous }
    }
}
