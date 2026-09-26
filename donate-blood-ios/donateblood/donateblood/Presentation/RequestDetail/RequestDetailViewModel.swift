import Foundation

@MainActor
final class RequestDetailViewModel: ObservableObject {
    @Published private(set) var detail: GetBloodRequestDetailUseCase.Detail?
    @Published private(set) var state: LoadState = .idle
    @Published private(set) var isWorking = false
    @Published var didDelete = false
    @Published var error: AppError?
    @Published var infoMessage: InfoMessage?

    let requestId: UUID
    private let getDetail: GetBloodRequestDetailUseCase
    private let togglePledge: TogglePledgeUseCase
    private let setActive: SetBloodRequestActiveUseCase
    private let deleteRequest: DeleteBloodRequestUseCase
    private let reportContent: ReportContentUseCase
    private let blockUser: BlockUserUseCase
    private let events: DataEvents

    init(
        getDetail: GetBloodRequestDetailUseCase,
        togglePledge: TogglePledgeUseCase,
        setActive: SetBloodRequestActiveUseCase,
        deleteRequest: DeleteBloodRequestUseCase,
        reportContent: ReportContentUseCase,
        blockUser: BlockUserUseCase,
        events: DataEvents,
        requestId: UUID
    ) {
        self.getDetail = getDetail
        self.togglePledge = togglePledge
        self.setActive = setActive
        self.deleteRequest = deleteRequest
        self.reportContent = reportContent
        self.blockUser = blockUser
        self.events = events
        self.requestId = requestId
    }

    func load(isGuest: Bool) async {
        if detail == nil { state = .loading }
        do {
            detail = try await getDetail(id: requestId, includeContacts: !isGuest)
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            guard appError != .cancelled else { return }
            if detail == nil { state = .failed(appError) } else { self.error = appError }
        }
    }

    func setPledged(_ pledged: Bool, isGuest: Bool) async {
        await perform {
            try await self.togglePledge(requestId: self.requestId, pledged: pledged)
            await self.load(isGuest: isGuest)
            if pledged { self.infoMessage = .pledgeThanks }
        }
    }

    func setActive(_ isActive: Bool, isGuest: Bool) async {
        await perform {
            try await self.setActive(id: self.requestId, isActive: isActive)
            self.events.requestsChanged.send()
            await self.load(isGuest: isGuest)
        }
    }

    func delete() async {
        await perform {
            try await self.deleteRequest(id: self.requestId)
            self.events.requestsChanged.send()
            self.didDelete = true
        }
    }

    func report(reason: String) async {
        await perform {
            try await self.reportContent(target: .bloodRequest, targetId: self.requestId.uuidString, reason: reason)
            self.infoMessage = .reported
        }
    }

    func blockOwner() async {
        guard let owner = detail?.request.ownerId else { return }
        await perform {
            try await self.blockUser(userId: owner)
            self.events.requestsChanged.send()
            self.didDelete = true
        }
    }

    private func perform(_ work: () async throws -> Void) async {
        guard !isWorking else { return }
        isWorking = true
        defer { isWorking = false }
        do { try await work() } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }
}

/// Bilgilendirme mesajları (alert) için yerelleştirilmiş anahtarlar.
enum InfoMessage: Identifiable {
    case pledgeThanks, reported, blocked
    var id: Self { self }
    var text: String {
        switch self {
        case .pledgeThanks: return L10n.string("detail.pledgeThanks")
        case .reported: return L10n.string("safety.reported")
        case .blocked: return L10n.string("safety.blocked")
        }
    }
}
