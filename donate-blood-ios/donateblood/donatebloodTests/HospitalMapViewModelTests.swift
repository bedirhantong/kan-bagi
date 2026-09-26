import Foundation
import Testing
@testable import donateblood

struct MapViewportTests {
    private let ankara = Coordinate(latitude: 39.93, longitude: 32.86)

    @Test func radiusIsClamped() {
        #expect(MapViewport(center: ankara, radiusKm: 0.1).radiusKm == MapViewport.minRadiusKm)
        #expect(MapViewport(center: ankara, radiusKm: 5000).radiusKm == MapViewport.maxRadiusKm)
        #expect(MapViewport(center: ankara, radiusKm: 20).radiusKm == 20)
    }

    @Test func searchIsOfferedOnlyAfterMeaningfulMovement() {
        let loaded = MapViewport(center: ankara, radiusKm: 20)
        #expect(MapViewport(center: ankara, radiusKm: 20).needsSearch(comparedTo: nil))
        #expect(!MapViewport(center: ankara, radiusKm: 22).needsSearch(comparedTo: loaded))              // küçük yakınlaştırma
        let smallPan = Coordinate(latitude: 39.97, longitude: 32.86)                                       // ~4,5 km
        #expect(!MapViewport(center: smallPan, radiusKm: 20).needsSearch(comparedTo: loaded))
        let bigPan = Coordinate(latitude: 40.05, longitude: 32.86)                                         // ~13 km (> %35 × 20)
        #expect(MapViewport(center: bigPan, radiusKm: 20).needsSearch(comparedTo: loaded))
        #expect(MapViewport(center: ankara, radiusKm: 30).needsSearch(comparedTo: loaded))                // uzaklaştırma (> %40)
    }
}

@MainActor
struct HospitalMapViewModelTests {
    private struct Fixture {
        let sut: HospitalMapViewModel
        let hospitals: FakeHospitalRepository
        let points: FakeBloodDonationPointRepository
        let location: FakeLocationProvider
    }

    private let userLocation = Coordinate(latitude: 36.9, longitude: 30.7)

    private func makeFixture(location: FakeLocationProvider) -> Fixture {
        let hospitals = FakeHospitalRepository()
        hospitals.all = [TestData.hospital]
        let points = FakeBloodDonationPointRepository()
        points.points = [TestData.bloodPoint(id: 1), TestData.bloodPoint(id: 2, kind: .mobile)]
        let requests = FakeBloodRequestRepository()
        requests.listHandler = { filter, _ in filter.hospital?.id == TestData.hospital.id ? TestData.requests(count: 2) : [] }
        let sut = HospitalMapViewModel(
            getNearby: .init(hospitals: hospitals), getNearbyBloodPoints: .init(points: points),
            getRequests: .init(requests: requests), getCurrentLocation: .init(location: location))
        return Fixture(sut: sut, hospitals: hospitals, points: points, location: location)
    }

    @Test func startCentersOnTheUserAndLoadsOnce() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        await f.sut.start()
        await f.sut.start() // ikinci çağrı bir şey yapmaz
        #expect(f.sut.cameraCommand?.viewport.center == userLocation)
        #expect(f.sut.userLocation == userLocation)
        #expect(f.hospitals.nearbyCalls.count == 1)
        #expect(f.sut.hospitals == [TestData.hospital])
        #expect(f.sut.bloodPoints.count == 2)
        #expect(f.sut.hasLoaded && !f.sut.isLoading)
        #expect(!f.sut.isLocationDenied)
    }

    @Test func withoutPermissionItOpensOnTheDefaultCenterAndSaysSo() async {
        let f = makeFixture(location: FakeLocationProvider(nil, authorization: .denied))
        await f.sut.start()
        #expect(f.sut.cameraCommand?.viewport.center == HospitalMapViewModel.defaultCenter)
        #expect(f.sut.isLocationDenied)
        #expect(f.sut.distance(to: TestData.hospital.coordinate) == nil)
    }

    @Test func searchButtonAppearsOnlyAfterMovingAndLoadsTheVisibleArea() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        await f.sut.start()
        f.sut.cameraDidMove(to: MapViewport(center: userLocation, radiusKm: 15))
        #expect(!f.sut.canSearchVisibleArea)

        let farAway = MapViewport(center: Coordinate(latitude: 38.4, longitude: 27.1), radiusKm: 15)
        f.sut.cameraDidMove(to: farAway)
        #expect(f.sut.canSearchVisibleArea)

        await f.sut.searchVisibleArea()
        #expect(f.hospitals.nearbyCalls.last?.latitude == 38.4)
        #expect(f.sut.loadedViewport == farAway)
        #expect(!f.sut.canSearchVisibleArea)
    }

    @Test func aSlowOlderResponseDoesNotOverwriteANewerOne() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        let other = Hospital(id: 99, name: "Yeni", address: nil, phoneNumber: nil, email: nil, website: nil,
                             latitude: 41, longitude: 29, cityName: nil, districtName: nil, iconURL: nil)
        f.hospitals.delayByLatitude[1] = .milliseconds(300)
        f.hospitals.resultByLatitude[1] = [TestData.hospital]
        f.hospitals.resultByLatitude[2] = [other]

        let slow = Task { await f.sut.load(MapViewport(center: Coordinate(latitude: 1, longitude: 1), radiusKm: 10)) }
        try? await Task.sleep(for: .milliseconds(50))
        await f.sut.load(MapViewport(center: Coordinate(latitude: 2, longitude: 2), radiusKm: 10))
        await slow.value

        #expect(f.sut.hospitals == [other])
        #expect(f.sut.loadedViewport?.center.latitude == 2)
        #expect(!f.sut.isLoading)
    }

    @Test func oneLayerFailingStillShowsTheOther() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        f.hospitals.nearbyError = AppError.network
        await f.sut.start()
        #expect(f.sut.hospitals.isEmpty)
        #expect(f.sut.bloodPoints.count == 2)
        #expect(f.sut.error == .network)
    }

    @Test func layerFiltersMarkersAndClearsAHiddenSelection() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        await f.sut.start()
        await f.sut.select(TestData.hospital)
        #expect(f.sut.selectedRequests.count == 2)

        f.sut.layer = .bloodPoints
        #expect(f.sut.visibleHospitals.isEmpty)
        #expect(f.sut.visibleBloodPoints.count == 2)
        #expect(f.sut.selection == nil) // gizlenen katmandaki seçim kalkar

        f.sut.layer = .hospitals
        #expect(f.sut.visibleBloodPoints.isEmpty)
        #expect(!f.sut.isEmptyResult)
    }

    @Test func selectingAPointReplacesTheHospital() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        await f.sut.start()
        await f.sut.select(TestData.hospital)
        let point = TestData.bloodPoint()
        f.sut.select(point)
        #expect(f.sut.selection == .bloodPoint(point))
        #expect(f.sut.selection?.markerID == "p1")
        #expect(f.sut.selectedRequests.isEmpty)
        f.sut.clearSelection()
        #expect(f.sut.selection == nil)
    }

    @Test func emptyAreaIsReported() async {
        let f = makeFixture(location: FakeLocationProvider(userLocation))
        f.hospitals.all = []
        f.points.points = []
        await f.sut.start()
        #expect(f.sut.isEmptyResult)
    }

    @Test func centerOnUserReportsDeniedPermission() async {
        let location = FakeLocationProvider(userLocation)
        let f = makeFixture(location: location)
        await f.sut.start()
        location.coordinate = nil
        location.authorizationState = .denied
        await f.sut.centerOnUser()
        #expect(f.sut.isLocationDenied)
    }
}

struct BloodDonationPointMappingTests {
    private func decode(_ json: String) throws -> BloodDonationPoint {
        try JSONDecoder().decode(BloodDonationPointDTO.self, from: Data(json.utf8)).toDomain()
    }

    @Test func mapsAllFieldsFromTheServerRow() throws {
        let point = try decode(#"{"id":7,"name":"Adana Şehir Hastanesi Kan Alma Birimi","address":"Dr. Mithat Özsan Blv.","neighborhood":"Kışla","district":"Yüreğir","province_name":"Adana","phone_number":"+90 322 454 61 31","lat":37.03,"lon":35.34,"kind":"MOBILE"}"#)
        #expect(point.id == 7)
        #expect(point.kind == .mobile)
        #expect(point.coordinate == Coordinate(latitude: 37.03, longitude: 35.34))
        #expect(point.areaDescription == "Yüreğir, Adana")
    }

    @Test func unknownKindFallsBackToFixedAndMissingFieldsAreNil() throws {
        let point = try decode(#"{"id":1,"name":"X","address":null,"neighborhood":null,"district":null,"province_name":null,"phone_number":null,"lat":1,"lon":2,"kind":"???"}"#)
        #expect(point.kind == .fixed)
        #expect(point.phoneNumber == nil)
        #expect(point.areaDescription.isEmpty)
    }
}
