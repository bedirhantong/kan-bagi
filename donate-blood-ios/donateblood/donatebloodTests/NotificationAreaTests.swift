import Foundation
import Testing
@testable import donateblood

struct NotificationAreaCatalogTests {
    private let catalog = NotificationAreaCatalog(TestData.areas)

    @Test func groupsDistrictsUnderCitiesInTurkishOrder() {
        #expect(catalog.cities.map(\.city.city) == ["Ankara", "Antalya", "İstanbul"])
        #expect(catalog.city(named: "Antalya")?.districts.compactMap(\.district) == ["Konyaaltı", "Muratpaşa"])
        #expect(catalog.city(named: "İstanbul")?.districts.compactMap(\.district) == ["Fatih", "Pendik"])
    }

    @Test func searchIgnoresCaseAndTurkishCharacters() {
        #expect(catalog.filtered(by: "istanbul").map(\.city.city) == ["İstanbul"])
        #expect(catalog.filtered(by: "ISTANBUL").map(\.city.city) == ["İstanbul"])
        // Yalnızca ilçe eşleşirse il, yalnızca eşleşen ilçesiyle döner.
        let cankaya = catalog.filtered(by: "cankaya")
        #expect(cankaya.map(\.city.city) == ["Ankara"])
        #expect(cankaya.first?.districts.compactMap(\.district) == ["Çankaya"])
        // İl eşleşirse tüm ilçeleri gelir.
        #expect(catalog.filtered(by: "antal").first?.districts.count == 2)
        #expect(catalog.filtered(by: "  ").count == 3)
        #expect(catalog.filtered(by: "xyz").isEmpty)
    }
}

struct NotificationAreaSelectionTests {
    private let catalog = NotificationAreaCatalog(TestData.areas)
    private func area(_ key: String) -> NotificationArea { catalog.area(forKey: key)! }

    @Test func selectingACityReplacesItsDistrictsAndCoversThem() {
        var selection = NotificationAreaSelection()
        selection.toggle(area("Antalya/Muratpaşa"), in: catalog)
        selection.toggle(area("İstanbul/Fatih"), in: catalog)
        selection.toggle(area("Antalya"), in: catalog)
        #expect(selection.keys == ["Antalya", "İstanbul/Fatih"])
        #expect(selection.isCovered(area("Antalya/Konyaaltı"), in: catalog))
        // Kapsanan ilçe ayrıca seçilemez.
        let toggled1 = selection.toggle(area("Antalya/Konyaaltı"), in: catalog)
        #expect(!toggled1)
        #expect(selection.keys.count == 2)
    }

    @Test func limitIsSharedWithHospitals() {
        var selection = NotificationAreaSelection(otherSelections: NotificationScope.maxSelections - 1)
        let toggled2 = selection.toggle(area("Ankara"), in: catalog)
        #expect(toggled2)
        #expect(selection.isAtLimit)
        let toggled3 = selection.toggle(area("İstanbul"), in: catalog)
        #expect(!toggled3)
        // Kaldırmak her zaman serbest.
        let toggled4 = selection.toggle(area("Ankara"), in: catalog)
        #expect(toggled4)
        #expect(selection.keys.isEmpty)
    }

    @Test func choosingTheCityIsAllowedAtTheLimitBecauseItFreesItsDistricts() {
        var selection = NotificationAreaSelection(otherSelections: NotificationScope.maxSelections - 2)
        selection.toggle(area("Antalya/Muratpaşa"), in: catalog)
        selection.toggle(area("Antalya/Konyaaltı"), in: catalog)
        #expect(selection.isAtLimit)
        let toggled5 = selection.toggle(area("Antalya"), in: catalog)
        #expect(toggled5)
        #expect(selection.keys == ["Antalya"])
        #expect(selection.remaining == 1)
    }

    @Test func selectedAreasListCitiesFirstAndSkipUnknownKeys() {
        let selection = NotificationAreaSelection(keys: ["İstanbul/Pendik", "Ankara", "Silinmiş/İlçe"])
        #expect(selection.selectedAreas(in: catalog).map(\.key) == ["Ankara", "İstanbul/Pendik"])
        #expect(selection.count == 3) // bilinmeyen anahtar silinmez, sınıra sayılır
    }
}

struct NotificationScopeUseCaseTests {
    @Test func updateRejectsMoreThanTheLimit() async {
        let repo = FakeNotificationRepository()
        var preferences = NotificationPreferences.default
        preferences.preferredAreaKeys = Set((0..<NotificationScope.maxSelections).map { "İl\($0)" })
        preferences.preferredHospitalIds = [1]
        await #expect(throws: AppError.selectionLimit(NotificationScope.maxSelections)) {
            try await UpdateNotificationPreferencesUseCase(notifications: repo)(preferences)
        }
        #expect(repo.updateCount == 0)
    }

    @Test func settingAreasKeepsOtherPreferences() async throws {
        let repo = FakeNotificationRepository()
        repo.stored.chatNotifications = false
        repo.stored.preferredHospitalIds = [7]
        try await SetNotificationAreasUseCase(notifications: repo)(["Ankara"])
        #expect(repo.stored.preferredAreaKeys == ["Ankara"])
        #expect(repo.stored.chatNotifications == false)
        #expect(repo.stored.preferredHospitalIds == [7])
        // Değişmeyen seçim ağa gitmez.
        try await SetNotificationAreasUseCase(notifications: repo)(["Ankara"])
        #expect(repo.updateCount == 1)
    }

    @Test func suggestionUsesTheNearestHospitalsDistrict() async {
        let catalog = NotificationAreaCatalog(TestData.areas)
        let hospitals = FakeHospitalRepository()
        hospitals.all = [TestData.hospital] // Muratpaşa, Antalya
        let sut = SuggestNotificationAreaUseCase(location: FakeLocationProvider(Coordinate(latitude: 36.9, longitude: 30.7)), hospitals: hospitals)
        #expect(await sut(in: catalog) == .area(catalog.area(forKey: "Antalya/Muratpaşa")!))
    }

    @Test func suggestionFallsBackToTheCityOrReportsWhyItFailed() async {
        let catalog = NotificationAreaCatalog(TestData.areas)
        let here = FakeLocationProvider(Coordinate(latitude: 36.9, longitude: 30.7))
        let hospitals = FakeHospitalRepository()
        hospitals.all = [Hospital(id: 9, name: "H", address: nil, phoneNumber: nil, email: nil, website: nil,
                                  latitude: 36.9, longitude: 30.7, cityName: "Antalya", districtName: "Kepez", iconURL: nil)]
        #expect(await SuggestNotificationAreaUseCase(location: here, hospitals: hospitals)(in: catalog)
                == .area(catalog.area(forKey: "Antalya")!))

        #expect(await SuggestNotificationAreaUseCase(location: FakeLocationProvider(nil), hospitals: hospitals)(in: catalog)
                == .locationUnavailable)
        #expect(await SuggestNotificationAreaUseCase(location: here, hospitals: FakeHospitalRepository())(in: catalog)
                == .noneNearby)
    }
}

@MainActor
struct NotificationAreaPickerModelTests {
    private func makeSUT(
        repo: FakeNotificationRepository = FakeNotificationRepository(),
        hospitals: [Hospital] = [TestData.hospital],
        location: Coordinate? = Coordinate(latitude: 36.9, longitude: 30.7),
        selected: Set<String> = [], otherSelections: Int = 0
    ) -> NotificationAreaPickerModel {
        let hospitalRepo = FakeHospitalRepository()
        hospitalRepo.all = hospitals
        return NotificationAreaPickerModel(
            getAreas: .init(notifications: repo),
            suggestArea: .init(location: FakeLocationProvider(location), hospitals: hospitalRepo),
            selectedKeys: selected, otherSelections: otherSelections)
    }

    @Test func loadExpandsCitiesOfSelectedDistricts() async throws {
        let sut = makeSUT(selected: ["İstanbul/Pendik"])
        await sut.load()
        #expect(sut.state == .loaded)
        #expect(sut.isExpanded(try #require(sut.catalog.city(named: "İstanbul"))))
        #expect(!sut.isExpanded(try #require(sut.catalog.city(named: "Ankara"))))
        #expect(sut.selectedAreas.map(\.key) == ["İstanbul/Pendik"])
    }

    @Test func loadFailureCanBeRetried() async {
        let repo = FakeNotificationRepository()
        repo.areasError = AppError.network
        let sut = makeSUT(repo: repo)
        await sut.load()
        #expect(sut.state == .failed(.network))
        repo.areasError = nil
        await sut.retry()
        #expect(sut.state == .loaded)
    }

    @Test func searchingForADistrictOpensItsCity() async throws {
        let sut = makeSUT()
        await sut.load()
        sut.query = "pendik"
        let istanbul = try #require(sut.visibleCities.first)
        #expect(sut.visibleCities.count == 1)
        #expect(sut.isExpanded(istanbul))
    }

    @Test func myLocationSelectsTheNearestDistrictOnce() async {
        let sut = makeSUT()
        await sut.load()
        await sut.useMyLocation()
        let muratpasa = sut.catalog.area(forKey: "Antalya/Muratpaşa")!
        #expect(sut.suggestion == .added(muratpasa))
        #expect(sut.selectedKeys == ["Antalya/Muratpaşa"])
        await sut.useMyLocation()
        #expect(sut.suggestion == .alreadySelected(muratpasa))
        #expect(sut.selectedKeys.count == 1)
    }

    @Test func myLocationReportsLimitAndMissingLocation() async {
        let full = makeSUT(otherSelections: NotificationScope.maxSelections)
        await full.load()
        await full.useMyLocation()
        #expect(full.suggestion == .limitReached)
        #expect(full.selectedKeys.isEmpty)

        let noLocation = makeSUT(location: nil)
        await noLocation.load()
        await noLocation.useMyLocation()
        #expect(noLocation.suggestion == .locationUnavailable)
    }
}

struct NotificationAreaMappingTests {
    @Test func dtoDecodesCityAndDistrictRows() throws {
        let json = """
        [{"area_key":"Antalya","city":"Antalya","district":null,"hospital_count":3},
         {"area_key":"Antalya/Muratpaşa","city":"Antalya","district":"Muratpaşa","hospital_count":2}]
        """
        let areas = try JSONDecoder().decode([NotificationAreaDTO].self, from: Data(json.utf8)).map { $0.toDomain() }
        #expect(areas[0].isCity)
        #expect(areas[1].displayName == "Muratpaşa, Antalya")
        #expect(areas[1].hospitalCount == 2)
    }

    @Test func preferencesRoundTripKeepAreas() throws {
        var preferences = NotificationPreferences.default
        preferences.preferredAreaKeys = ["Ankara", "Antalya/Muratpaşa"]
        let data = try JSONEncoder().encode(NotificationPreferencesDTO(preferences))
        let object = try #require(try JSONSerialization.jsonObject(with: data) as? [String: Any])
        #expect(object["preferred_areas"] as? [String] == ["Ankara", "Antalya/Muratpaşa"])
        let decoded = try JSONDecoder().decode(NotificationPreferencesDTO.self, from: data).toDomain()
        #expect(decoded.preferredAreaKeys == preferences.preferredAreaKeys)
    }
}
