import SwiftUI

struct RequestFormView: View {
    @StateObject var viewModel: RequestFormViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var isPickingHospital = false
    @State private var didApplyPrefill = false

    var body: some View {
        Form {
            Section("request.patientSection") {
                TextField("request.patientName", text: $viewModel.draft.patientFullName)
                    .textContentType(.name)
                TextField("request.patientAge", text: $viewModel.ageText).keyboardType(.numberPad)
                Picker("profile.bloodType", selection: $viewModel.draft.bloodType) {
                    Text("registration.select").tag(BloodType?.none)
                    ForEach(BloodType.allCases) { Text(verbatim: $0.displayName).tag(BloodType?.some($0)) }
                }
            }

            Section("request.detailsSection") {
                TextField("request.titleField", text: $viewModel.draft.title)
                TextField("request.description", text: $viewModel.draft.description, axis: .vertical)
                    .lineLimit(3...8)
                Button { isPickingHospital = true } label: {
                    HStack {
                        LabeledContent("request.hospital", value: viewModel.selectedHospital?.name ?? L10n.string("request.selectHospital"))
                        Image(systemName: "chevron.right").font(.footnote.weight(.semibold)).foregroundStyle(.tertiary)
                    }
                }
                .tint(.primary)
            }

            Section {
                Toggle(isOn: $viewModel.draft.isEmergency) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("request.emergency")
                        Text("request.emergencyDescription").font(.caption).foregroundStyle(.secondary)
                    }
                }
                .tint(.red)
            }

            Section {
                ForEach(viewModel.draft.phoneNumbers.indices, id: \.self) { index in
                    TextField("request.phone", text: $viewModel.draft.phoneNumbers[index])
                        .keyboardType(.phonePad)
                        .textContentType(.telephoneNumber)
                }
                .onDelete(perform: viewModel.removePhone)
                if viewModel.draft.phoneNumbers.count < 3 {
                    Button { viewModel.addPhone() } label: { Label("request.addPhone", systemImage: "plus.circle") }
                }
            } header: { Text("request.phones") } footer: { Text("request.phonesFooter") }

            if !viewModel.isEditing {
                Section {
                    Toggle("request.acceptTerms", isOn: $viewModel.draft.acceptedTerms)
                    Link("settings.terms", destination: AppLinks.terms)
                    Link("settings.privacy", destination: AppLinks.privacy)
                }
            }
        }
        .navigationTitle(viewModel.isEditing ? "request.editTitle" : "bloodRequest.create")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) { Button("common.cancel") { dismiss() } }
            ToolbarItem(placement: .confirmationAction) {
                if viewModel.isSaving { ProgressView() } else { Button("common.save") { Task { await viewModel.save() } } }
            }
        }
        .navigationDestination(isPresented: $isPickingHospital) {
            HospitalPickerView(getHospitals: viewModel.getHospitals) { viewModel.select($0) }
        }
        .onAppear {
            // Kısayoldan gelindiyse hastane seçimi bir kez açılır (geri dönünce tekrar açılmaz).
            guard !didApplyPrefill else { return }
            didApplyPrefill = true
            if viewModel.opensHospitalPicker { isPickingHospital = true }
        }
        .task { await viewModel.loadContactsIfEditing() }
        .onChange(of: viewModel.didSave) { if $0 { dismiss() } }
        .errorAlert($viewModel.error)
        .interactiveDismissDisabled(viewModel.isSaving)
    }
}

/// Hastane seçimi: arama destekli native liste.
struct HospitalPickerView: View {
    let getHospitals: GetHospitalsUseCase
    let onSelect: (Hospital) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var query = ""
    @State private var hospitals: [Hospital] = []
    @State private var isLoading = true
    @State private var error: AppError?

    var body: some View {
        List(hospitals) { hospital in
            Button {
                onSelect(hospital)
                dismiss()
            } label: {
                VStack(alignment: .leading, spacing: 2) {
                    Text(hospital.name).foregroundStyle(.primary)
                    if !hospital.locationDescription.isEmpty {
                        Text(hospital.locationDescription).font(.caption).foregroundStyle(.secondary)
                    }
                }
            }
        }
        .overlay {
            if isLoading { ProgressView() }
            else if hospitals.isEmpty { EmptyStateView(title: "hospital.empty", systemImage: "cross.case") }
        }
        .navigationTitle("request.hospital")
        .searchable(text: $query, prompt: Text("hospital.searchPrompt"))
        .task(id: query) {
            if !query.isEmpty { try? await Task.sleep(nanoseconds: 300_000_000) }
            guard !Task.isCancelled else { return }
            do {
                hospitals = try await getHospitals(search: query)
            } catch {
                let appError = AppError.from(error)
                if appError != .cancelled { self.error = appError }
            }
            isLoading = false
        }
        .errorAlert($error)
    }
}
