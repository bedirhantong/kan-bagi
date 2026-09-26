import SwiftUI
import MapKit

/// Haritadaki bir yer (hastane ya da kan bağış noktası). Kimlik sabittir, böylece güncellemede yalnızca farklar eklenip çıkarılır.
final class PlaceAnnotation: NSObject, MKAnnotation {
    enum Kind {
        case hospital(Hospital)
        case bloodPoint(BloodDonationPoint)
    }

    let id: String
    let kind: Kind
    let coordinate: CLLocationCoordinate2D
    let title: String?

    init?(hospital: Hospital) {
        guard let coordinate = hospital.coordinate else { return nil }
        id = MapMarkerID.hospital(hospital.id)
        kind = .hospital(hospital)
        self.coordinate = coordinate.clCoordinate
        title = hospital.name
    }

    init(point: BloodDonationPoint) {
        id = MapMarkerID.bloodPoint(point.id)
        kind = .bloodPoint(point)
        coordinate = point.coordinate.clCoordinate
        title = point.name
    }
}

/// UIKit `MKMapView` sarmalayıcısı. SwiftUI `Map` yerine tercih nedenleri (performans ve iOS 16 desteği):
/// - Yerel **kümeleme**: yakın pinler tek bir sayılı işarette toplanır (üst üste binme olmaz).
/// - İşaret görünümleri **yeniden kullanılır**; yüzlerce pinde bile akıcıdır.
/// - Güncellemelerde yalnızca **eklenen/çıkan** işaretler değişir.
/// - Tek kod yolu: iOS 16 ve 17+ için ayrı harita kodu yok.
struct PlacesMapView: UIViewRepresentable {
    let hospitals: [Hospital]
    let bloodPoints: [BloodDonationPoint]
    let selectedMarkerID: String?
    let cameraCommand: HospitalMapViewModel.CameraCommand?
    let onSelectHospital: (Hospital) -> Void
    let onSelectBloodPoint: (BloodDonationPoint) -> Void
    let onDeselect: () -> Void
    let onCameraChange: (MapViewport) -> Void

    func makeCoordinator() -> Coordinator { Coordinator(parent: self) }

    func makeUIView(context: Context) -> MKMapView {
        let map = MKMapView()
        map.delegate = context.coordinator
        map.showsUserLocation = true
        map.showsCompass = true
        map.showsScale = false
        // Apple'ın kendi hastane simgeleri bizim hastane işaretlerimizle çakışmasın.
        map.pointOfInterestFilter = MKPointOfInterestFilter(excluding: [.hospital])
        map.register(HospitalMarkerView.self, forAnnotationViewWithReuseIdentifier: HospitalMarkerView.reuseID)
        map.register(BloodPointMarkerView.self, forAnnotationViewWithReuseIdentifier: BloodPointMarkerView.reuseID)
        map.register(PlaceClusterView.self, forAnnotationViewWithReuseIdentifier: MKMapViewDefaultClusterAnnotationViewReuseIdentifier)
        return map
    }

    func updateUIView(_ map: MKMapView, context: Context) {
        let coordinator = context.coordinator
        coordinator.parent = self
        coordinator.syncAnnotations(on: map)
        coordinator.applyCamera(cameraCommand, on: map)
        coordinator.syncSelection(selectedMarkerID, on: map)
    }

    @MainActor
    final class Coordinator: NSObject, MKMapViewDelegate {
        var parent: PlacesMapView
        private var annotationsByID: [String: PlaceAnnotation] = [:]
        private var lastCameraID: UUID?
        private var isSelectingProgrammatically = false

        init(parent: PlacesMapView) { self.parent = parent }

        // MARK: Senkronizasyon

        func syncAnnotations(on map: MKMapView) {
            var desired: [String: PlaceAnnotation] = [:]
            for hospital in parent.hospitals {
                let id = MapMarkerID.hospital(hospital.id)
                desired[id] = annotationsByID[id] ?? PlaceAnnotation(hospital: hospital)
            }
            for point in parent.bloodPoints {
                let id = MapMarkerID.bloodPoint(point.id)
                desired[id] = annotationsByID[id] ?? PlaceAnnotation(point: point)
            }
            let removed = annotationsByID.filter { desired[$0.key] == nil }.map(\.value)
            let added = desired.filter { annotationsByID[$0.key] == nil }.map(\.value)
            if !removed.isEmpty { map.removeAnnotations(removed) }
            if !added.isEmpty { map.addAnnotations(added) }
            annotationsByID = desired
        }

        func applyCamera(_ command: HospitalMapViewModel.CameraCommand?, on map: MKMapView) {
            guard let command, command.id != lastCameraID else { return }
            lastCameraID = command.id
            let side = command.viewport.radiusKm * 1000 * 2.squareRoot() // yarı köşegen -> kare kenarı
            let region = MKCoordinateRegion(center: command.viewport.center.clCoordinate, latitudinalMeters: side, longitudinalMeters: side)
            map.setRegion(map.regionThatFits(region), animated: map.window != nil)
        }

        func syncSelection(_ id: String?, on map: MKMapView) {
            let selected = map.selectedAnnotations.compactMap { $0 as? PlaceAnnotation }
            if let id, let annotation = annotationsByID[id] {
                guard !selected.contains(annotation) else { return }
                isSelectingProgrammatically = true
                map.selectAnnotation(annotation, animated: true)
                isSelectingProgrammatically = false
            } else if id == nil, !selected.isEmpty {
                isSelectingProgrammatically = true
                selected.forEach { map.deselectAnnotation($0, animated: true) }
                isSelectingProgrammatically = false
            }
        }

        // MARK: MKMapViewDelegate

        func mapView(_ mapView: MKMapView, viewFor annotation: MKAnnotation) -> MKAnnotationView? {
            if annotation is MKUserLocation { return nil }
            if annotation is MKClusterAnnotation {
                return mapView.dequeueReusableAnnotationView(withIdentifier: MKMapViewDefaultClusterAnnotationViewReuseIdentifier, for: annotation)
            }
            guard let place = annotation as? PlaceAnnotation else { return nil }
            switch place.kind {
            case .hospital:
                return mapView.dequeueReusableAnnotationView(withIdentifier: HospitalMarkerView.reuseID, for: annotation)
            case .bloodPoint:
                return mapView.dequeueReusableAnnotationView(withIdentifier: BloodPointMarkerView.reuseID, for: annotation)
            }
        }

        func mapView(_ mapView: MKMapView, didSelect view: MKAnnotationView) {
            if let cluster = view.annotation as? MKClusterAnnotation {
                // Kümeye dokununca içindekileri gösterecek kadar yakınlaş.
                mapView.deselectAnnotation(cluster, animated: false)
                mapView.showAnnotations(cluster.memberAnnotations, animated: true)
                return
            }
            guard !isSelectingProgrammatically, let place = view.annotation as? PlaceAnnotation else { return }
            UISelectionFeedbackGenerator().selectionChanged()
            switch place.kind {
            case .hospital(let hospital): parent.onSelectHospital(hospital)
            case .bloodPoint(let point): parent.onSelectBloodPoint(point)
            }
        }

        func mapView(_ mapView: MKMapView, didDeselect view: MKAnnotationView) {
            guard !isSelectingProgrammatically, view.annotation is PlaceAnnotation else { return }
            // Başka bir pine geçişte önce seçimi bırakıp sonra yenisini seçer; kart titremesin diye bir tur bekle.
            DispatchQueue.main.async { [weak self, weak mapView] in
                guard let self, let mapView, mapView.selectedAnnotations.isEmpty else { return }
                self.parent.onDeselect()
            }
        }

        func mapView(_ mapView: MKMapView, regionDidChangeAnimated animated: Bool) {
            parent.onCameraChange(MapViewport(region: mapView.region))
        }
    }
}

extension MapViewport {
    /// Görünür bölgeden merkez + yarı köşegen.
    init(region: MKCoordinateRegion) {
        let center = region.center.domain
        let corner = Coordinate(latitude: region.center.latitude + region.span.latitudeDelta / 2,
                                longitude: region.center.longitude + region.span.longitudeDelta / 2)
        self.init(center: center, radiusKm: center.distance(to: corner) / 1000)
    }
}

// MARK: - İşaret görünümleri (yeniden kullanılır)

private final class HospitalMarkerView: MKMarkerAnnotationView {
    static let reuseID = "hospital"

    override var annotation: MKAnnotation? {
        didSet {
            clusteringIdentifier = "place"
            markerTintColor = .systemBlue
            glyphImage = UIImage(systemName: "cross.fill")
            displayPriority = .defaultHigh
            animatesWhenAdded = false
        }
    }
}

private final class BloodPointMarkerView: MKMarkerAnnotationView {
    static let reuseID = "bloodPoint"

    override var annotation: MKAnnotation? {
        didSet {
            clusteringIdentifier = "place"
            markerTintColor = UIColor(named: "Brand") ?? .systemRed
            if case .bloodPoint(let point)? = (annotation as? PlaceAnnotation)?.kind {
                glyphImage = UIImage(systemName: point.kind == .mobile ? "bus.fill" : "drop.fill")
            }
            displayPriority = .defaultLow
            animatesWhenAdded = false
        }
    }
}

private final class PlaceClusterView: MKMarkerAnnotationView {
    override var annotation: MKAnnotation? {
        didSet {
            markerTintColor = .darkGray
            displayPriority = .required
            if let cluster = annotation as? MKClusterAnnotation {
                glyphText = cluster.memberAnnotations.count > 99 ? "99+" : "\(cluster.memberAnnotations.count)"
                accessibilityLabel = L10n.format("map.cluster %lld", cluster.memberAnnotations.count)
            }
        }
    }
}
