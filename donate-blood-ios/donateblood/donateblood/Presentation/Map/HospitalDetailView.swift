import SwiftUI

/// Hastane ayrıntısı: başlık, hızlı eylemler (yol tarifi, ara, web) ve iletişim bilgileri.
struct HospitalDetailView: View {
    let hospital: Hospital

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                header
                actions.padding(.horizontal, Theme.Spacing.l).padding(.bottom, Theme.Spacing.l)
                HairlineDivider()
                InfoSection(title: "hospital.contact", systemImage: "phone") {
                    if let address = hospital.address, !address.isEmpty {
                        infoLine(systemImage: "mappin.and.ellipse", text: address)
                    }
                    if let phone = hospital.phoneNumber, let url = MapActions.phoneURL(phone) {
                        Link(destination: url) { infoLine(systemImage: "phone.fill", text: phone, tinted: true) }
                    }
                    if let email = hospital.email, let url = URL(string: "mailto:\(email)") {
                        Link(destination: url) { infoLine(systemImage: "envelope.fill", text: email, tinted: true) }
                    }
                    if let website = hospital.website {
                        Link(destination: website) { infoLine(systemImage: "safari", text: website.host ?? website.absoluteString, tinted: true) }
                    }
                }
            }
        }
        .navigationTitle(hospital.name)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var header: some View {
        HStack(spacing: Theme.Spacing.m) {
            RemoteImage(url: hospital.iconURL, pointSize: 56, contentMode: .fit) {
                Image(systemName: "cross.fill").font(.title2).foregroundStyle(.white)
                    .frame(width: 56, height: 56).background(Color.blue, in: Circle())
            }
            .frame(width: 56, height: 56)
            .clipShape(Circle())
            .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                Text(hospital.name).font(.title3.weight(.semibold))
                if !hospital.locationDescription.isEmpty {
                    Text(hospital.locationDescription).font(.subheadline).foregroundStyle(.secondary)
                }
            }
        }
        .padding(Theme.Spacing.l)
    }

    private var actions: some View {
        HStack(spacing: Theme.Spacing.s) {
            if let coordinate = hospital.coordinate {
                Button { MapActions.openDirections(to: coordinate, name: hospital.name) } label: {
                    Label("hospital.directions", systemImage: "arrow.triangle.turn.up.right.diamond.fill")
                }
                .buttonStyle(PrimaryPillButtonStyle())
            }
            if let phone = hospital.phoneNumber, let url = MapActions.phoneURL(phone) {
                Link(destination: url) { Label("hospital.call", systemImage: "phone.fill") }
                    .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
            }
        }
    }

    private func infoLine(systemImage: String, text: String, tinted: Bool = false) -> some View {
        HStack(alignment: .top, spacing: Theme.Spacing.m) {
            Image(systemName: systemImage).frame(width: 24).foregroundStyle(Theme.brand).accessibilityHidden(true)
            Text(text).foregroundStyle(tinted ? Theme.brand : .primary).multilineTextAlignment(.leading)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
        .contentShape(Rectangle())
    }
}
