import Foundation

/// Ana sayfa akışı: konumu alır, yakınlığa göre sıralı sayfayı döndürür.
struct GetBloodRequestFeedUseCase: Sendable {
    struct Result: Sendable {
        let requests: [BloodRequest]
        let location: Coordinate?
    }

    let requests: BloodRequestRepository
    let location: LocationProvider

    /// `reusing` verilirse (sonraki sayfalar) konum yeniden sorulmaz.
    func callAsFunction(page: PageRequest, reusing coordinate: Coordinate? = nil) async throws -> Result {
        let current: Coordinate?
        if let coordinate { current = coordinate } else { current = await location.currentCoordinate() }
        return Result(requests: try await requests.feed(near: current, page: page), location: current)
    }
}

/// Bir hastaneye ait aktif ilanlar (harita kartı).
struct GetBloodRequestsUseCase: Sendable {
    let requests: BloodRequestRepository
    func callAsFunction(filter: BloodRequestFilter, page: PageRequest) async throws -> [BloodRequest] {
        try await requests.requests(filter: filter, page: page)
    }
}

struct GetMyBloodRequestsUseCase: Sendable {
    let requests: BloodRequestRepository
    func callAsFunction() async throws -> [BloodRequest] { try await requests.myRequests() }
}

struct GetMyPledgedRequestsUseCase: Sendable {
    let requests: BloodRequestRepository
    func callAsFunction() async throws -> [BloodRequest] { try await requests.pledgedRequests() }
}

struct GetBloodRequestDetailUseCase: Sendable {
    struct Detail: Sendable {
        let request: BloodRequest
        let pledgeCount: Int
        let hasPledged: Bool
        /// Misafirler için `nil`; sunucu tarafında da RLS ile engellenir.
        let phoneNumbers: [String]?
    }

    let requests: BloodRequestRepository

    func callAsFunction(id: UUID, includeContacts: Bool) async throws -> Detail {
        async let request = requests.request(id: id)
        async let count = requests.pledgeCount(requestId: id)
        async let pledged = includeContacts ? requests.hasPledged(requestId: id) : false
        async let phones: [String]? = includeContacts ? requests.contactNumbers(requestId: id) : nil
        return try await Detail(request: request, pledgeCount: count, hasPledged: pledged, phoneNumbers: phones)
    }
}

struct SaveBloodRequestUseCase: Sendable {
    let requests: BloodRequestRepository

    /// `id == nil` ise oluşturur, değilse günceller.
    @discardableResult
    func callAsFunction(id: UUID?, draft: BloodRequestDraft) async throws -> UUID {
        let clean = try Self.validated(draft)
        if id == nil, !clean.acceptedTerms { throw AppError.invalidInput(L10n.string("request.terms")) }
        if let id {
            try await requests.update(id: id, draft: clean)
            return id
        }
        return try await requests.create(clean)
    }

    static func validated(_ draft: BloodRequestDraft) throws -> BloodRequestDraft {
        var draft = draft
        draft.patientFullName = draft.patientFullName.trimmingCharacters(in: .whitespacesAndNewlines)
        draft.title = draft.title.trimmingCharacters(in: .whitespacesAndNewlines)
        draft.description = draft.description.trimmingCharacters(in: .whitespacesAndNewlines)
        draft.phoneNumbers = draft.phoneNumbers
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty }

        guard Validators.isValidName(draft.patientFullName) else { throw AppError.invalidInput(L10n.string("request.patientName")) }
        guard draft.bloodType != nil else { throw AppError.invalidInput(L10n.string("profile.bloodType")) }
        guard draft.title.count >= 3 else { throw AppError.invalidInput(L10n.string("request.titleField")) }
        if let age = draft.patientAge, !Validators.isValidAge(age) { throw AppError.invalidInput(L10n.string("request.patientAge")) }
        guard draft.hospitalId != nil else { throw AppError.invalidInput(L10n.string("request.hospital")) }
        guard (1...3).contains(draft.phoneNumbers.count), draft.phoneNumbers.allSatisfy(Validators.isValidPhone) else {
            throw AppError.invalidInput(L10n.string("request.phones"))
        }
        return draft
    }
}

struct SetBloodRequestActiveUseCase: Sendable {
    let requests: BloodRequestRepository
    func callAsFunction(id: UUID, isActive: Bool) async throws { try await requests.setActive(id: id, isActive: isActive) }
}

struct DeleteBloodRequestUseCase: Sendable {
    let requests: BloodRequestRepository
    func callAsFunction(id: UUID) async throws { try await requests.delete(id: id) }
}

/// "Bağış yapmak istiyorum" niyet bildirimi (doğrulama yok, ilan sahibine bildirim gider).
struct TogglePledgeUseCase: Sendable {
    let requests: BloodRequestRepository
    func callAsFunction(requestId: UUID, pledged: Bool) async throws { try await requests.setPledge(requestId: requestId, pledged: pledged) }
}
