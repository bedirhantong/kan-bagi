import Foundation
import Combine
import Testing
@testable import donateblood

@MainActor
struct RequestDetailViewModelTests {
    private struct Fixture {
        let sut: RequestDetailViewModel
        let repo: FakeBloodRequestRepository
        let safety: FakeSafetyRepository
        let events: DataEvents
        let request: BloodRequest
    }

    private func makeFixture() -> Fixture {
        let request = TestData.request()
        let repo = FakeBloodRequestRepository()
        repo.detailResult = .success(request)
        repo.contacts = ["0532 000 00 00"]
        repo.pledgeCountValue = 3
        let safety = FakeSafetyRepository()
        let events = DataEvents()
        let sut = RequestDetailViewModel(
            getDetail: .init(requests: repo), togglePledge: .init(requests: repo), setActive: .init(requests: repo),
            deleteRequest: .init(requests: repo), reportContent: .init(safety: safety), blockUser: .init(safety: safety),
            events: events, requestId: request.id)
        return Fixture(sut: sut, repo: repo, safety: safety, events: events, request: request)
    }

    @Test func registeredUserSeesContactsAndPledgeState() async {
        let f = makeFixture()
        f.repo.hasPledgedValue = true
        await f.sut.load(isGuest: false)
        #expect(f.sut.state == .loaded)
        #expect(f.sut.detail?.phoneNumbers == ["0532 000 00 00"])
        #expect(f.sut.detail?.pledgeCount == 3)
        #expect(f.sut.detail?.hasPledged == true)
    }

    @Test func guestNeverRequestsContactNumbers() async {
        let f = makeFixture()
        await f.sut.load(isGuest: true)
        #expect(f.sut.detail?.phoneNumbers == nil)
        #expect(f.repo.contactRequests == 0)
    }

    @Test func loadFailureShowsErrorState() async {
        let f = makeFixture()
        f.repo.detailResult = .failure(AppError.notFound)
        await f.sut.load(isGuest: false)
        #expect(f.sut.state == .failed(.notFound))
    }

    @Test func pledgingCallsRepositoryAndThanksUser() async {
        let f = makeFixture()
        await f.sut.load(isGuest: false)
        await f.sut.setPledged(true, isGuest: false)
        #expect(f.repo.pledgeChanges.first?.1 == true)
        #expect(f.sut.infoMessage == .pledgeThanks)
    }

    @Test func pledgeFailureSurfacesError() async {
        let f = makeFixture()
        f.repo.mutationError = AppError.forbidden
        await f.sut.setPledged(true, isGuest: false)
        #expect(f.sut.error == .forbidden)
        #expect(f.sut.infoMessage == nil)
    }

    @Test func deletingBroadcastsChangeAndSignalsDismissal() async {
        let f = makeFixture()
        var notified = 0
        let cancellable = f.events.requestsChanged.sink { notified += 1 }
        await f.sut.delete()
        #expect(f.repo.deleted == [f.request.id])
        #expect(f.sut.didDelete)
        #expect(notified == 1)
        cancellable.cancel()
    }

    @Test func deactivatingUpdatesActivenessAndBroadcasts() async {
        let f = makeFixture()
        await f.sut.setActive(false, isGuest: false)
        #expect(f.repo.activeChanges.first?.1 == false)
    }

    @Test func reportingIsForwardedWithRequestId() async {
        let f = makeFixture()
        await f.sut.report(reason: "spam")
        #expect(f.safety.reports.first?.0 == .bloodRequest)
        #expect(f.safety.reports.first?.1 == f.request.id.uuidString)
        #expect(f.sut.infoMessage == .reported)
    }

    @Test func blockingOwnerBlocksAndDismisses() async {
        let f = makeFixture()
        await f.sut.load(isGuest: false)
        await f.sut.blockOwner()
        #expect(f.safety.blocked == [f.request.ownerId])
        #expect(f.sut.didDelete)
    }
}
