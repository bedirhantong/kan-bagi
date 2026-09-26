import Foundation

/// Yeni ilan formunu bir kısayoldan açarken önceden seçilecekler (ör. paylaşma alanındaki kan grubu menüsü).
struct RequestPrefill: Identifiable, Equatable, Sendable {
    let id = UUID()
    var bloodType: BloodType?
    var isEmergency = false
    /// Form açılınca hastane seçimi de açılsın.
    var opensHospitalPicker = false

    static let empty = RequestPrefill()
}

@MainActor
final class RequestFormViewModel: ObservableObject {
    @Published var draft: BloodRequestDraft
    @Published var ageText: String
    @Published var selectedHospital: Hospital?
    @Published private(set) var isSaving = false
    @Published private(set) var didSave = false
    @Published var error: AppError?

    let editingId: UUID?
    /// Form açılınca hastane seçimini de aç (yalnızca yeni ilan kısayolundan).
    let opensHospitalPicker: Bool
    private let saveRequest: SaveBloodRequestUseCase
    private let getRequestDetail: GetBloodRequestDetailUseCase
    private let events: DataEvents
    let getHospitals: GetHospitalsUseCase

    var isEditing: Bool { editingId != nil }

    init(
        saveRequest: SaveBloodRequestUseCase,
        getRequestDetail: GetBloodRequestDetailUseCase,
        getHospitals: GetHospitalsUseCase,
        events: DataEvents,
        editing request: BloodRequest?,
        prefill: RequestPrefill? = nil
    ) {
        self.saveRequest = saveRequest
        self.getRequestDetail = getRequestDetail
        self.getHospitals = getHospitals
        self.events = events
        editingId = request?.id
        var draft = BloodRequestDraft()
        if let request {
            draft.patientFullName = request.patientFullName
            draft.patientAge = request.patientAge
            draft.title = request.title
            draft.description = request.description ?? ""
            draft.bloodType = request.bloodType
            draft.hospitalId = request.hospital?.id
            draft.isEmergency = request.isEmergency
            draft.acceptedTerms = true
        } else if let prefill {
            if let bloodType = prefill.bloodType { draft.bloodType = bloodType }
            draft.isEmergency = prefill.isEmergency
        }
        self.draft = draft
        opensHospitalPicker = request == nil && prefill?.opensHospitalPicker == true
        ageText = request?.patientAge.map(String.init) ?? ""
        selectedHospital = request?.hospital
    }

    /// Düzenlemede telefon numaraları ayrı tabloda olduğundan yüklenir.
    func loadContactsIfEditing() async {
        guard let editingId else { return }
        if let detail = try? await getRequestDetail(id: editingId, includeContacts: true),
           let phones = detail.phoneNumbers, !phones.isEmpty {
            draft.phoneNumbers = phones
        }
    }

    func addPhone() { if draft.phoneNumbers.count < 3 { draft.phoneNumbers.append("") } }
    func removePhone(at offsets: IndexSet) {
        draft.phoneNumbers.remove(atOffsets: offsets)
        if draft.phoneNumbers.isEmpty { draft.phoneNumbers = [""] }
    }

    func select(_ hospital: Hospital) {
        selectedHospital = hospital
        draft.hospitalId = hospital.id
    }

    func save() async {
        guard !isSaving else { return }
        isSaving = true
        defer { isSaving = false }
        draft.patientAge = Int(ageText.trimmingCharacters(in: .whitespaces))
        do {
            try await saveRequest(id: editingId, draft: draft)
            events.requestsChanged.send()
            didSave = true
        } catch { self.error = AppError.from(error) }
    }
}
