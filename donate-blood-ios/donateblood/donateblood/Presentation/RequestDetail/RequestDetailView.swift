import SwiftUI

/// İlan detayı (X gönderi detayı düzeni): yazar satırı, başlık/açıklama, zaman, bağış niyeti sayısı, bilgi bölümleri
/// ve altta sabit eylem çubuğu. Alt sekme çubuğu bu ekranda gizlidir.
struct RequestDetailView: View {
    @StateObject var viewModel: RequestDetailViewModel
    let container: AppContainer
    @EnvironmentObject private var session: SessionStore
    @Environment(\.dismiss) private var dismiss
    @State private var isConfirmingDelete = false
    @State private var isChoosingReportReason = false
    @State private var isConfirmingBlock = false
    @State private var isConfirmingToggleActive = false

    private var isOwner: Bool { session.user?.id == viewModel.detail?.request.ownerId }

    var body: some View {
        Group {
            switch viewModel.state {
            case .idle, .loading:
                ProgressView().controlSize(.large).frame(maxWidth: .infinity, maxHeight: .infinity)
            case .failed(let error):
                ErrorStateView(error: error) { Task { await viewModel.load(isGuest: session.isGuest) } }
            case .loaded:
                if let detail = viewModel.detail { post(detail) }
            }
        }
        .navigationTitle("detail.title")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { toolbar }
        .task(id: session.isGuest) { await viewModel.load(isGuest: session.isGuest) }
        .onChange(of: viewModel.didDelete) { if $0 { dismiss() } }
        .errorAlert($viewModel.error)
        .alert(item: $viewModel.infoMessage) { message in
            Alert(title: Text(message.text), dismissButton: .default(Text("common.ok")))
        }
        .confirmationDialog(
            viewModel.detail?.request.isActive == true ? "detail.deactivate.confirm" : "detail.activate.confirm",
            isPresented: $isConfirmingToggleActive, titleVisibility: .visible
        ) {
            Button(viewModel.detail?.request.isActive == true ? "detail.deactivate" : "detail.activate") {
                let makeActive = viewModel.detail?.request.isActive != true
                Task { await viewModel.setActive(makeActive, isGuest: session.isGuest) }
            }
        }
        .confirmationDialog("detail.delete.confirm", isPresented: $isConfirmingDelete, titleVisibility: .visible) {
            Button("common.delete", role: .destructive) { Task { await viewModel.delete() } }
        }
        .confirmationDialog("safety.reportReason", isPresented: $isChoosingReportReason, titleVisibility: .visible) {
            ForEach(ReportReason.forRequests) { reason in
                Button(reason.title) { Task { await viewModel.report(reason: reason.rawValue) } }
            }
        }
        .confirmationDialog("safety.block.confirm", isPresented: $isConfirmingBlock, titleVisibility: .visible) {
            Button("safety.block", role: .destructive) { Task { await viewModel.blockOwner() } }
        } message: { Text("safety.block.message") }
        .overlay { if viewModel.isWorking { ProgressView().padding().background(.regularMaterial, in: RoundedRectangle(cornerRadius: Theme.Radius.field)) } }
    }

    // MARK: Gönderi

    private func post(_ detail: GetBloodRequestDetailUseCase.Detail) -> some View {
        let request = detail.request
        return ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                author(request)
                Text(request.title).font(.title3.weight(.semibold))
                    .padding(.horizontal, Theme.Spacing.l).padding(.bottom, Theme.Spacing.s)
                if let description = request.description, !description.isEmpty {
                    Text(description).font(.body).padding(.horizontal, Theme.Spacing.l).padding(.bottom, Theme.Spacing.s)
                }
                Text(request.createdAt.formatted(date: .abbreviated, time: .shortened))
                    .font(.footnote).foregroundStyle(.secondary)
                    .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)

                HairlineDivider()
                HStack(spacing: Theme.Spacing.xs) {
                    Text(verbatim: "\(detail.pledgeCount)").font(.subheadline.weight(.bold))
                    Text("detail.pledgeLabel").font(.subheadline).foregroundStyle(.secondary)
                }
                .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
                .accessibilityElement(children: .combine)
                HairlineDivider()

                InfoSection(title: "detail.patientInfo", systemImage: "person.crop.circle") {
                    InfoRow(label: "request.patientName", value: request.patientFullName, valueColor: .primary)
                    if let age = request.patientAge { InfoRow(label: "request.patientAge", value: "\(age)") }
                    InfoRow(label: "profile.bloodType", value: request.bloodType.displayName, valueColor: Theme.brand)
                }

                if let hospital = request.hospital {
                    HairlineDivider().padding(.top, Theme.Spacing.m)
                    InfoSection(title: "detail.hospitalInfo", systemImage: "cross.case") {
                        NavigationLink(value: Route.hospital(hospital)) {
                            HStack(spacing: Theme.Spacing.m) {
                                VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                                    Text(hospital.name).font(.body.weight(.semibold))
                                    if let address = hospital.address, !address.isEmpty {
                                        Text(address).font(.subheadline).foregroundStyle(.secondary)
                                    }
                                }
                                Spacer(minLength: 0)
                                Image(systemName: "chevron.right").font(.footnote).foregroundStyle(.tertiary)
                            }
                            .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }

                HairlineDivider().padding(.top, Theme.Spacing.m)
                contact(detail)

                if !isOwner && !session.isGuest {
                    HairlineDivider().padding(.top, Theme.Spacing.m)
                    NavigationLink(value: Route.healthForm) {
                        HStack {
                            Label("detail.healthCheck", systemImage: "heart.text.square").foregroundStyle(.primary)
                            Spacer()
                            Image(systemName: "chevron.right").font(.footnote).foregroundStyle(.tertiary)
                        }
                        .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }

                Text("detail.pledgeFooter").font(.footnote).foregroundStyle(.secondary)
                    .padding(Theme.Spacing.l)
                Color.clear.frame(height: 72) // alt eylem çubuğunun altında kalan içerik için boşluk
            }
        }
        .refreshable { await viewModel.load(isGuest: session.isGuest) }
        .safeAreaInset(edge: .bottom) { actionBar(detail) }
    }

    private func author(_ request: BloodRequest) -> some View {
        HStack(spacing: Theme.Spacing.m) {
            BloodTypeAvatar(bloodType: request.bloodType, size: 48)
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: Theme.Spacing.xs) {
                    Text(request.ownerName ?? request.patientFullName).font(.headline).lineLimit(1)
                    if request.isVerified {
                        Image(systemName: "checkmark.seal.fill").foregroundStyle(Theme.Palette.verified)
                            .accessibilityLabel(Text("bloodRequest.verified"))
                    }
                }
                Text(request.createdAt.relativeDescription).font(.subheadline).foregroundStyle(.secondary)
            }
            Spacer(minLength: Theme.Spacing.s)
            if request.isEmergency { PillBadge(text: "bloodRequest.urgent") }
            if !request.isActive { PillBadge(text: "bloodRequest.closed", color: Theme.Palette.warning) }
        }
        .padding(Theme.Spacing.l)
    }

    @ViewBuilder
    private func contact(_ detail: GetBloodRequestDetailUseCase.Detail) -> some View {
        InfoSection(title: "detail.contactInfo", systemImage: "phone") {
            if let phones = detail.phoneNumbers {
                ForEach(phones, id: \.self) { phone in
                    if let url = URL(string: "tel:\(phone.filter { $0.isNumber || $0 == "+" })") {
                        Link(destination: url) {
                            HStack {
                                Label(phone, systemImage: "phone.fill").foregroundStyle(Theme.brand)
                                Spacer()
                            }
                            .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
                            .contentShape(Rectangle())
                        }
                    }
                }
            } else {
                Button { session.isAuthSheetPresented = true } label: {
                    HStack {
                        Label("detail.signInToSeeContact", systemImage: "lock").foregroundStyle(.secondary)
                        Spacer()
                    }
                    .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
                    .contentShape(Rectangle())
                }
            }
        }
    }

    // MARK: Alt eylem çubuğu

    private func actionBar(_ detail: GetBloodRequestDetailUseCase.Detail) -> some View {
        let request = detail.request
        return VStack(spacing: 0) {
            HairlineDivider()
            HStack(spacing: Theme.Spacing.m) {
                if isOwner {
                    NavigationLink(value: Route.editRequest(request)) {
                        Label("common.edit", systemImage: "pencil")
                    }
                    .buttonStyle(SecondaryPillButtonStyle())
                    Button { isConfirmingToggleActive = true } label: {
                        Label(request.isActive ? "detail.deactivate" : "detail.activate",
                              systemImage: request.isActive ? "pause.circle" : "play.circle")
                    }
                    .buttonStyle(SecondaryPillButtonStyle())
                } else if request.isActive {
                    Button {
                        Haptics.impact()
                        session.requireAccount { Task { await viewModel.setPledged(!detail.hasPledged, isGuest: session.isGuest) } }
                    } label: {
                        Label(detail.hasPledged ? "detail.withdrawPledge" : "detail.pledge",
                              systemImage: detail.hasPledged ? "heart.slash" : "heart.fill")
                    }
                    .buttonStyle(PrimaryPillButtonStyle())

                    if session.isGuest {
                        Button { session.isAuthSheetPresented = true } label: { Image(systemName: "bubble.left").frame(minWidth: 24) }
                            .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
                            .accessibilityLabel(Text("detail.message"))
                    } else {
                        NavigationLink(value: Route.chat(.user(id: request.ownerId, name: request.ownerName ?? L10n.string("chat.unknownUser")))) {
                            Image(systemName: "bubble.left").frame(minWidth: 24)
                        }
                        .buttonStyle(SecondaryPillButtonStyle(fullWidth: false))
                        .accessibilityLabel(Text("detail.message"))
                    }
                }
            }
            .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.s)
        }
        .background(.bar)
    }

    @ToolbarContentBuilder
    private var toolbar: some ToolbarContent {
        if let request = viewModel.detail?.request {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                ShareLink(item: shareText(for: request)) { Image(systemName: "square.and.arrow.up") }
                    .tint(.primary)
                if isOwner {
                    Button { isConfirmingDelete = true } label: { Image(systemName: "trash") }
                        .tint(.primary)
                        .accessibilityLabel(Text("common.delete"))
                } else if !session.isGuest {
                    Menu {
                        Button { isChoosingReportReason = true } label: { Label("safety.report", systemImage: "flag") }
                        Button(role: .destructive) { isConfirmingBlock = true } label: { Label("safety.block", systemImage: "hand.raised") }
                    } label: { Image(systemName: "ellipsis") }
                    .tint(.primary)
                }
            }
        }
    }

    private func shareText(for request: BloodRequest) -> String {
        L10n.format("detail.shareText %@ %@ %@", request.bloodType.displayName, request.hospital?.name ?? "-", request.title)
    }
}
