import Foundation
import Combine
import Testing
@testable import donateblood

@MainActor
struct HomeViewModelTests {
    private let antalya = Coordinate(latitude: 36.8969, longitude: 30.7133)

    private func makeSUT(
        _ repo: FakeBloodRequestRepository, location: FakeLocationProvider = FakeLocationProvider(),
        stories: [Story] = [], events: DataEvents = DataEvents()
    ) -> HomeViewModel {
        let storyRepo = FakeStoryRepository()
        storyRepo.result = stories
        return HomeViewModel(
            getFeed: .init(requests: repo, location: location), getStories: .init(stories: storyRepo), events: events)
    }

    @Test func reloadLoadsFirstPageUsingCurrentLocation() async {
        let repo = FakeBloodRequestRepository()
        repo.feedHandler = { _, page in TestData.requests(count: 3, startingAt: page.offset) }
        let sut = makeSUT(repo, location: FakeLocationProvider(antalya))
        await sut.reload()
        #expect(sut.state == .loaded)
        #expect(sut.requests.count == 3)
        #expect(sut.userLocation == antalya)
        #expect(repo.feedCalls.first?.0 == antalya)
        #expect(repo.feedCalls.first?.1 == PageRequest.first)
    }

    @Test func withoutLocationFeedIsRequestedWithoutCoordinate() async {
        let repo = FakeBloodRequestRepository()
        repo.feedHandler = { _, _ in TestData.requests(count: 2) }
        let sut = makeSUT(repo, location: FakeLocationProvider(nil))
        await sut.reload()
        #expect(sut.state == .loaded)
        #expect(sut.userLocation == nil)
        #expect(repo.feedCalls.first?.0 == nil)
    }

    @Test func failureWithEmptyListShowsErrorState() async {
        let repo = FakeBloodRequestRepository()
        repo.feedHandler = { _, _ in throw URLError(.notConnectedToInternet) }
        let sut = makeSUT(repo)
        await sut.reload()
        #expect(sut.state == .failed(.network))
    }

    @Test func failureWithExistingItemsKeepsListAndSurfacesAlert() async {
        let repo = FakeBloodRequestRepository()
        repo.feedHandler = { _, _ in TestData.requests(count: 2) }
        let sut = makeSUT(repo)
        await sut.reload()
        repo.feedHandler = { _, _ in throw AppError.server("boom") }
        await sut.reload()
        #expect(sut.state == .loaded)
        #expect(sut.requests.count == 2)
        #expect(sut.error == .server("boom"))
    }

    @Test func paginationReusesLocationAppendsWithoutDuplicatesAndStopsOnShortPage() async {
        let repo = FakeBloodRequestRepository()
        let location = FakeLocationProvider(antalya)
        let firstPage = TestData.requests(count: 20)
        var secondPage = TestData.requests(count: 4, startingAt: 20)
        secondPage.append(firstPage[0]) // yinelenen kayıt
        repo.feedHandler = { _, page in page.offset == 0 ? firstPage : secondPage }
        let sut = makeSUT(repo, location: location)

        await sut.reload()
        await sut.loadMoreIfNeeded(current: sut.requests.last!)
        #expect(sut.requests.count == 24) // 20 + 4 (yinelenen atıldı)
        #expect(location.requests == 1) // ikinci sayfada konum yeniden sorulmadı
        #expect(repo.feedCalls.last?.0 == antalya)

        let calls = repo.feedCalls.count
        await sut.loadMoreIfNeeded(current: sut.requests.last!) // kısa sayfa -> daha fazla yok
        #expect(repo.feedCalls.count == calls)
    }

    @Test func loadMoreIgnoresRowsThatAreNotLast() async {
        let repo = FakeBloodRequestRepository()
        repo.feedHandler = { _, _ in TestData.requests(count: 20) }
        let sut = makeSUT(repo)
        await sut.reload()
        let calls = repo.feedCalls.count
        await sut.loadMoreIfNeeded(current: sut.requests[0])
        #expect(repo.feedCalls.count == calls)
    }

    @Test func reloadRefreshesLocation() async {
        let repo = FakeBloodRequestRepository()
        let location = FakeLocationProvider(antalya)
        let sut = makeSUT(repo, location: location)
        await sut.reload()
        location.coordinate = Coordinate(latitude: 41.0, longitude: 29.0)
        await sut.reload()
        #expect(sut.userLocation == Coordinate(latitude: 41.0, longitude: 29.0))
    }

    @Test func storiesLoadOnce() async {
        let story = Story(id: UUID(), title: "Haber", body: nil, imageURL: nil, logoURL: nil, linkURL: nil)
        let sut = makeSUT(FakeBloodRequestRepository(), stories: [story])
        await sut.loadStories()
        #expect(sut.stories == [story])
    }

    @Test func requestsChangedEventTriggersReload() async {
        let repo = FakeBloodRequestRepository()
        repo.feedHandler = { _, _ in TestData.requests(count: 1) }
        let events = DataEvents()
        let sut = makeSUT(repo, events: events)
        events.requestsChanged.send()
        let reloaded = await waitUntil { sut.state == .loaded }
        #expect(reloaded)
    }
}

struct CoordinateAndDistanceTests {
    @Test func haversineDistanceMatchesKnownValue() {
        // İstanbul (Fatih) - Ankara (Çankaya): yaklaşık 350 km
        let istanbul = Coordinate(latitude: 41.0128, longitude: 28.9440)
        let ankara = Coordinate(latitude: 39.9337, longitude: 32.8115)
        let km = istanbul.distance(to: ankara) / 1000
        #expect((340...360).contains(km))
        #expect(istanbul.distance(to: istanbul) == 0)
    }

    @Test func requestDistanceUsesHospitalCoordinate() {
        let user = Coordinate(latitude: 36.9, longitude: 30.7)
        let request = TestData.request()
        let meters = request.distance(from: user)
        #expect(meters != nil)
        #expect(meters! < 5_000) // test hastanesi kullanıcıya yakın

        #expect(request.distance(from: nil) == nil)
        var noCoordinates = request
        noCoordinates.hospital = Hospital(id: 2, name: "?", address: nil, phoneNumber: nil, email: nil, website: nil,
                                          latitude: nil, longitude: nil, cityName: nil, districtName: nil, iconURL: nil)
        #expect(noCoordinates.distance(from: user) == nil)
    }

    @Test func distanceFormattingUsesReadableUnits() {
        let tr = Locale(identifier: "tr_TR")
        #expect(DistanceFormatting.string(meters: 850, locale: tr).contains("850"))
        #expect(DistanceFormatting.string(meters: 3_240, locale: tr).contains("3,2"))
    }
}

@MainActor
struct FeedUseCaseTests {
    @Test func firstPageAsksLocationSubsequentPagesReuseIt() async throws {
        let repo = FakeBloodRequestRepository()
        let location = FakeLocationProvider(Coordinate(latitude: 1, longitude: 2))
        let useCase = GetBloodRequestFeedUseCase(requests: repo, location: location)

        let first = try await useCase(page: .first)
        #expect(first.location == Coordinate(latitude: 1, longitude: 2))
        _ = try await useCase(page: PageRequest.first.next(), reusing: first.location)
        #expect(location.requests == 1)
        #expect(repo.feedCalls.count == 2)
    }
}
