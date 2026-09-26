import Foundation
import Testing
@testable import donateblood

struct ProfileValidatorTests {
    private let now = Date(timeIntervalSince1970: 1_800_000_000)
    private let calendar = Calendar(identifier: .gregorian)

    private func update(name: String = "Ali", surname: String = "Veli", phone: String? = nil, yearsAgo: Int? = 30) -> ProfileUpdate {
        ProfileUpdate(name: name, surname: surname, phoneNumber: phone,
                      birthDate: yearsAgo.flatMap { calendar.date(byAdding: .year, value: -$0, to: now) },
                      bloodType: nil, gender: nil)
    }

    private func validate(_ update: ProfileUpdate, registration: Bool = true) -> [ProfileField: ProfileValidationError] {
        ProfileValidator.validate(update, isRegistration: registration, now: now, calendar: calendar)
    }

    @Test func validRegistrationHasNoErrors() {
        #expect(validate(update(phone: "0532 123 45 67")).isEmpty)
    }

    @Test func reportsEveryInvalidFieldAtOnce() {
        let errors = validate(update(name: " ", surname: "V", phone: "123", yearsAgo: 16))
        #expect(errors[.name] == .required)
        #expect(errors[.surname] == .invalidName)
        #expect(errors[.phone] == .invalidPhone)
        #expect(errors[.birthDate] == .underage)
    }

    @Test func birthDateIsRequiredOnlyForRegistration() {
        #expect(validate(update(yearsAgo: nil))[.birthDate] == .required)
        #expect(validate(update(yearsAgo: nil), registration: false)[.birthDate] == nil)
        #expect(validate(update(yearsAgo: 16), registration: false)[.birthDate] == nil)
    }

    @Test func emptyPhoneIsFine() {
        #expect(validate(update(phone: "   "))[.phone] == nil)
    }
}

@MainActor
final class FakePermissionRequester: PushPermissionRequesting {
    private(set) var requests = 0
    func requestAuthorization() async { requests += 1 }
}

@MainActor
struct RegistrationViewModelTests {
    private let now = Date(timeIntervalSince1970: 1_800_000_000)

    private final class Callbacks {
        var completed: [Profile] = []
        var signedOut = 0
    }

    private func makeSUT(profile: Profile, repo: FakeProfileRepository = FakeProfileRepository(),
                         notifications: FakeNotificationRepository = FakeNotificationRepository(),
                         push: FakePermissionRequester? = nil, callbacks: Callbacks = Callbacks()) -> RegistrationViewModel {
        RegistrationViewModel(
            profile: profile, completeProfile: .init(profiles: repo),
            setAreas: .init(notifications: notifications),
            areaPicker: NotificationAreaPickerModel(
                getAreas: .init(notifications: notifications),
                suggestArea: .init(location: FakeLocationProvider(), hospitals: FakeHospitalRepository())),
            push: push ?? FakePermissionRequester(), now: now,
            onCompleted: { callbacks.completed.append($0) },
            onSignOut: { callbacks.signedOut += 1 })
    }

    /// Kişisel ve bağışçı adımlarını geçerli verilerle geçer (bölge adımına gelir).
    private func passProfileSteps(_ sut: RegistrationViewModel) {
        sut.name = "Ali"; sut.surname = "Veli"
        sut.continueFromPersonal()
        sut.bloodType = .known(.aPositive)
        sut.birthDate = sut.suggestedBirthDate
        sut.continueFromDonor()
    }

    private func emptyProfile(name: String? = nil) -> Profile {
        Profile(id: UUID(), name: name, surname: nil, email: nil, phoneNumber: nil, birthDate: nil, bloodType: nil,
                gender: nil, userType: .regularUser, isProfileCompleted: false, createdAt: nil)
    }

    @Test func healthDetailsAreNeverPrefilledButNamesAre() {
        let sut = makeSUT(profile: emptyProfile(name: "Ayşe"))
        #expect(sut.name == "Ayşe")
        #expect(sut.bloodType == nil)
        #expect(sut.birthDate == nil)
        #expect(sut.gender == nil)
        #expect(sut.step == .personal)
    }

    @Test func errorsAppearOnlyAfterTryingToContinue() {
        let sut = makeSUT(profile: emptyProfile())
        #expect(sut.error(for: .name) == nil)
        sut.continueFromPersonal()
        #expect(sut.step == .personal)
        #expect(sut.error(for: .name) == .required)
        #expect(sut.error(for: .birthDate) == nil) // sonraki adımın hatası henüz gösterilmez
    }

    @Test func fullFlowSavesAreasAndProfileThenAsksForPermissionAndFinishes() async throws {
        let repo = FakeProfileRepository()
        let notifications = FakeNotificationRepository()
        let push = FakePermissionRequester()
        let callbacks = Callbacks()
        let sut = makeSUT(profile: emptyProfile(), repo: repo, notifications: notifications, push: push, callbacks: callbacks)

        sut.name = "Ali"; sut.surname = "Veli"
        sut.continueFromPersonal()
        #expect(sut.step == .donor)

        sut.continueFromDonor() // kan grubu seçilmedi
        #expect(sut.isBloodTypeMissing)
        #expect(sut.step == .donor)

        sut.bloodType = .known(.oNegative)
        sut.birthDate = Calendar.current.date(byAdding: .year, value: -30, to: now)
        sut.continueFromDonor()
        #expect(sut.step == .areas)
        #expect(repo.lastMarkCompleted == nil) // profil bölge adımında kaydedilir

        // Bölge seçmeden "Devam" çalışmaz.
        await sut.continueFromAreas()
        #expect(sut.step == .areas)

        await sut.areaPicker.load()
        let muratpasa = try #require(sut.areaPicker.catalog.area(forKey: "Antalya/Muratpaşa"))
        await sut.areaPicker.toggle(muratpasa)
        #expect(notifications.updateCount == 0) // kayıt akışında seçici kendisi kaydetmez

        await sut.continueFromAreas()
        #expect(sut.step == .notifications)
        #expect(notifications.stored.preferredAreaKeys == ["Antalya/Muratpaşa"])
        #expect(repo.lastMarkCompleted == true)
        #expect(callbacks.completed.isEmpty) // izin adımı bitmeden oturuma bildirilmez

        await sut.enableNotifications()
        #expect(push.requests == 1)
        #expect(callbacks.completed.count == 1)
        #expect(callbacks.completed.first?.bloodType == .oNegative)
    }

    @Test func failingAreaSaveKeepsTheProfileIncomplete() async throws {
        let repo = FakeProfileRepository()
        let notifications = FakeNotificationRepository()
        notifications.updateError = AppError.network
        let sut = makeSUT(profile: emptyProfile(), repo: repo, notifications: notifications)
        passProfileSteps(sut)
        await sut.areaPicker.load()
        await sut.areaPicker.toggle(try #require(sut.areaPicker.catalog.area(forKey: "Ankara")))
        await sut.continueFromAreas()
        #expect(sut.step == .areas)
        #expect(sut.error == .network)
        #expect(repo.lastMarkCompleted == nil) // yarım kayıt yok: bir sonraki açılışta akış yine gelir
    }

    @Test func skippingAreasCompletesProfileWithoutAnyArea() async {
        let repo = FakeProfileRepository()
        let notifications = FakeNotificationRepository()
        let sut = makeSUT(profile: emptyProfile(), repo: repo, notifications: notifications)
        passProfileSteps(sut)
        await sut.skipAreas()
        #expect(sut.step == .notifications)
        #expect(repo.lastMarkCompleted == true)
        #expect(notifications.updateCount == 0)
        #expect(notifications.stored.preferredAreaKeys.isEmpty)
    }

    @Test func unknownBloodTypeAndSkippingNotificationsStillCompletes() async {
        let push = FakePermissionRequester()
        let callbacks = Callbacks()
        let sut = makeSUT(profile: emptyProfile(name: "Ali"), push: push, callbacks: callbacks)
        sut.surname = "Veli"
        sut.continueFromPersonal()
        sut.bloodType = .unknown
        sut.birthDate = sut.suggestedBirthDate
        sut.continueFromDonor()
        await sut.skipAreas()
        await sut.skipNotifications()
        #expect(push.requests == 0)
        #expect(callbacks.completed.first?.bloodType == nil)
    }

    @Test func underageIsBlockedWithAFieldError() async {
        let sut = makeSUT(profile: emptyProfile(name: "Ali"))
        sut.surname = "Veli"
        sut.continueFromPersonal()
        sut.bloodType = .known(.aPositive)
        sut.birthDate = Calendar.current.date(byAdding: .year, value: -16, to: now)
        sut.continueFromDonor()
        #expect(sut.step == .donor)
        #expect(sut.error(for: .birthDate) == .underage)
    }

    @Test func backStepsThroughDonorAndAreasAndSignOutIsDelegated() async {
        let callbacks = Callbacks()
        let sut = makeSUT(profile: emptyProfile(name: "Ali"), callbacks: callbacks)
        #expect(!sut.canGoBack)
        passProfileSteps(sut)
        #expect(sut.step == .areas)
        #expect(sut.canGoBack)
        sut.goBack()
        #expect(sut.step == .donor)
        sut.goBack()
        #expect(sut.step == .personal)
        #expect(!sut.canGoBack)
        await sut.signOut()
        #expect(callbacks.signedOut == 1)
    }

    @Test func birthDateRangeEnforcesEighteenPlus() {
        let sut = makeSUT(profile: emptyProfile())
        let eighteen = Calendar.current.date(byAdding: .year, value: -18, to: now)!
        #expect(sut.birthDateRange.upperBound == eighteen)
        #expect(sut.birthDateRange.contains(sut.suggestedBirthDate))
    }
}

@MainActor
struct LoginFlowTests {
    @Test func onlyOneProviderRunsAtATimeAndItIsReported() async {
        let auth = FakeAuthRepository()
        let profiles = FakeProfileRepository()
        let sut = LoginViewModel(
            signInWithApple: .init(auth: auth, profiles: profiles),
            signInWithGoogle: .init(auth: auth, profiles: profiles, googleProvider: FakeCredentialProvider(result: .failure(AppError.cancelled))),
            signInWithEmail: .init(auth: auth), signInAsGuest: .init(auth: auth), isEmailLoginEnabled: false)
        #expect(sut.activeProvider == nil)
        await sut.guest()
        #expect(sut.activeProvider == nil)
        #expect(auth.guestSignIns == 1)
        // Geliştirici girişi kapalıyken test hesabı çalışmaz.
        await sut.signIn(as: TestAccount.all[0])
        #expect(auth.emailSignIns.isEmpty)
    }

    @Test func legalTextLinksBothDocuments() {
        let terms = URL(string: "https://example.org/terms")!
        let privacy = URL(string: "https://example.org/privacy")!
        let text = LegalText.attributed(terms: terms, privacy: privacy)
        let links = text.runs.compactMap(\.link)
        #expect(links.contains(terms))
        #expect(links.contains(privacy))
    }
}
