import Foundation
import Testing
@testable import donateblood

@MainActor
struct SessionStoreTests {
    private struct Fixture {
        let sut: SessionStore
        let auth: FakeAuthRepository
        let profiles: FakeProfileRepository
        let notifications: FakeNotificationRepository
        let push: FakePush
    }

    private func makeFixture() -> Fixture {
        let auth = FakeAuthRepository()
        let profiles = FakeProfileRepository()
        let notifications = FakeNotificationRepository()
        let push = FakePush()
        let sut = SessionStore(
            observeAuth: .init(auth: auth), getProfile: .init(profiles: profiles),
            getUnreadCount: .init(notifications: notifications),
            signOut: .init(auth: auth),
            push: push, events: DataEvents())
        sut.start()
        return Fixture(sut: sut, auth: auth, profiles: profiles, notifications: notifications, push: push)
    }

    @Test func startsInLoadingThenSignedOut() async {
        let f = makeFixture()
        #expect(f.sut.phase == .loading)
        f.auth.emit(.signedOut)
        #expect(await waitUntil { f.sut.phase == .signedOut })
    }

    @Test func guestSignsInWithoutProfileAndCannotPassTheGate() async {
        let f = makeFixture()
        f.auth.emit(.signedIn(AuthUser(id: UUID(), isGuest: true)))
        #expect(await waitUntil { f.sut.phase == .signedIn })
        #expect(f.sut.isGuest)
        #expect(f.sut.profile == nil)

        var ran = false
        f.sut.requireAccount { ran = true }
        #expect(!ran)
        #expect(f.sut.isAuthSheetPresented)
    }

    @Test func registeredUserLoadsProfileAndUnreadCountWithoutAnUnaskedPermissionPrompt() async {
        let f = makeFixture()
        let id = UUID()
        f.profiles.profile = TestData.profile(id: id)
        f.notifications.unread = 4
        f.auth.emit(.signedIn(AuthUser(id: id, isGuest: false)))
        // Profil ve okunmamış sayacı sırayla gelir: son adımı bekle.
        #expect(await waitUntil { f.push.badge == 4 })
        #expect(f.sut.profile != nil)
        #expect(f.sut.unreadNotifications == 4)
        // İzin, açılışta açıklamasız istenmez (kayıt akışında ya da ayarlarda istenir).
        #expect(f.push.permissionRequests == 0)

        var ran = false
        f.sut.requireAccount { ran = true }
        #expect(ran)
        #expect(!f.sut.isAuthSheetPresented)
    }

    @Test func incompleteProfileDoesNotRequestPushYet() async {
        let f = makeFixture()
        f.profiles.profile = TestData.profile(completed: false)
        f.auth.emit(.signedIn(AuthUser(id: UUID(), isGuest: false)))
        #expect(await waitUntil { f.sut.profile != nil })
        #expect(f.push.permissionRequests == 0)
    }

    @Test func profileLoadFailureIsFlaggedForRetry() async {
        let f = makeFixture()
        f.profiles.error = AppError.network
        f.auth.emit(.signedIn(AuthUser(id: UUID(), isGuest: false)))
        #expect(await waitUntil { f.sut.profileLoadFailed })
    }

    @Test func registeredUserIsIdentifiedToPushImmediately() async {
        let f = makeFixture()
        let id = UUID()
        f.profiles.profile = TestData.profile(id: id, completed: false)
        f.auth.emit(.signedIn(AuthUser(id: id, isGuest: false)))
        #expect(await waitUntil { f.push.identified == [id] })
        // Profil tamamlanmadıkça izin istenmez.
        #expect(f.push.permissionRequests == 0)
    }

    @Test func guestIsNeverIdentifiedAndClearsAnyPreviousIdentity() async {
        let f = makeFixture()
        f.auth.emit(.signedIn(AuthUser(id: UUID(), isGuest: true)))
        #expect(await waitUntil { f.push.clearCount == 1 })
        #expect(f.push.identified.isEmpty)
    }

    @Test func signOutClearsSessionAndPushIdentity() async throws {
        let f = makeFixture()
        f.profiles.profile = TestData.profile()
        f.auth.emit(.signedIn(AuthUser(id: UUID(), isGuest: false)))
        #expect(await waitUntil { f.sut.profile != nil })

        try await f.sut.signOut()
        #expect(f.auth.signOuts == 1)
        f.auth.emit(.signedOut)
        #expect(await waitUntil { f.sut.phase == .signedOut })
        #expect(f.push.clearCount == 1)
        #expect(f.sut.profile == nil)
        #expect(f.sut.unreadNotifications == 0)
    }

    @Test func pushTapSetsPendingDeepLink() async {
        let f = makeFixture()
        let id = UUID()
        f.push.onOpen?(.request(id))
        #expect(f.sut.pendingLink == .request(id))
    }
}

@MainActor
struct LoginViewModelTests {
    private func makeSUT(auth: FakeAuthRepository, provider: FakeCredentialProvider? = nil, emailEnabled: Bool = true) -> LoginViewModel {
        let profiles = FakeProfileRepository()
        let google = provider ?? FakeCredentialProvider(result: .failure(AppError.cancelled))
        return LoginViewModel(
            signInWithApple: .init(auth: auth, profiles: profiles),
            signInWithGoogle: .init(auth: auth, profiles: profiles, googleProvider: google),
            signInWithEmail: .init(auth: auth),
            signInAsGuest: .init(auth: auth),
            isEmailLoginEnabled: emailEnabled)
    }

    @Test func emailSignInTrimsEmailAndClearsPasswordOnSuccess() async {
        let auth = FakeAuthRepository()
        let sut = makeSUT(auth: auth)
        sut.email = "  donor@test.dev "
        sut.password = "Test1234!"
        #expect(sut.canSubmitEmail)
        await sut.emailSignIn()
        #expect(auth.emailSignIns.first?.0 == "donor@test.dev")
        #expect(auth.emailSignIns.first?.1 == "Test1234!")
        #expect(sut.password.isEmpty)
        #expect(sut.error == nil)
    }

    @Test func invalidEmailIsRejectedBeforeNetwork() async {
        let auth = FakeAuthRepository()
        let sut = makeSUT(auth: auth)
        sut.email = "not-an-email"
        sut.password = "x"
        await sut.emailSignIn()
        #expect(auth.emailSignIns.isEmpty)
        #expect(sut.error != nil)
    }

    @Test func wrongCredentialsAreShownAndPasswordKept() async {
        let auth = FakeAuthRepository()
        auth.signInError = AppError.invalidCredentials
        let sut = makeSUT(auth: auth)
        sut.email = "donor@test.dev"
        sut.password = "wrong"
        await sut.emailSignIn()
        #expect(sut.error == .invalidCredentials)
        #expect(sut.password == "wrong")
    }

    @Test func testAccountButtonSignsInWithoutTyping() async {
        let auth = FakeAuthRepository()
        let sut = makeSUT(auth: auth)
        await sut.signIn(as: TestAccount.all[0])
        #expect(auth.emailSignIns.first?.0 == "donor@test.dev")
        #expect(auth.emailSignIns.first?.1 == "Test1234!")
        #expect(sut.error == nil)
    }

    @Test func testAccountsAreIgnoredWhenTheFeatureIsDisabled() async {
        let auth = FakeAuthRepository()
        let sut = makeSUT(auth: auth, emailEnabled: false)
        await sut.signIn(as: TestAccount.all[0])
        #expect(auth.emailSignIns.isEmpty)
    }

    @Test func testAccountsMatchTheSqlSeed() throws {
        // supabase/08_test_users.sql ile aynı e-posta ve şifreler olmalı.
        let sql = try String(contentsOfFile: #filePath.replacingOccurrences(of: "donate-blood-ios/donateblood/donatebloodTests/SessionAndLoginTests.swift", with: "supabase/08_test_users.sql"), encoding: .utf8)
        for account in TestAccount.all {
            #expect(sql.contains(account.email))
            #expect(sql.contains(account.password))
        }
        #expect(Set(TestAccount.all.map(\.email)).count == TestAccount.all.count)
    }

    @Test func emailFormIsHiddenWhenDisabled() {
        let sut = makeSUT(auth: FakeAuthRepository(), emailEnabled: false)
        #expect(!sut.isEmailLoginEnabled)
    }

    @Test func guestSignInSucceeds() async {
        let auth = FakeAuthRepository()
        let sut = makeSUT(auth: auth)
        await sut.guest()
        #expect(auth.guestSignIns == 1)
        #expect(sut.error == nil)
        #expect(!sut.isLoading)
    }

    @Test func failureIsShownToUser() async {
        let auth = FakeAuthRepository()
        auth.signInError = AppError.network
        let sut = makeSUT(auth: auth)
        await sut.guest()
        #expect(sut.error == .network)
    }

    @Test func userCancellationIsSilent() async {
        let auth = FakeAuthRepository()
        auth.signInError = AppError.cancelled
        let sut = makeSUT(auth: auth)
        await sut.guest()
        #expect(sut.error == nil)
    }

    @Test func googleCancellationIsSilentAndSuccessSignsIn() async {
        let auth = FakeAuthRepository()
        let cancelled = makeSUT(auth: auth)
        await cancelled.google()
        #expect(cancelled.error == nil)
        #expect(auth.credentials.isEmpty)

        let credential = IdentityCredential(provider: .google, idToken: "t", nonce: "n", accessToken: nil, fullName: nil)
        let ok = makeSUT(auth: auth, provider: FakeCredentialProvider(result: .success(credential)))
        await ok.google()
        #expect(auth.credentials.count == 1)
    }
}

