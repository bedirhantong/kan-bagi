import SwiftUI

/// Android'deki "Kan Bağışı Uygunluk Formu": her soru için kart ve Evet/Hayır seçimi.
struct HealthFormView: View {
    @StateObject var viewModel: HealthFormViewModel
    @EnvironmentObject private var session: SessionStore

    var body: some View {
        Group {
            switch viewModel.state {
            case .idle, .loading: ProgressView().controlSize(.large)
            case .failed(let error): ErrorStateView(error: error) { Task { await viewModel.load() } }
            case .loaded: form
            }
        }
        .navigationTitle("health.title")
        .navigationBarTitleDisplayMode(.inline)
        .task { if !session.isGuest { await viewModel.load() } }
        .errorAlert($viewModel.error)
        .overlay {
            if session.isGuest {
                EmptyStateView(
                    title: "health.guest.title", systemImage: "heart.text.square", message: "health.guest.message",
                    actionTitle: "auth.signIn", action: { session.isAuthSheetPresented = true }
                )
                .background(Color(.systemBackground))
            }
        }
    }

    private var form: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("health.header").font(.title2.bold())
                    Text("health.subheader").font(.subheadline).foregroundStyle(.secondary)
                    Text("health.intro").font(.footnote).foregroundStyle(.secondary).padding(.top, 4)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))

                ForEach(HealthCatalog.questions) { question in
                    VStack(alignment: .leading, spacing: 12) {
                        Text(question.localizedText).font(.body)
                        Picker(question.localizedText, selection: Binding(
                            get: { viewModel.binding(for: question.key) },
                            set: { viewModel.answers[question.key] = $0 }
                        )) {
                            Text("health.yes").tag(true)
                            Text("health.no").tag(false)
                        }
                        .pickerStyle(.segmented)
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
                }

                resultCard
            }
            .padding()
        }
        .safeAreaInset(edge: .bottom) {
            Button { Task { await viewModel.save() } } label: {
                HStack {
                    Text("common.save").textCase(.uppercase).bold()
                    if viewModel.isSaving { ProgressView().tint(.white) }
                    if viewModel.didSave { Image(systemName: "checkmark") }
                }
                .frame(maxWidth: .infinity, minHeight: 32)
            }
            .buttonStyle(.borderedProminent).tint(.black).controlSize(.large)
            .disabled(viewModel.isSaving)
            .padding(.horizontal).padding(.vertical, 8)
            .background(.bar)
        }
    }

    private var resultCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("health.result").font(.headline)
            if viewModel.flagged.isEmpty {
                Label("health.result.ok", systemImage: "checkmark.circle.fill").foregroundStyle(.green)
            } else {
                Label("health.result.attention", systemImage: "exclamationmark.triangle.fill").foregroundStyle(.orange)
                ForEach(viewModel.flagged) { Text("• " + $0.localizedText).font(.footnote) }
            }
            Text("health.disclaimer").font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}
