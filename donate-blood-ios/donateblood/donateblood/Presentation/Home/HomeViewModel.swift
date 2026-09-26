import Foundation
import Combine

/// Ana sayfa: tüm kan grupları, kullanıcıya en yakın hastaneden en uzağa (konum yoksa en yeniden eskiye).
/// Arama/filtre yoktur; bildirim tercihleri (kan grubu, hastane) yalnızca bildirimleri etkiler.
@MainActor
final class HomeViewModel: ObservableObject {
    @Published private(set) var requests: [BloodRequest] = []
    @Published private(set) var state: LoadState = .idle
    @Published private(set) var isLoadingMore = false
    @Published private(set) var stories: [Story] = []
    /// Sıralamada kullanılan konum; kartlarda mesafe göstermek için de kullanılır.
    @Published private(set) var userLocation: Coordinate?
    @Published var error: AppError?

    private let getFeed: GetBloodRequestFeedUseCase
    private let getStories: GetStoriesUseCase
    private var page = PageRequest.first
    private var canLoadMore = true
    private var cancellables = Set<AnyCancellable>()

    init(
        getFeed: GetBloodRequestFeedUseCase,
        getStories: GetStoriesUseCase,
        events: DataEvents
    ) {
        self.getFeed = getFeed
        self.getStories = getStories
        events.requestsChanged
            .sink { [weak self] in Task { await self?.reload() } }
            .store(in: &cancellables)
    }

    func loadStories() async {
        guard stories.isEmpty else { return }
        stories = (try? await getStories()) ?? []
    }

    /// İlk sayfayı yeniden yükler (konum da yenilenir).
    func reload() async {
        page = .first
        canLoadMore = true
        if requests.isEmpty { state = .loading }
        do {
            let result = try await getFeed(page: page)
            guard !Task.isCancelled else { return }
            userLocation = result.location
            requests = result.requests
            canLoadMore = result.requests.count == page.limit
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            guard appError != .cancelled else { return }
            if requests.isEmpty { state = .failed(appError) } else { self.error = appError }
        }
    }

    func loadMoreIfNeeded(current: BloodRequest) async {
        guard canLoadMore, !isLoadingMore, requests.last?.id == current.id else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        let next = page.next()
        do {
            // Aynı konumla devam edilir; sayfalar arasında sıra tutarlı kalır.
            let result = try await getFeed(page: next, reusing: userLocation)
            let known = Set(requests.map(\.id))
            requests += result.requests.filter { !known.contains($0.id) }
            canLoadMore = result.requests.count == next.limit
            page = next
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { self.error = appError }
        }
    }
}
