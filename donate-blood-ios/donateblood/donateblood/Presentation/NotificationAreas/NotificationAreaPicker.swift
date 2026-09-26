import SwiftUI

/// Bildirim bölgesi seçici içeriği (kaydırma kabı dışarıdan verilir).
/// Üstte "Konumumu kullan" ve arama, altında seçilenler (çip) ve il listesi; il satırı açılınca ilçeleri gösterir.
struct NotificationAreaPicker: View {
    @ObservedObject var model: NotificationAreaPickerModel

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.l) {
            locationButton
            suggestionMessage
            searchField
            if !model.selectedAreas.isEmpty { selectedChips }
            content
        }
        .task { await model.load() }
    }

    // MARK: Üst kısım

    private var locationButton: some View {
        Button { Task { await model.useMyLocation() } } label: {
            HStack(spacing: Theme.Spacing.s) {
                if model.suggestion == .locating {
                    ProgressView()
                } else {
                    Image(systemName: "location.fill").foregroundStyle(Theme.brand)
                }
                Text("notificationAreas.useLocation")
            }
        }
        .buttonStyle(OutlinePillButtonStyle())
        .disabled(model.state != .loaded || model.suggestion == .locating)
    }

    @ViewBuilder
    private var suggestionMessage: some View {
        switch model.suggestion {
        case .added(let area):
            message(L10n.format("notificationAreas.suggestion.added %@", area.displayName), systemImage: "checkmark.circle.fill", color: .green)
        case .alreadySelected(let area):
            message(L10n.format("notificationAreas.suggestion.already %@", area.displayName), systemImage: "checkmark.circle", color: .secondary)
        case .locationUnavailable:
            message(L10n.string("notificationAreas.suggestion.noLocation"), systemImage: "location.slash", color: .secondary)
        case .noneNearby:
            message(L10n.string("notificationAreas.suggestion.noneNearby"), systemImage: "mappin.slash", color: .secondary)
        case .limitReached:
            message(L10n.format("error.selectionLimit %lld", NotificationScope.maxSelections), systemImage: "exclamationmark.circle.fill", color: .orange)
        case .idle, .locating:
            EmptyView()
        }
    }

    private func message(_ text: String, systemImage: String, color: Color) -> some View {
        Label(text, systemImage: systemImage)
            .font(.footnote).foregroundStyle(color)
            .transition(.opacity)
    }

    private var searchField: some View {
        HStack(spacing: Theme.Spacing.s) {
            Image(systemName: "magnifyingglass").foregroundStyle(.secondary).accessibilityHidden(true)
            TextField("notificationAreas.search", text: $model.query)
                .textInputAutocapitalization(.words).autocorrectionDisabled()
                .submitLabel(.search)
            if !model.query.isEmpty {
                Button { model.query = "" } label: { Image(systemName: "xmark.circle.fill").foregroundStyle(.secondary) }
                    .accessibilityLabel(Text("common.clear"))
            }
        }
        .padding(.horizontal, Theme.Spacing.m)
        .frame(minHeight: Theme.Size.minTap)
        .background(Theme.Palette.surface, in: RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous))
    }

    private var selectedChips: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.s) {
            HStack {
                Text("notificationAreas.selected").font(.subheadline.weight(.semibold))
                Spacer()
                Text(L10n.format("notificationAreas.count %lld %lld", model.selection.count, NotificationScope.maxSelections))
                    .font(.footnote.monospacedDigit())
                    .foregroundStyle(model.selection.isAtLimit ? Color.orange : Color.secondary)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: Theme.Spacing.s) {
                    ForEach(model.selectedAreas) { area in
                        Button { Task { await model.toggle(area) } } label: {
                            HStack(spacing: Theme.Spacing.xs) {
                                Text(area.displayName).lineLimit(1)
                                Image(systemName: "xmark").font(.caption2.weight(.bold))
                            }
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(.white)
                            .padding(.horizontal, Theme.Spacing.m)
                            .frame(minHeight: 34)
                            .background(Theme.brand, in: Capsule())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text(verbatim: area.displayName))
                        .accessibilityHint(Text("notificationAreas.remove"))
                    }
                }
            }
        }
    }

    // MARK: Liste

    @ViewBuilder
    private var content: some View {
        switch model.state {
        case .idle, .loading:
            ProgressView().frame(maxWidth: .infinity).padding(.vertical, Theme.Spacing.xl)
        case .failed(let error):
            ErrorStateView(error: error) { Task { await model.retry() } }
        case .loaded:
            let cities = model.visibleCities
            if cities.isEmpty {
                Text(model.isSearching ? "notificationAreas.noResults" : "notificationAreas.empty")
                    .font(.subheadline).foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity).padding(.vertical, Theme.Spacing.xl)
            } else {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(cities) { group in
                        cityRow(group)
                        if model.isExpanded(group) {
                            ForEach(group.districts) { districtRow($0) }
                        }
                        HairlineDivider()
                    }
                }
            }
        }
    }

    private func cityRow(_ group: CityAreas) -> some View {
        HStack(spacing: Theme.Spacing.s) {
            areaButton(group.city, title: group.city.city, font: .body.weight(.semibold))
            if !group.districts.isEmpty {
                Button { withAnimation(.easeInOut(duration: 0.2)) { model.toggleExpanded(group) } } label: {
                    Image(systemName: "chevron.down")
                        .font(.subheadline.weight(.semibold)).foregroundStyle(.secondary)
                        .rotationEffect(.degrees(model.isExpanded(group) ? 180 : 0))
                        .frame(width: Theme.Size.minTap, height: Theme.Size.minTap)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(model.isExpanded(group) ? "notificationAreas.collapse" : "notificationAreas.expand"))
            }
        }
    }

    private func districtRow(_ area: NotificationArea) -> some View {
        areaButton(area, title: area.district ?? area.city, font: .body)
            .padding(.leading, Theme.Spacing.xl + Theme.Spacing.s)
            .padding(.trailing, Theme.Size.minTap + Theme.Spacing.s)
    }

    private func areaButton(_ area: NotificationArea, title: String, font: Font) -> some View {
        let isSelected = model.isSelected(area)
        let isCovered = model.isCovered(area)
        let isEnabled = model.canToggle(area)
        return Button { Task { await model.toggle(area) } } label: {
            HStack(spacing: Theme.Spacing.m) {
                Image(systemName: isSelected || isCovered ? "checkmark.circle.fill" : "circle")
                    .font(.title3)
                    .foregroundStyle(isSelected || isCovered ? Theme.brand : Color.secondary)
                    .opacity(isCovered ? 0.4 : 1)
                VStack(alignment: .leading, spacing: 2) {
                    Text(verbatim: title).font(font).foregroundStyle(.primary)
                    Text(isCovered ? L10n.string("notificationAreas.coveredByCity") : L10n.format("notificationAreas.hospitalCount %lld", area.hospitalCount))
                        .font(.caption).foregroundStyle(.secondary)
                }
                Spacer(minLength: 0)
            }
            .frame(minHeight: 52)
            .contentShape(Rectangle())
            .opacity(isEnabled || isCovered ? 1 : 0.45)
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}
