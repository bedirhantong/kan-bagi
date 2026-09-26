import Foundation

/// Haritadaki seçili öğe: hastane ya da Kızılay kan bağış noktası. Aynı anda yalnızca biri seçilir.
enum MapSelection: Equatable {
    case hospital(Hospital)
    case bloodPoint(BloodDonationPoint)

    /// Harita işaretinin kimliği (hastane ve nokta kimlikleri çakışmasın diye önekli).
    var markerID: String {
        switch self {
        case .hospital(let hospital): return MapMarkerID.hospital(hospital.id)
        case .bloodPoint(let point): return MapMarkerID.bloodPoint(point.id)
        }
    }
}

enum MapMarkerID {
    static func hospital(_ id: Int) -> String { "h\(id)" }
    static func bloodPoint(_ id: Int) -> String { "p\(id)" }
}

/// Harita ekranının durumu. CoreLocation/MapKit bilmez; yalnızca domain tipleri ve use case'lerle çalışır.
@MainActor
final class HospitalMapViewModel: ObservableObject {
    enum Layer: String, CaseIterable, Identifiable {
        case all, hospitals, bloodPoints
        var id: String { rawValue }
    }

    /// Haritayı bir bölgeye taşıma komutu. Her komut benzersizdir; aynı bölgeye tekrar gitmek de mümkündür.
    struct CameraCommand: Equatable {
        let id = UUID()
        let viewport: MapViewport
    }

    /// Konum izni yokken açılış bölgesi (Türkiye'nin ortası, Ankara).
    static let defaultCenter = Coordinate(latitude: 39.9334, longitude: 32.8597)

    @Published var layer: Layer = .all {
        didSet { clearSelectionIfHiddenByLayer() }
    }
    @Published private(set) var hospitals: [Hospital] = []
    @Published private(set) var bloodPoints: [BloodDonationPoint] = []
    @Published private(set) var isLoading = false
    @Published private(set) var hasLoaded = false
    @Published private(set) var selection: MapSelection?
    @Published private(set) var selectedRequests: [BloodRequest] = []
    @Published private(set) var isLoadingRequests = false
    @Published private(set) var canSearchVisibleArea = false
    @Published private(set) var cameraCommand: CameraCommand?
    @Published private(set) var userLocation: Coordinate?
    @Published var isLocationDenied = false
    @Published var error: AppError?

    private let getNearby: GetNearbyHospitalsUseCase
    private let getNearbyBloodPoints: GetNearbyBloodPointsUseCase
    private let getRequests: GetBloodRequestsUseCase
    private let getCurrentLocation: GetCurrentLocationUseCase

    private(set) var loadedViewport: MapViewport?
    private var visibleViewport: MapViewport?
    private var hasStarted = false
    /// Her yükleme bir nesil numarası alır; geç dönen eski yanıtlar yenisinin üzerine yazmaz.
    private var loadGeneration = 0
    private var requestsGeneration = 0

    init(
        getNearby: GetNearbyHospitalsUseCase,
        getNearbyBloodPoints: GetNearbyBloodPointsUseCase,
        getRequests: GetBloodRequestsUseCase,
        getCurrentLocation: GetCurrentLocationUseCase
    ) {
        self.getNearby = getNearby
        self.getNearbyBloodPoints = getNearbyBloodPoints
        self.getRequests = getRequests
        self.getCurrentLocation = getCurrentLocation
    }

    // MARK: Görünür veri

    var visibleHospitals: [Hospital] { layer == .bloodPoints ? [] : hospitals }
    var visibleBloodPoints: [BloodDonationPoint] { layer == .hospitals ? [] : bloodPoints }

    /// Yükleme bitti ama seçili katmanda bu bölgede hiçbir şey yok.
    var isEmptyResult: Bool { hasLoaded && !isLoading && visibleHospitals.isEmpty && visibleBloodPoints.isEmpty }

    /// Kullanıcıdan bir yere mesafe (metre); konum yoksa `nil`.
    func distance(to coordinate: Coordinate?) -> Double? {
        guard let userLocation, let coordinate else { return nil }
        return userLocation.distance(to: coordinate)
    }

    // MARK: Yaşam döngüsü

    /// İlk açılış: önce konum (gerekirse izin), sonra tek bir yükleme. Tekrar çağrılırsa bir şey yapmaz.
    func start() async {
        guard !hasStarted else { return }
        hasStarted = true
        let coordinate = await getCurrentLocation()
        userLocation = coordinate
        if coordinate == nil { isLocationDenied = await getCurrentLocation.authorization() == .denied }
        let viewport = MapViewport(center: coordinate ?? Self.defaultCenter, radiusKm: coordinate == nil ? 40 : 15)
        cameraCommand = CameraCommand(viewport: viewport)
        await load(viewport)
    }

    /// Harita her durduğunda çağrılır; yalnızca "ara" önerisini günceller, istek atmaz.
    func cameraDidMove(to viewport: MapViewport) {
        visibleViewport = viewport
        canSearchVisibleArea = !isLoading && viewport.needsSearch(comparedTo: loadedViewport)
    }

    func searchVisibleArea() async {
        guard let visibleViewport else { return }
        await load(visibleViewport)
    }

    func centerOnUser() async {
        if let coordinate = await getCurrentLocation() {
            userLocation = coordinate
            isLocationDenied = false
            cameraCommand = CameraCommand(viewport: MapViewport(center: coordinate, radiusKm: 10))
        } else {
            isLocationDenied = await getCurrentLocation.authorization() == .denied
        }
    }

    // MARK: Seçim

    func select(_ hospital: Hospital) async {
        selection = .hospital(hospital)
        selectedRequests = []
        requestsGeneration += 1
        let generation = requestsGeneration
        isLoadingRequests = true
        let result = try? await getRequests(filter: BloodRequestFilter(hospital: hospital), page: PageRequest(offset: 0, limit: 3))
        guard generation == requestsGeneration else { return }
        selectedRequests = result ?? []
        isLoadingRequests = false
    }

    func select(_ point: BloodDonationPoint) {
        requestsGeneration += 1
        selectedRequests = []
        isLoadingRequests = false
        selection = .bloodPoint(point)
    }

    func clearSelection() {
        requestsGeneration += 1
        selection = nil
        selectedRequests = []
        isLoadingRequests = false
    }

    // MARK: Yükleme

    /// İki katman paralel yüklenir; biri başarısız olsa da diğeri gösterilir.
    func load(_ viewport: MapViewport) async {
        loadGeneration += 1
        let generation = loadGeneration
        isLoading = true
        canSearchVisibleArea = false

        let center = viewport.center
        async let hospitalsResult = Result { try await getNearby(latitude: center.latitude, longitude: center.longitude, radiusKm: viewport.radiusKm) }
        async let pointsResult = Result { try await getNearbyBloodPoints(latitude: center.latitude, longitude: center.longitude, radiusKm: viewport.radiusKm) }
        let (hospitalsOutcome, pointsOutcome) = await (hospitalsResult, pointsResult)
        guard generation == loadGeneration else { return } // daha yeni bir yükleme başladı

        var failure: AppError?
        switch hospitalsOutcome {
        case .success(let value): hospitals = value
        case .failure(let error): failure = AppError.from(error)
        }
        switch pointsOutcome {
        case .success(let value): bloodPoints = value
        case .failure(let error): failure = failure ?? AppError.from(error)
        }
        loadedViewport = viewport
        hasLoaded = true
        isLoading = false
        if let visibleViewport { canSearchVisibleArea = visibleViewport.needsSearch(comparedTo: viewport) }
        if let failure, failure != .cancelled { error = failure }
        clearSelectionIfNoLongerPresent()
    }

    private func clearSelectionIfHiddenByLayer() {
        switch (selection, layer) {
        case (.hospital, .bloodPoints), (.bloodPoint, .hospitals): clearSelection()
        default: break
        }
    }

    private func clearSelectionIfNoLongerPresent() {
        switch selection {
        case .hospital(let hospital) where !hospitals.contains(where: { $0.id == hospital.id }): clearSelection()
        case .bloodPoint(let point) where !bloodPoints.contains(where: { $0.id == point.id }): clearSelection()
        default: break
        }
    }
}
