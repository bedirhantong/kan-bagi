import Foundation
import Testing
@testable import donateblood

@MainActor
struct ChatDetailViewModelTests {
    private func makeSUT(_ repo: FakeChatRepository, target: ChatTarget, safety: FakeSafetyRepository = FakeSafetyRepository()) -> ChatDetailViewModel {
        ChatDetailViewModel(
            openRoom: .init(chat: repo), getMessages: .init(chat: repo), sendMessage: .init(chat: repo),
            observeMessages: .init(chat: repo), reportContent: .init(safety: safety), blockUser: .init(safety: safety),
            target: target)
    }

    @Test func userTargetOpensRoomAndLoadsHistoryOldestFirst() async {
        let repo = FakeChatRepository()
        repo.history = [TestData.message(content: "ikinci", at: 10), TestData.message(content: "ilk", at: 0)]
        let userId = UUID()
        let sut = makeSUT(repo, target: .user(id: userId, name: "Ayşe"))
        await sut.start()
        sut.stop()
        #expect(repo.openedWith == [userId])
        #expect(sut.state == .loaded)
        #expect(sut.messages.map(\.content) == ["ilk", "ikinci"])
    }

    @Test func roomTargetDoesNotCreateRoom() async {
        let repo = FakeChatRepository()
        let sut = makeSUT(repo, target: .room(id: UUID(), userId: UUID(), name: "Ayşe"))
        await sut.start()
        sut.stop()
        #expect(repo.openedWith.isEmpty)
    }

    @Test func sendTrimsClearsDraftAndFailureRestoresIt() async {
        let repo = FakeChatRepository()
        let sut = makeSUT(repo, target: .room(id: UUID(), userId: UUID(), name: "Ayşe"))
        await sut.start()

        sut.draft = "  selam  "
        await sut.send()
        #expect(repo.sent == ["selam"])
        #expect(sut.draft.isEmpty)

        repo.sendError = AppError.network
        sut.draft = "tekrar"
        await sut.send()
        #expect(sut.draft == "tekrar")
        #expect(sut.error == .network)
        sut.stop()
    }

    @Test func blankDraftIsNotSent() async {
        let repo = FakeChatRepository()
        let sut = makeSUT(repo, target: .room(id: UUID(), userId: UUID(), name: "Ayşe"))
        await sut.start()
        sut.draft = "   "
        await sut.send()
        #expect(repo.sent.isEmpty)
        sut.stop()
    }

    @Test func realtimeMessagesAreMergedWithoutDuplicates() async {
        let repo = FakeChatRepository()
        let existing = TestData.message(content: "eski", at: 0)
        repo.history = [existing]
        let sut = makeSUT(repo, target: .room(id: existing.roomId, userId: UUID(), name: "Ayşe"))
        await sut.start()
        _ = await waitUntil { sut.isLive }

        let incoming = TestData.message(roomId: existing.roomId, content: "yeni", at: 5)
        repo.push(incoming)
        repo.push(incoming) // aynı mesaj iki kez gelirse tek görünmeli
        repo.push(existing)
        let merged = await waitUntil { sut.messages.count == 2 }
        #expect(merged)
        try? await Task.sleep(for: .milliseconds(100))
        #expect(sut.messages.map(\.content) == ["eski", "yeni"])
        sut.stop()
    }

    @Test func blockingRequiresKnownUser() async {
        let safety = FakeSafetyRepository()
        let repo = FakeChatRepository()
        let unknown = makeSUT(repo, target: .room(id: UUID(), userId: nil, name: "?"), safety: safety)
        await unknown.block()
        #expect(safety.blocked.isEmpty)

        let userId = UUID()
        let known = makeSUT(repo, target: .room(id: UUID(), userId: userId, name: "Ayşe"), safety: safety)
        await known.block()
        #expect(safety.blocked == [userId])
        #expect(known.didBlock)
    }
}
