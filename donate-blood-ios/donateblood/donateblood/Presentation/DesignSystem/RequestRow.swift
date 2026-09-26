import SwiftUI

/// Akıştaki ilan kartı. Ürün araştırmasına dayanır (bkz. docs/DESIGN_RESEARCH.md):
/// - Yalnızca karar için gerekenler: **kan grubu**, **hastane**, **mesafe/zaman**, **aciliyet**, **uygunluk**.
///   Hasta adı, açıklama ve iletişim ayrıntı ekranındadır (kademeli gösterim).
/// - Her ilan kendi sınırı içindedir (ortak bölge): akışta ilanlar birbirine karışmaz.
/// - Aciliyet renk + ikon + yazı ile verilir (yalnızca renge güvenilmez).
struct RequestRow: View {
    let request: BloodRequest
    /// Kullanıcı konumundan hastaneye mesafe (metre); konum yoksa `nil`.
    var distance: Double?
    /// Kullanıcının kan grubu; bu hastaya bağış yapabiliyorsa "Sana uygun" işareti gösterilir.
    var viewerBloodType: BloodType?
    /// Profil listelerinde ilan başlığı da gösterilir (kişi kendi ilanlarını başlıkla tanır).
    var showsTitle = false
    /// Kapatılmış ilan için "Tekrar Paylaş" gibi ek eylem (profil listeleri).
    var trailingAction: (title: LocalizedStringKey, systemImage: String, action: () -> Void)?

    private var isCompatible: Bool {
        guard let viewerBloodType, request.isActive else { return false }
        return viewerBloodType.canDonate(to: request.bloodType)
    }

    private var isUrgent: Bool { request.isEmergency && request.isActive }

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.s) {
            HStack(alignment: .center, spacing: Theme.Spacing.m) {
                BloodTypeTile(bloodType: request.bloodType, isEmergency: isUrgent)
                VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                    Text(request.hospital?.name ?? request.title)
                        .font(.headline).lineLimit(2).fixedSize(horizontal: false, vertical: true)
                    if showsTitle, request.hospital != nil {
                        Text(request.title).font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
                    }
                    if let area = request.hospital?.locationDescription, !area.isEmpty {
                        Text(area).font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
                    }
                    metaLine
                }
                Spacer(minLength: 0)
                Image(systemName: "chevron.right").font(.footnote.weight(.semibold)).foregroundStyle(.tertiary)
                    .accessibilityHidden(true)
            }
            chips
            if let trailingAction {
                Button(action: trailingAction.action) {
                    Label(trailingAction.title, systemImage: trailingAction.systemImage)
                }
                .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
            }
        }
        .padding(Theme.Spacing.m + 2)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Theme.Palette.card, in: RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous))
        .overlay(alignment: .leading) {
            // Acil ilanda soldaki vurgu şeridi: karttan kart ayırt etmeyi ve öncelik taramayı kolaylaştırır.
            if isUrgent {
                RoundedRectangle(cornerRadius: 2).fill(Theme.brand).frame(width: 4).padding(.vertical, Theme.Spacing.m)
            }
        }
        .overlay(RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous)
            .stroke(Theme.Palette.separator.opacity(0.35), lineWidth: 1))
        .opacity(request.isActive ? 1 : 0.6)
        .contentShape(RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous))
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text(accessibilitySummary))
    }

    /// "0,8 km · 2 sa önce" — konum ve zaman, ikinci düzey bilgi.
    private var metaLine: some View {
        HStack(spacing: Theme.Spacing.xs) {
            if let distance {
                Image(systemName: "location.fill").font(.caption2)
                Text(DistanceFormatting.string(meters: distance))
                Text(verbatim: "·")
            }
            Text(request.createdAt.relativeDescription)
        }
        .font(.footnote).foregroundStyle(.secondary).lineLimit(1)
    }

    @ViewBuilder
    private var chips: some View {
        if isUrgent || !request.isActive || isCompatible || request.isVerified {
            HStack(spacing: Theme.Spacing.s) {
                if !request.isActive {
                    StatusChip(text: "bloodRequest.closed", systemImage: "lock.fill", color: Theme.Palette.warning)
                } else if isUrgent {
                    StatusChip(text: "bloodRequest.urgent", systemImage: "bolt.fill", color: Theme.brand, filled: true)
                }
                if isCompatible {
                    StatusChip(text: "request.compatible", systemImage: "checkmark.circle.fill", color: Theme.Palette.compatible)
                }
                if request.isVerified {
                    StatusChip(text: "bloodRequest.verified", systemImage: "checkmark.seal.fill", color: Theme.Palette.verified)
                }
            }
        }
    }

    private var accessibilitySummary: String {
        var parts = [L10n.format("accessibility.bloodType %@", request.bloodType.displayName)]
        if isUrgent { parts.append(L10n.string("bloodRequest.urgent")) }
        if isCompatible { parts.append(L10n.string("request.compatible")) }
        parts.append(request.hospital?.name ?? request.title)
        if let distance { parts.append(DistanceFormatting.string(meters: distance)) }
        parts.append(request.createdAt.relativeDescription)
        return parts.joined(separator: ", ")
    }
}
