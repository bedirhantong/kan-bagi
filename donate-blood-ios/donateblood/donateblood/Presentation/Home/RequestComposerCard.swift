import SwiftUI

/// X'teki "Neler oluyor?" alanının karşılığı: akışın başında, ilan kartlarıyla aynı kenar boşluğu ve köşelerle.
/// Üstte avatar + büyük yer tutucu metin, altında kısayol simgeleri ve sağda "İlan ver".
/// Kısayollar formu önceden doldurur (kan grubu, acil, hastane); misafirde giriş kararı `onCreate`'i verendedir.
struct RequestComposerCard: View {
    /// Oturumdaki profil; misafirde `nil` (genel kişi simgesi gösterilir).
    let profile: Profile?
    let onCreate: (RequestPrefill) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.s) {
            Button { onCreate(.empty) } label: {
                HStack(alignment: .center, spacing: Theme.Spacing.m) {
                    avatar
                    Text("home.composer.placeholder")
                        .font(.title3).foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .multilineTextAlignment(.leading)
                }
                .frame(minHeight: 48)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text("bloodRequest.create"))
            .accessibilityHint(Text("home.composer.placeholder"))

            HStack(spacing: Theme.Spacing.xs) {
                bloodTypeMenu
                shortcut("exclamationmark.triangle", label: "home.composer.emergency") {
                    onCreate(RequestPrefill(isEmergency: true))
                }
                shortcut("building.2", label: "home.composer.hospital") {
                    onCreate(RequestPrefill(opensHospitalPicker: true))
                }
                Spacer(minLength: Theme.Spacing.s)
                Button { onCreate(.empty) } label: { Text("home.composer.action") }
                    .buttonStyle(PrimaryPillButtonStyle(fullWidth: false))
                    .accessibilityHidden(true) // üstteki alanla aynı eylem; VoiceOver'da tekrar etmesin
            }
            // Simgeler metinle hizalı başlar (X düzeni).
            .padding(.leading, Self.avatarSize + Theme.Spacing.m - Theme.Spacing.s)
        }
        .padding(Theme.Spacing.m + 2) // RequestRow ile aynı iç boşluk
        .background(Theme.Palette.card, in: RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous)
            .stroke(Theme.Palette.separator.opacity(0.35), lineWidth: 1))
    }

    private static let avatarSize: CGFloat = 44

    @ViewBuilder
    private var avatar: some View {
        if let profile, !profile.fullName.isEmpty {
            AvatarView(name: profile.fullName, seed: profile.id, size: Self.avatarSize)
        } else {
            Image(systemName: "person.fill")
                .font(.system(size: 20, weight: .semibold)).foregroundStyle(.secondary)
                .frame(width: Self.avatarSize, height: Self.avatarSize)
                .background(Theme.Palette.surface, in: Circle())
                .accessibilityHidden(true)
        }
    }

    /// Kan grubu kısayolu: menüden seçilen grup formda hazır gelir.
    private var bloodTypeMenu: some View {
        Menu {
            ForEach(BloodType.allCases) { type in
                Button { onCreate(RequestPrefill(bloodType: type)) } label: { Text(verbatim: type.displayName) }
            }
        } label: {
            shortcutIcon("drop")
        }
        .accessibilityLabel(Text("home.composer.bloodType"))
    }

    private func shortcut(_ systemImage: String, label: LocalizedStringKey, action: @escaping () -> Void) -> some View {
        Button(action: action) { shortcutIcon(systemImage) }
            .accessibilityLabel(Text(label))
    }

    private func shortcutIcon(_ systemImage: String) -> some View {
        Image(systemName: systemImage)
            .font(.system(size: 19, weight: .medium))
            .foregroundStyle(Theme.brand)
            .frame(width: Theme.Size.minTap, height: Theme.Size.minTap)
            .contentShape(Rectangle())
    }
}
