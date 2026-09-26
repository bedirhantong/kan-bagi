import SwiftUI

/// Haritada seçili yerin kartı için ortak iskelet: ikon, başlık, alt bilgi, eylemler ve isteğe bağlı ek içerik.
struct MapPlaceCard<Icon: View, Extra: View>: View {
    let title: String
    let subtitle: String?
    let distance: Double?
    let onClose: () -> Void
    @ViewBuilder var icon: () -> Icon
    @ViewBuilder var extra: () -> Extra

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.m) {
            HStack(alignment: .top, spacing: Theme.Spacing.m) {
                icon()
                VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                    Text(title).font(.headline).fixedSize(horizontal: false, vertical: true)
                    if let subtitle, !subtitle.isEmpty {
                        Text(subtitle).font(.subheadline).foregroundStyle(.secondary).lineLimit(2)
                    }
                    if let distance {
                        Label(DistanceFormatting.string(meters: distance), systemImage: "location.fill")
                            .font(.footnote).foregroundStyle(.secondary)
                    }
                }
                Spacer(minLength: 0)
                Button(action: onClose) {
                    Image(systemName: "xmark").font(.footnote.weight(.bold)).foregroundStyle(.secondary)
                        .frame(width: 30, height: 30).background(Theme.Palette.surface, in: Circle())
                }
                .buttonStyle(.plain)
                .frame(minWidth: Theme.Size.minTap, minHeight: Theme.Size.minTap, alignment: .topTrailing)
                .accessibilityLabel(Text("common.close"))
            }
            extra()
        }
        .padding(Theme.Spacing.l)
        .background(Theme.Palette.card, in: RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous))
        .shadow(color: .black.opacity(0.15), radius: 12, y: 4)
    }
}

/// Seçili hastane: yol tarifi, arama, ayrıntı ve o hastanedeki aktif kan ihtiyaçları.
struct HospitalMapCard: View {
    let hospital: Hospital
    let distance: Double?
    let requests: [BloodRequest]
    let isLoadingRequests: Bool
    let onClose: () -> Void

    var body: some View {
        MapPlaceCard(title: hospital.name, subtitle: hospital.address ?? hospital.locationDescription, distance: distance, onClose: onClose) {
            Image(systemName: "cross.fill").font(.title3).foregroundStyle(.white)
                .frame(width: Theme.Size.avatar, height: Theme.Size.avatar)
                .background(Color.blue, in: Circle())
                .accessibilityHidden(true)
        } extra: {
            HStack(spacing: Theme.Spacing.s) {
                if let coordinate = hospital.coordinate {
                    Button { MapActions.openDirections(to: coordinate, name: hospital.name) } label: {
                        Label("hospital.directions", systemImage: "arrow.triangle.turn.up.right.diamond.fill")
                    }
                    .buttonStyle(PrimaryPillButtonStyle())
                }
                if let phone = hospital.phoneNumber, let url = MapActions.phoneURL(phone) {
                    Link(destination: url) { Image(systemName: "phone.fill").frame(minWidth: 24) }
                        .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
                        .accessibilityLabel(Text("hospital.call"))
                }
                NavigationLink(value: Route.hospital(hospital)) { Image(systemName: "info.circle").frame(minWidth: 24) }
                    .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
                    .accessibilityLabel(Text("detail.details"))
            }

            VStack(alignment: .leading, spacing: Theme.Spacing.s) {
                Text("map.bloodNeeds").font(.subheadline.weight(.semibold))
                if isLoadingRequests {
                    ProgressView().frame(maxWidth: .infinity, alignment: .leading)
                } else if requests.isEmpty {
                    Text("map.noNeeds").font(.footnote).foregroundStyle(.secondary)
                } else {
                    ForEach(requests) { request in
                        NavigationLink(value: Route.request(request.id)) {
                            HStack(spacing: Theme.Spacing.m) {
                                BloodTypeTile(bloodType: request.bloodType, isEmergency: request.isEmergency, size: 36)
                                Text(request.title).font(.subheadline).foregroundStyle(.primary).lineLimit(1)
                                Spacer(minLength: 0)
                                Image(systemName: "chevron.right").font(.caption.weight(.semibold)).foregroundStyle(.tertiary)
                            }
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }
}

/// Seçili Kızılay kan bağış noktası: tür (sabit birim / gezici ekip), adres, arama ve yol tarifi.
struct BloodPointMapCard: View {
    let point: BloodDonationPoint
    let distance: Double?
    let onClose: () -> Void

    var body: some View {
        MapPlaceCard(title: point.name, subtitle: [point.address, point.areaDescription].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " · "),
                     distance: distance, onClose: onClose) {
            Image(systemName: point.kind == .mobile ? "bus.fill" : "drop.fill").font(.title3).foregroundStyle(.white)
                .frame(width: Theme.Size.avatar, height: Theme.Size.avatar)
                .background(Theme.brand, in: Circle())
                .accessibilityHidden(true)
        } extra: {
            HStack(spacing: Theme.Spacing.s) {
                if point.kind == .fixed {
                    StatusChip(text: "map.point.fixed", systemImage: "building.2.fill", color: Theme.brand)
                } else {
                    StatusChip(text: "map.point.mobile", systemImage: "bus.fill", color: Theme.Palette.warning)
                }
            }
            if point.kind == .mobile {
                Text("map.point.mobileNote").font(.footnote).foregroundStyle(.secondary)
            }
            HStack(spacing: Theme.Spacing.s) {
                Button { MapActions.openDirections(to: point.coordinate, name: point.name) } label: {
                    Label("hospital.directions", systemImage: "arrow.triangle.turn.up.right.diamond.fill")
                }
                .buttonStyle(PrimaryPillButtonStyle())
                if let phone = point.phoneNumber, let url = MapActions.phoneURL(phone) {
                    Link(destination: url) { Label("hospital.call", systemImage: "phone.fill") }
                        .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
                }
            }
        }
    }
}
