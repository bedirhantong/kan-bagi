import Foundation

@MainActor
final class HealthFormViewModel: ObservableObject {
    @Published var answers: HealthAnswers = Dictionary(uniqueKeysWithValues: HealthCatalog.questions.map { ($0.key, false) })
    @Published private(set) var state: LoadState = .idle
    @Published private(set) var isSaving = false
    @Published private(set) var didSave = false
    @Published var error: AppError?

    private let getForm: GetHealthFormUseCase
    private let submitForm: SubmitHealthFormUseCase

    init(
        getForm: GetHealthFormUseCase,
        submitForm: SubmitHealthFormUseCase
    ) {
        self.getForm = getForm
        self.submitForm = submitForm
    }

    var flagged: [HealthQuestion] { HealthFormEvaluator.flaggedQuestions(for: answers) }

    func binding(for key: String) -> Bool {
        answers[key] ?? false
    }

    func load() async {
        guard state != .loaded else { return }
        state = .loading
        do {
            if let saved = try await getForm() {
                answers.merge(saved) { _, new in new }
            }
            state = .loaded
        } catch {
            let appError = AppError.from(error)
            if appError != .cancelled { state = .failed(appError) }
        }
    }

    func save() async {
        isSaving = true
        defer { isSaving = false }
        do {
            try await submitForm(answers)
            didSave = true
        } catch { self.error = AppError.from(error) }
    }
}
