import Foundation
import Testing
@testable import donateblood

struct DesignSystemTests {
    @Test func avatarColorIsStableAndInRange() {
        let id = UUID()
        let first = AvatarView.paletteIndex(for: id, count: 6)
        #expect((0..<6).contains(first))
        #expect(AvatarView.paletteIndex(for: id, count: 6) == first) // aynı kişi hep aynı renk
    }

    @Test func differentPeopleGetVariedColors() {
        let indexes = Set((0..<200).map { _ in AvatarView.paletteIndex(for: UUID(), count: 6) })
        #expect(indexes.count > 1)
    }

    @Test func spacingScaleIsMonotonic() {
        let scale = [Theme.Spacing.xs, Theme.Spacing.s, Theme.Spacing.m, Theme.Spacing.l, Theme.Spacing.xl]
        #expect(scale == scale.sorted())
        #expect(Theme.Size.minTap >= 44) // Apple HIG asgari dokunma alanı
    }

    @Test func relativeDatesUseAShortStyle() {
        let text = Date(timeIntervalSinceNow: -18 * 60).relativeDescription
        #expect(!text.isEmpty)
        #expect(text.count < 20) // "18 min. ago" gibi kısa; "18 minutes ago" değil
    }
}

struct BloodCompatibilityTests {
    /// Sunucudaki `compatible_donor_types(recipient)` tablosunun aynısı (supabase/schema.sql).
    private static let recipientToDonors: [BloodType: Set<BloodType>] = [
        .aPositive: [.aPositive, .aNegative, .oPositive, .oNegative],
        .aNegative: [.aNegative, .oNegative],
        .bPositive: [.bPositive, .bNegative, .oPositive, .oNegative],
        .bNegative: [.bNegative, .oNegative],
        .abPositive: Set(BloodType.allCases),
        .abNegative: [.aNegative, .bNegative, .abNegative, .oNegative],
        .oPositive: [.oPositive, .oNegative],
        .oNegative: [.oNegative],
    ]

    @Test func matchesTheServerCompatibilityTableForEveryPair() {
        for recipient in BloodType.allCases {
            let expected = Self.recipientToDonors[recipient]!
            let actual = Set(BloodType.allCases.filter { $0.canDonate(to: recipient) })
            #expect(actual == expected, "alıcı \(recipient.rawValue)")
        }
    }

    @Test func universalDonorAndRecipient() {
        #expect(BloodType.allCases.allSatisfy { BloodType.oNegative.canDonate(to: $0) })
        #expect(BloodType.allCases.allSatisfy { $0.canDonate(to: .abPositive) })
    }

    @Test func rhPositiveCannotDonateToRhNegative() {
        #expect(!BloodType.oPositive.canDonate(to: .oNegative))
        #expect(!BloodType.aPositive.canDonate(to: .aNegative))
    }
}

