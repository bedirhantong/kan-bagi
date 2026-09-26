import Foundation
import UIKit
import CoreGraphics
import Testing
@testable import donateblood
import Supabase

// MARK: - Sağlık formu

struct HealthFormEvaluatorTests {
    private func allAnswered(_ value: Bool) -> HealthAnswers {
        Dictionary(uniqueKeysWithValues: HealthCatalog.questions.map { ($0.key, value) })
    }

    @Test func catalogHasFortyFiveUniqueQuestions() {
        #expect(HealthCatalog.questions.count == 45)
        #expect(Set(HealthCatalog.questions.map(\.key)).count == 45)
    }

    @Test func positiveAnswersAreNotFlagged() {
        var answers = allAnswered(false)
        for key in ["ONAM_FORMU_OKUNDUMU", "SAGLIKLI_MI", "UYKU", "KAHVALTI"] { answers[key] = true }
        #expect(HealthFormEvaluator.flaggedQuestions(for: answers).isEmpty)
    }

    @Test func riskAnswerIsFlagged() {
        var answers = allAnswered(false)
        for key in ["ONAM_FORMU_OKUNDUMU", "SAGLIKLI_MI", "UYKU", "KAHVALTI"] { answers[key] = true }
        answers["ALKOL"] = true
        #expect(HealthFormEvaluator.flaggedQuestions(for: answers).map(\.key) == ["ALKOL"])
    }

    @Test func unansweredPositiveQuestionsAreNotFlaggedButIncomplete() {
        #expect(HealthFormEvaluator.flaggedQuestions(for: [:]).isEmpty)
        #expect(!HealthFormEvaluator.isComplete([:]))
        #expect(HealthFormEvaluator.isComplete(allAnswered(false)))
    }

    @Test func informationalQuestionsNeverFlag() {
        var answers = allAnswered(false)
        for key in ["ONAM_FORMU_OKUNDUMU", "SAGLIKLI_MI", "UYKU", "KAHVALTI", "SIGARA", "ADET_DONEMI"] { answers[key] = true }
        #expect(HealthFormEvaluator.flaggedQuestions(for: answers).isEmpty)
    }
}

// MARK: - Doğrulayıcılar

struct ValidatorsTests {
    @Test func phoneNumbers() {
        #expect(Validators.isValidPhone("+90 532 123 45 67"))
        #expect(Validators.isValidPhone("05321234567"))
        #expect(!Validators.isValidPhone("12345"))
        #expect(!Validators.isValidPhone("abc"))
    }

    @Test func names() {
        #expect(Validators.isValidName("Ali"))
        #expect(!Validators.isValidName(" a "))
        #expect(!Validators.isValidName(String(repeating: "a", count: 61)))
    }

    @Test func adulthood() {
        let now = Date(timeIntervalSince1970: 1_800_000_000)
        let calendar = Calendar(identifier: .gregorian)
        let eighteen = calendar.date(byAdding: .year, value: -18, to: now)!
        let seventeen = calendar.date(byAdding: .year, value: -17, to: now)!
        #expect(Validators.isAdult(birthDate: eighteen, now: now, calendar: calendar))
        #expect(!Validators.isAdult(birthDate: seventeen, now: now, calendar: calendar))
    }
}

// MARK: - Use case'ler (sahte repository ile)

struct SaveBloodRequestUseCaseTests {
    private func validDraft() -> BloodRequestDraft {
        var draft = BloodRequestDraft()
        draft.patientFullName = "  Ayşe Yılmaz "
        draft.title = "Acil A+ kan"
        draft.bloodType = .aPositive
        draft.hospitalId = 1
        draft.phoneNumbers = ["0532 123 45 67", "  "]
        draft.acceptedTerms = true
        return draft
    }

    @Test func bloodTypeIsNotPreselectedAndIsRequired() async {
        #expect(BloodRequestDraft().bloodType == nil)
        var draft = validDraft()
        draft.bloodType = nil
        let repo = FakeBloodRequestRepository()
        await #expect(throws: AppError.self) { try await SaveBloodRequestUseCase(requests: repo)(id: nil, draft: draft) }
        #expect(repo.created.isEmpty)
    }

    @Test func createTrimsAndDropsEmptyPhones() async throws {
        let repo = FakeBloodRequestRepository()
        let id = try await SaveBloodRequestUseCase(requests: repo)(id: nil, draft: validDraft())
        #expect(id == repo.newId)
        #expect(repo.created.count == 1)
        #expect(repo.created[0].patientFullName == "Ayşe Yılmaz")
        #expect(repo.created[0].phoneNumbers == ["0532 123 45 67"])
    }

    @Test func existingIdUpdatesInsteadOfCreating() async throws {
        let repo = FakeBloodRequestRepository()
        let existing = UUID()
        _ = try await SaveBloodRequestUseCase(requests: repo)(id: existing, draft: validDraft())
        #expect(repo.created.isEmpty)
        #expect(repo.updated.first?.0 == existing)
    }

    @Test func newRequestWithoutAcceptedTermsIsRejected() async {
        var draft = validDraft()
        draft.acceptedTerms = false
        await #expect(throws: AppError.self) {
            try await SaveBloodRequestUseCase(requests: FakeBloodRequestRepository())(id: nil, draft: draft)
        }
    }

    @Test func editingDoesNotRequireTermsAgain() async throws {
        var draft = validDraft()
        draft.acceptedTerms = false
        _ = try await SaveBloodRequestUseCase(requests: FakeBloodRequestRepository())(id: UUID(), draft: draft)
    }

    @Test func missingHospitalIsRejected() async {
        var draft = validDraft()
        draft.hospitalId = nil
        await #expect(throws: AppError.self) {
            try await SaveBloodRequestUseCase(requests: FakeBloodRequestRepository())(id: nil, draft: draft)
        }
    }

    @Test func noPhoneIsRejected() async {
        var draft = validDraft()
        draft.phoneNumbers = [""]
        await #expect(throws: AppError.self) {
            try await SaveBloodRequestUseCase(requests: FakeBloodRequestRepository())(id: nil, draft: draft)
        }
    }

    @Test func moreThanThreePhonesIsRejected() async {
        var draft = validDraft()
        draft.phoneNumbers = Array(repeating: "0532 123 45 67", count: 4)
        await #expect(throws: AppError.self) {
            try await SaveBloodRequestUseCase(requests: FakeBloodRequestRepository())(id: nil, draft: draft)
        }
    }
}

// MARK: - Profil

struct CompleteProfileUseCaseTests {
    private func update(birthYearsAgo: Int, bloodType: BloodType? = .oPositive) -> ProfileUpdate {
        ProfileUpdate(name: "Ali", surname: "Veli", phoneNumber: nil,
                      birthDate: Calendar.current.date(byAdding: .year, value: -birthYearsAgo, to: .now),
                      bloodType: bloodType, gender: .male)
    }

    @Test func adultWithBloodTypeCompletesProfile() async throws {
        let repo = FakeProfileRepository()
        let profile = try await CompleteProfileUseCase(profiles: repo)(update(birthYearsAgo: 30))
        #expect(profile.isProfileCompleted)
        #expect(repo.lastMarkCompleted == true)
    }

    @Test func minorIsRejected() async {
        await #expect(throws: AppError.self) {
            try await CompleteProfileUseCase(profiles: FakeProfileRepository())(update(birthYearsAgo: 16))
        }
    }

    @Test func unknownBloodTypeIsAllowed() async throws {
        let profile = try await CompleteProfileUseCase(profiles: FakeProfileRepository())(update(birthYearsAgo: 30, bloodType: nil))
        #expect(profile.isProfileCompleted)
        #expect(profile.bloodType == nil)
    }

    @Test func missingBirthDateIsRejected() async {
        let noBirthDate = ProfileUpdate(name: "Ali", surname: "Veli", phoneNumber: nil, birthDate: nil, bloodType: .aPositive, gender: nil)
        await #expect(throws: AppError.self) {
            try await CompleteProfileUseCase(profiles: FakeProfileRepository())(noBirthDate)
        }
    }
}

// MARK: - Derin bağlantı ve hata eşleme

struct DeepLinkTests {
    @Test func parsesRequestAndRoom() {
        let id = UUID()
        #expect(DeepLink(userInfo: ["request_id": id.uuidString]) == .request(id))
        #expect(DeepLink(userInfo: ["room_id": id.uuidString]) == .room(id))
        #expect(DeepLink(userInfo: ["other": "x"]) == nil)
    }
}

struct AppErrorTests {
    @Test func urlErrorsMapToNetworkOrCancelled() {
        #expect(AppError.from(URLError(.notConnectedToInternet)) == .network)
        #expect(AppError.from(URLError(.cancelled)) == .cancelled)
        #expect(AppError.from(CancellationError()) == .cancelled)
    }

    @Test func placeholderConfigIsDetected() {
        let config = AppConfig(supabaseURL: URL(string: "https://YOUR-PROJECT-REF.supabase.co")!, supabaseAnonKey: "k")
        #expect(config.isPlaceholder)
    }
}

// MARK: - Bildirim tercihleri ve SSS

struct NotificationPreferencesMappingTests {
    @Test func dtoRoundTripKeepsSelections() {
        var prefs = NotificationPreferences.default
        prefs.preferredBloodTypes = [.oNegative, .aPositive]
        prefs.preferredHospitalIds = [3, 1]
        let dto = NotificationPreferencesDTO(prefs)
        #expect(dto.preferredBloodTypes == ["0-", "A+"])
        #expect(dto.preferredHospitalIds == [1, 3])
        #expect(dto.toDomain() == prefs)
    }

    @Test func unknownBloodTypesAreIgnored() throws {
        let json = #"{"push_enabled":true,"blood_type_alerts":true,"chat_notifications":false,"preferred_blood_types":["A+","??"],"preferred_hospital_ids":[]}"#
        let dto = try JSONDecoder().decode(NotificationPreferencesDTO.self, from: Data(json.utf8))
        #expect(dto.toDomain().preferredBloodTypes == [.aPositive])
        #expect(dto.toDomain().chatNotifications == false)
        // preferred_areas yoksa (bölge yaması çalıştırılmamış veritabanı) boş kabul edilir.
        #expect(dto.toDomain().preferredAreaKeys.isEmpty)
    }
}

struct FAQCatalogTests {
    @Test func questionsAreUniqueAndAnswered() {
        #expect(!FAQCatalog.items.isEmpty)
        #expect(Set(FAQCatalog.items.map(\.id)).count == FAQCatalog.items.count)
        #expect(FAQCatalog.items.allSatisfy { !$0.answer.isEmpty })
    }
}

// MARK: - Görsel yükleme (bellek)

struct ImageLoaderTests {
    /// 6000x6000'lik bir görsel 200 px'e küçültülmeli: tam boyutta çözülseydi ~144 MB tutardı.
    @Test func hugeImageIsDownsampled() throws {
        let size = 6000
        let context = try #require(CGContext(
            data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: 0,
            space: CGColorSpaceCreateDeviceRGB(), bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue))
        context.setFillColor(red: 0.9, green: 0.2, blue: 0.2, alpha: 1)
        context.fill(CGRect(x: 0, y: 0, width: size, height: size))
        let image = try #require(context.makeImage())
        let data = try #require(UIImage(cgImage: image).pngData())

        let result = try #require(ImageLoader.downsample(data: data, maxPixelSize: 200))
        let cg = try #require(result.cgImage)
        #expect(max(cg.width, cg.height) <= 200)
        #expect(cg.bytesPerRow * cg.height < 1_000_000)
    }

    @Test func contentfulUrlsGetResizeParameters() throws {
        let url = try #require(URL(string: "https://images.ctfassets.net/space/asset/hash/logo.png"))
        let optimized = ImageVariants.optimized(url, width: 216)
        #expect(optimized.query?.contains("w=216") == true)
        #expect(optimized.query?.contains("q=80") == true)
        let other = try #require(URL(string: "https://example.com/a.png"))
        #expect(ImageVariants.optimized(other, width: 216) == other)
    }
}

struct EmailValidationTests {
    @Test func acceptsCommonAddressesAndRejectsMalformedOnes() {
        #expect(Validators.isValidEmail("donor@test.dev"))
        #expect(Validators.isValidEmail("a.b+tag@example.co.uk"))
        #expect(!Validators.isValidEmail("nope"))
        #expect(!Validators.isValidEmail("a@b"))
        #expect(!Validators.isValidEmail("@test.dev"))
    }
}

struct ReportReasonTests {
    @Test func everyReasonIsOfferedSomewhereAndCodesAreStable() {
        let offered = Set(ReportReason.forRequests + ReportReason.forUsers)
        #expect(offered == Set(ReportReason.allCases))
        #expect(ReportReason.forRequests.contains(.other) && ReportReason.forUsers.contains(.other))
        // Sunucuya giden kodlar değişmemeli (raporlar bu değerlerle saklanır).
        #expect(ReportReason.allCases.map(\.rawValue).sorted() == ["harassment", "inappropriate", "misleading", "other", "spam"])
    }
}

struct SourceCodeLinkTests {
    @Test func onlyAcceptsWellFormedHttpsUrls() {
        #expect(AppLinks.parse("https://example.org/owner/repo")?.host == "example.org")
        #expect(AppLinks.parse(nil) == nil)
        #expect(AppLinks.parse("") == nil)
        #expect(AppLinks.parse("  ") == nil)
        #expect(AppLinks.parse("$(SOURCE_CODE_URL)") == nil)
        #expect(AppLinks.parse("http://example.org") == nil)
        #expect(AppLinks.parse("not a url") == nil)
    }
}

struct RequestLimitErrorMappingTests {
    @Test func databaseLimitErrorsBecomeReadableMessages() {
        let active = SupabaseErrorMapper.map(PostgrestError(code: "P0001", message: "active_request_limit"))
        let daily = SupabaseErrorMapper.map(PostgrestError(code: "P0001", message: "daily_request_limit"))
        #expect(active == .activeRequestLimit)
        #expect(daily == .dailyRequestLimit)
        #expect(active.errorDescription?.contains("\(BloodRequestLimits.maxActive)") == true)
        #expect(daily.errorDescription?.contains("\(BloodRequestLimits.maxPerDay)") == true)
    }

    @Test func limitsMatchTheDatabase() throws {
        let sql = try String(contentsOfFile: #filePath.replacingOccurrences(
            of: "donate-blood-ios/donateblood/donatebloodTests/donatebloodTests.swift",
            with: "supabase/13_request_limits_patch.sql"), encoding: .utf8)
        #expect(sql.contains("max_active constant integer := \(BloodRequestLimits.maxActive);"))
        #expect(sql.contains("max_daily  constant integer := \(BloodRequestLimits.maxPerDay);"))
    }
}
