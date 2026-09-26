import Foundation
import Testing
@testable import donateblood

@MainActor
struct RequestPrefillTests {
    private func makeSUT(editing request: BloodRequest? = nil, prefill: RequestPrefill?) -> RequestFormViewModel {
        let requests = FakeBloodRequestRepository()
        return RequestFormViewModel(
            saveRequest: .init(requests: requests), getRequestDetail: .init(requests: requests),
            getHospitals: .init(hospitals: FakeHospitalRepository()), events: DataEvents(),
            editing: request, prefill: prefill)
    }

    @Test func shortcutsPrefillTheNewRequestForm() {
        let bloodType = makeSUT(prefill: RequestPrefill(bloodType: .oNegative))
        #expect(bloodType.draft.bloodType == .oNegative)
        #expect(!bloodType.draft.isEmergency)
        #expect(!bloodType.opensHospitalPicker)

        let urgent = makeSUT(prefill: RequestPrefill(isEmergency: true))
        #expect(urgent.draft.isEmergency)

        let hospital = makeSUT(prefill: RequestPrefill(opensHospitalPicker: true))
        #expect(hospital.opensHospitalPicker)
    }

    @Test func editingIgnoresPrefill() {
        let request = TestData.request(bloodType: .bPositive)
        let sut = makeSUT(editing: request, prefill: RequestPrefill(bloodType: .oNegative, isEmergency: true, opensHospitalPicker: true))
        #expect(sut.draft.bloodType == .bPositive)
        #expect(sut.draft.isEmergency == request.isEmergency)
        #expect(!sut.opensHospitalPicker)
    }

    @Test func eachPrefillIsANewPresentation() {
        // Aynı kısayola iki kez dokunmak formu iki kez açabilmeli (sheet(item:) kimliğe bakar).
        #expect(RequestPrefill.empty.id != RequestPrefill().id)
    }
}
