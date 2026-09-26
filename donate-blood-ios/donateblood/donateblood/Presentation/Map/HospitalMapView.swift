import SwiftUI

/// Harita sekmesi: hastaneler (mavi) ve Kızılay kan bağış noktaları (marka rengi), kümelemeli yerel harita.
/// Üstte katman seçici ve durum etiketi, altta seçili yerin kartı ya da konum izni uyarısı.
struct HospitalMapView: View {
    let container: AppContainer
    @StateObject private var viewModel: HospitalMapViewModel

    init(container: AppContainer) {
        self.container = container
        _viewModel = StateObject(wrappedValue: container.makeHospitalMapViewModel())
    }

    var body: some View {
        PlacesMapView(
            hospitals: viewModel.visibleHospitals,
            bloodPoints: viewModel.visibleBloodPoints,
            selectedMarkerID: viewModel.selection?.markerID,
            cameraCommand: viewModel.cameraCommand,
            onSelectHospital: { hospital in Task { await viewModel.select(hospital) } },
            onSelectBloodPoint: { viewModel.select($0) },
            onDeselect: { viewModel.clearSelection() },
            onCameraChange: { viewModel.cameraDidMove(to: $0) }
        )
        .ignoresSafeArea(edges: [.top, .bottom])
        .overlay(alignment: .top) { topControls }
        .overlay(alignment: .bottom) { bottomContent }
        .animation(.spring(response: 0.35, dampingFraction: 0.85), value: viewModel.selection)
        .animation(.easeInOut(duration: 0.2), value: viewModel.isLoading)
        .animation(.easeInOut(duration: 0.2), value: viewModel.canSearchVisibleArea)
        .navigationTitle("map.title")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button { Task { await viewModel.centerOnUser() } } label: { Image(systemName: "location") }
                    .tint(.primary)
                    .accessibilityLabel(Text("map.myLocation"))
            }
        }
        .task {
            await viewModel.start()
            #if DEBUG
            switch DebugLaunch.mapSelection {
            case "hospital": if let hospital = viewModel.hospitals.first { await viewModel.select(hospital) }
            case "point": if let point = viewModel.bloodPoints.first { viewModel.select(point) }
            default: break
            }
            #endif
        }
        .errorAlert($viewModel.error)
    }

    // MARK: Üst: katman seçici + durum

    private var topControls: some View {
        VStack(spacing: Theme.Spacing.s) {
            Picker("map.layer", selection: $viewModel.layer) {
                Text("map.layer.all").tag(HospitalMapViewModel.Layer.all)
                Text("map.layer.hospitals").tag(HospitalMapViewModel.Layer.hospitals)
                Text("map.layer.bloodPoints").tag(HospitalMapViewModel.Layer.bloodPoints)
            }
            .pickerStyle(.segmented)
            .padding(Theme.Spacing.xs)
            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous))
            .padding(.horizontal, Theme.Spacing.l)

            statusPill
        }
        .padding(.top, Theme.Spacing.s)
    }

    @ViewBuilder
    private var statusPill: some View {
        if viewModel.isLoading {
            HStack(spacing: Theme.Spacing.s) {
                ProgressView().controlSize(.small)
                Text("map.loading").font(.footnote.weight(.semibold))
            }
            .padding(.horizontal, Theme.Spacing.m).padding(.vertical, Theme.Spacing.s)
            .background(.regularMaterial, in: Capsule())
            .transition(.opacity)
        } else if viewModel.canSearchVisibleArea {
            Button {
                Haptics.impact()
                Task { await viewModel.searchVisibleArea() }
            } label: {
                Label("map.searchArea", systemImage: "arrow.clockwise").font(.footnote.weight(.semibold))
            }
            .buttonStyle(PrimaryPillButtonStyle(fullWidth: false))
            .shadow(color: .black.opacity(0.15), radius: 6, y: 2)
            .transition(.move(edge: .top).combined(with: .opacity))
        } else if viewModel.isEmptyResult {
            Text("map.empty").font(.footnote.weight(.semibold))
                .padding(.horizontal, Theme.Spacing.m).padding(.vertical, Theme.Spacing.s)
                .background(.regularMaterial, in: Capsule())
                .transition(.opacity)
        }
    }

    // MARK: Alt: seçili yer kartı ya da konum izni uyarısı

    @ViewBuilder
    private var bottomContent: some View {
        Group {
            switch viewModel.selection {
            case .hospital(let hospital):
                HospitalMapCard(
                    hospital: hospital, distance: viewModel.distance(to: hospital.coordinate),
                    requests: viewModel.selectedRequests, isLoadingRequests: viewModel.isLoadingRequests,
                    onClose: { viewModel.clearSelection() })
                    .id(hospital.id)
            case .bloodPoint(let point):
                BloodPointMapCard(point: point, distance: viewModel.distance(to: point.coordinate), onClose: { viewModel.clearSelection() })
                    .id(point.id)
            case nil:
                if viewModel.isLocationDenied { locationDeniedBanner }
            }
        }
        .padding(.horizontal, Theme.Spacing.m)
        .padding(.bottom, Theme.Spacing.m)
        .transition(.move(edge: .bottom).combined(with: .opacity))
    }

    private var locationDeniedBanner: some View {
        HStack(spacing: Theme.Spacing.m) {
            Image(systemName: "location.slash.fill").font(.title3).foregroundStyle(Theme.brand).accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text("map.locationDenied.title").font(.subheadline.weight(.semibold))
                Text("map.locationDenied.message").font(.footnote).foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
            Button("common.settings") { MapActions.openSettings() }
                .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
        }
        .padding(Theme.Spacing.m)
        .background(Theme.Palette.card, in: RoundedRectangle(cornerRadius: Theme.Radius.feedCard, style: .continuous))
        .shadow(color: .black.opacity(0.12), radius: 10, y: 3)
    }
}
