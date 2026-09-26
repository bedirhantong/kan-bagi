import Foundation
import Testing
@testable import donateblood

struct OneSignalConfigTests {
    private func appID(_ value: String?) -> String? { AppConfig.oneSignalAppID { _ in value } }

    @Test func validIdEnablesPush() {
        #expect(appID("0a1b2c3d-1111-2222-3333-444455556666") == "0a1b2c3d-1111-2222-3333-444455556666")
        #expect(appID("  abc  ") == "abc")
    }

    @Test func missingEmptyUnexpandedOrPlaceholderDisablesPush() {
        #expect(appID(nil) == nil)
        #expect(appID("") == nil)
        #expect(appID("   ") == nil)
        #expect(appID("$(ONESIGNAL_APP_ID)") == nil)
        #expect(appID("YOUR_ONESIGNAL_APP_ID") == nil)
    }
}

@MainActor
struct NoPushServiceTests {
    @Test func disabledServiceDoesNothingAndNeverCrashes() async {
        let service = NoPushService()
        await service.requestAuthorization()
        service.identify(userId: UUID())
        service.clearIdentity()
        service.setBadge(3)
    }
}

/// Derin bağlantı: OneSignal `additionalData` sözlüğü ile aynı biçim.
struct DeepLinkPayloadTests {
    @Test func additionalDataFromServerBecomesLink() {
        let id = UUID()
        #expect(DeepLink(userInfo: ["request_id": id.uuidString, "type": "DONATION_PLEDGE"]) == .request(id))
        #expect(DeepLink(userInfo: ["room_id": id.uuidString, "type": "CHAT_MESSAGE"]) == .room(id))
        #expect(DeepLink(userInfo: ["request_id": "bozuk"]) == nil)
    }
}
