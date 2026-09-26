import Foundation
import Testing
@testable import donateblood

@MainActor
struct NotificationSettingsViewModelTests {
    private func makeSUT(_ repo: FakeNotificationRepository, hospitals: [Hospital] = [], push: FakePush) -> NotificationSettingsViewModel {
        let hospitalRepo = FakeHospitalRepository()
        hospitalRepo.all = hospitals
        return NotificationSettingsViewModel(
            getPreferences: .init(notifications: repo), updatePreferences: .init(notifications: repo),
            getHospitalsByIds: .init(hospitals: hospitalRepo), getHospitals: .init(hospitals: hospitalRepo),
            getAreas: .init(notifications: repo),
            suggestArea: .init(location: FakeLocationProvider(), hospitals: hospitalRepo),
            push: push)
    }

    @Test func missingScopeIsFlaggedAndAreasAreNamed() async {
        let repo = FakeNotificationRepository()
        let sut = makeSUT(repo, push: FakePush())
        await sut.load()
        #expect(sut.isMissingScope) // bölge yok: ilan bildirimi gelmez, kullanıcı uyarılır

        repo.stored.preferredAreaKeys = ["Antalya/Muratpaşa"]
        await sut.load()
        #expect(!sut.isMissingScope)
        #expect(sut.selectedAreas.map(\.district) == ["Muratpaşa"])
        #expect(sut.hospitalLimit == NotificationScope.maxSelections - 1)

        await sut.update { $0.bloodTypeAlerts = false }
        repo.stored.preferredAreaKeys = []
        await sut.load()
        #expect(!sut.isMissingScope) // ilan bildirimi kapalıysa uyarı gereksiz
    }

    @Test func areaPickerPersistsEachChangeAndRollsBackOnFailure() async throws {
        let repo = FakeNotificationRepository()
        let sut = makeSUT(repo, push: FakePush())
        await sut.load()
        let picker = sut.makeAreaPicker()
        await picker.load()

        await picker.toggle(try #require(picker.catalog.area(forKey: "Ankara")))
        #expect(repo.stored.preferredAreaKeys == ["Ankara"])
        #expect(sut.preferences.preferredAreaKeys == ["Ankara"])

        repo.updateError = AppError.network
        await picker.toggle(try #require(picker.catalog.area(forKey: "İstanbul")))
        #expect(picker.selectedKeys == ["Ankara"]) // geri alındı
        #expect(sut.error == .network)
    }

    @Test func loadReadsPreferencesAndSelectedHospitals() async {
        let repo = FakeNotificationRepository()
        repo.stored.preferredHospitalIds = [TestData.hospital.id]
        repo.stored.preferredBloodTypes = [.aPositive]
        let sut = makeSUT(repo, hospitals: [TestData.hospital], push: FakePush())
        await sut.load()
        #expect(sut.state == .loaded)
        #expect(sut.selectedHospitals == [TestData.hospital])
        #expect(sut.preferences.preferredBloodTypes == [.aPositive])
    }

    @Test func updateIsPersistedAndAsksForPushPermission() async {
        let repo = FakeNotificationRepository()
        let push = FakePush()
        let sut = makeSUT(repo, push: push)
        await sut.load()
        await sut.update { $0.chatNotifications = false }
        #expect(repo.stored.chatNotifications == false)
        #expect(push.permissionRequests == 1)
    }

    @Test func failedUpdateIsRolledBack() async {
        let repo = FakeNotificationRepository()
        let sut = makeSUT(repo, push: FakePush())
        await sut.load()
        repo.updateError = AppError.network
        await sut.update { $0.bloodTypeAlerts = false }
        #expect(sut.preferences.bloodTypeAlerts == true)
        #expect(sut.error == .network)
    }

    @Test func unchangedValueDoesNotHitTheNetwork() async {
        let repo = FakeNotificationRepository()
        let sut = makeSUT(repo, push: FakePush())
        await sut.load()
        repo.updateError = AppError.network // çağrılsaydı hata üretirdi
        await sut.update { $0.pushEnabled = true } // zaten true
        #expect(sut.error == nil)
    }

    @Test func settingHospitalsStoresIdsAndRollsBackOnFailure() async {
        let repo = FakeNotificationRepository()
        let sut = makeSUT(repo, push: FakePush())
        await sut.load()
        await sut.setHospitals([TestData.hospital])
        #expect(repo.stored.preferredHospitalIds == [TestData.hospital.id])
        #expect(sut.selectedHospitals == [TestData.hospital])

        repo.updateError = AppError.network
        await sut.setHospitals([])
        #expect(sut.selectedHospitals == [TestData.hospital]) // geri alındı
    }
}
