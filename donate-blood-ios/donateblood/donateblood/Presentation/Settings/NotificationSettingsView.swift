import SwiftUI
import UserNotifications

/// Bildirim Ayarları: kanallar, bildirim bölgesi (il/ilçe ve hastaneler) ve kan grupları.
struct NotificationSettingsView: View {
    @StateObject var viewModel: NotificationSettingsViewModel
    @State private var systemDenied = false

    var body: some View {
        Group {
            switch viewModel.state {
            case .idle, .loading: ProgressView().controlSize(.large)
            case .failed(let error): ErrorStateView(error: error) { Task { await viewModel.load() } }
            case .loaded: form
            }
        }
        .navigationTitle("settings.notifications")
        .task {
            await viewModel.load()
            systemDenied = await UNUserNotificationCenter.current().notificationSettings().authorizationStatus == .denied
        }
        .errorAlert($viewModel.error)
    }

    private var form: some View {
        Form {
            if systemDenied {
                Section {
                    Button("notificationSettings.openSystem") {
                        if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
                    }
                } footer: { Text("notificationSettings.systemDenied") }
            }

            Section {
                Toggle(isOn: binding(\.pushEnabled)) { toggleLabel("notificationSettings.push", "notificationSettings.push.subtitle") }
                Toggle(isOn: binding(\.bloodTypeAlerts)) { toggleLabel("notificationSettings.bloodAlerts", "notificationSettings.bloodAlerts.subtitle") }
                    .disabled(!viewModel.preferences.pushEnabled)
                Toggle(isOn: binding(\.chatNotifications)) { toggleLabel("notificationSettings.chat", "notificationSettings.chat.subtitle") }
                    .disabled(!viewModel.preferences.pushEnabled)
            } header: { Text("notificationSettings.channels") }

            Section {
                if viewModel.isMissingScope {
                    Label("notificationSettings.scope.missing", systemImage: "exclamationmark.triangle.fill")
                        .font(.subheadline).foregroundStyle(.orange)
                }
                NavigationLink {
                    NotificationAreaPreferenceView(model: viewModel.makeAreaPicker())
                } label: {
                    LabeledContent("notificationSettings.areas", value: areaSummary)
                }
                NavigationLink {
                    HospitalPreferenceView(getHospitals: viewModel.getHospitals, selection: viewModel.selectedHospitals,
                                           limit: viewModel.hospitalLimit) { hospitals in
                        Task { await viewModel.setHospitals(hospitals) }
                    }
                } label: {
                    LabeledContent("notificationSettings.hospitals", value: hospitalSummary)
                }
            } header: { Text("notificationSettings.scope") } footer: {
                Text(L10n.format("notificationSettings.scope.footer %lld", NotificationScope.maxSelections))
            }
            .disabled(!viewModel.preferences.pushEnabled || !viewModel.preferences.bloodTypeAlerts)

            Section {
                NavigationLink {
                    BloodTypePreferenceView(selection: viewModel.preferences.preferredBloodTypes) { selection in
                        Task { await viewModel.update { $0.preferredBloodTypes = selection } }
                    }
                } label: {
                    LabeledContent("notificationSettings.bloodTypes", value: bloodTypeSummary)
                }
            } footer: { Text("notificationSettings.footer") }
            .disabled(!viewModel.preferences.pushEnabled || !viewModel.preferences.bloodTypeAlerts)
        }
    }

    private var bloodTypeSummary: String {
        let selected = viewModel.preferences.preferredBloodTypes
        return selected.isEmpty
            ? L10n.string("notificationSettings.bloodTypes.none")
            : BloodType.allCases.filter(selected.contains).map(\.displayName).joined(separator: ", ")
    }

    private var areaSummary: String {
        let keys = viewModel.preferences.preferredAreaKeys
        guard !keys.isEmpty else { return L10n.string("notificationSettings.areas.none") }
        let names = viewModel.selectedAreas.map { $0.district ?? $0.city }
        // Adlar yüklenemediyse ya da çoksa: ilk ad + kalan sayı.
        guard let first = names.first else { return L10n.format("notificationSettings.areas.count %lld", keys.count) }
        return keys.count == 1 ? first : L10n.format("notificationSettings.areas.more %@ %lld", first, keys.count - 1)
    }

    private var hospitalSummary: String {
        let count = viewModel.selectedHospitals.count
        return count == 0 ? L10n.string("notificationSettings.hospitals.none") : L10n.format("notificationSettings.hospitals.count %lld", count)
    }

    private func toggleLabel(_ title: LocalizedStringKey, _ subtitle: LocalizedStringKey) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
            Text(subtitle).font(.caption).foregroundStyle(.secondary)
        }
    }

    private func binding(_ keyPath: WritableKeyPath<NotificationPreferences, Bool>) -> Binding<Bool> {
        Binding(
            get: { viewModel.preferences[keyPath: keyPath] },
            set: { newValue in Task { await viewModel.update { $0[keyPath: keyPath] = newValue } } }
        )
    }
}

/// Kan grubu çoklu seçimi.
struct BloodTypePreferenceView: View {
    @State var selection: Set<BloodType>
    let onChange: (Set<BloodType>) -> Void

    init(selection: Set<BloodType>, onChange: @escaping (Set<BloodType>) -> Void) {
        _selection = State(initialValue: selection)
        self.onChange = onChange
    }

    var body: some View {
        List {
            Section { ForEach(BloodType.allCases) { type in
                Button {
                    if selection.contains(type) { selection.remove(type) } else { selection.insert(type) }
                    onChange(selection)
                } label: {
                    HStack {
                        Text(type.displayName).foregroundStyle(.primary)
                        Spacer()
                        if selection.contains(type) { Image(systemName: "checkmark").foregroundStyle(.red) }
                    }
                }
                .accessibilityAddTraits(selection.contains(type) ? .isSelected : [])
            } } footer: { Text("notificationSettings.bloodTypes.footer") }
        }
        .navigationTitle("notificationSettings.bloodTypes")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("notificationSettings.clear") { selection = []; onChange([]) }.disabled(selection.isEmpty)
            }
        }
    }
}

/// Hastane çoklu seçimi (arama destekli).
struct HospitalPreferenceView: View {
    let getHospitals: GetHospitalsUseCase
    /// En fazla seçilebilecek hastane sayısı (il/ilçelerle ortak sınırdan kalan).
    let limit: Int
    let onChange: ([Hospital]) -> Void

    @State private var selected: [Hospital]
    @State private var query = ""
    @State private var results: [Hospital] = []
    @State private var isLoading = true

    init(getHospitals: GetHospitalsUseCase, selection: [Hospital], limit: Int, onChange: @escaping ([Hospital]) -> Void) {
        self.getHospitals = getHospitals
        self.limit = limit
        self.onChange = onChange
        _selected = State(initialValue: selection)
    }

    private var rows: [Hospital] {
        // Seçili olanlar her zaman üstte, sonra arama sonuçları.
        let selectedIds = Set(selected.map(\.id))
        return selected + results.filter { !selectedIds.contains($0.id) }
    }

    var body: some View {
        List(rows) { hospital in
            let isSelected = selected.contains { $0.id == hospital.id }
            let isEnabled = isSelected || selected.count < limit
            Button {
                if isSelected { selected.removeAll { $0.id == hospital.id } } else { selected.append(hospital) }
                onChange(selected)
            } label: {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(hospital.name).foregroundStyle(.primary)
                        if !hospital.locationDescription.isEmpty {
                            Text(hospital.locationDescription).font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    Spacer()
                    if isSelected { Image(systemName: "checkmark").foregroundStyle(Theme.brand) }
                }
            }
            .disabled(!isEnabled)
            .accessibilityAddTraits(isSelected ? .isSelected : [])
        }
        .safeAreaInset(edge: .bottom) {
            if selected.count >= limit {
                Text(L10n.format("error.selectionLimit %lld", NotificationScope.maxSelections))
                    .font(.footnote).foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity).padding(Theme.Spacing.m).background(.bar)
            }
        }
        .overlay { if isLoading { ProgressView() } }
        .navigationTitle("notificationSettings.hospitals")
        .searchable(text: $query, prompt: Text("hospital.searchPrompt"))
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("notificationSettings.clear") { selected = []; onChange([]) }.disabled(selected.isEmpty)
            }
        }
        .task(id: query) {
            if !query.isEmpty { try? await Task.sleep(nanoseconds: 300_000_000) }
            guard !Task.isCancelled else { return }
            results = (try? await getHospitals(search: query)) ?? []
            isLoading = false
        }
    }
}
