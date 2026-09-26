import SwiftUI

/// Kayıt akışı: üstte ilerleme çubuğu, her adımda büyük başlık + alanlar, altta sabit birincil düğme.
struct RegistrationView: View {
    @StateObject var viewModel: RegistrationViewModel
    @State private var isConfirmingSignOut = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ProgressView(value: viewModel.progress)
                    .tint(Theme.brand)
                    .padding(.horizontal, Theme.Spacing.l)
                    .accessibilityLabel(Text("registration.progress \(viewModel.step.rawValue + 1) \(RegistrationViewModel.Step.allCases.count)"))
                    .animation(.easeInOut, value: viewModel.step)

                Group {
                    switch viewModel.step {
                    case .personal: PersonalStepView(viewModel: viewModel)
                    case .donor: DonorStepView(viewModel: viewModel)
                    case .areas: AreasStepView(picker: viewModel.areaPicker)
                    case .notifications: NotificationsStepView()
                    }
                }
                .transition(.asymmetric(insertion: .move(edge: .trailing).combined(with: .opacity),
                                        removal: .move(edge: .leading).combined(with: .opacity)))
            }
            .animation(.easeInOut(duration: 0.25), value: viewModel.step)
            .safeAreaInset(edge: .bottom) { footer }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { toolbar }
        }
        .errorAlert($viewModel.error)
        .confirmationDialog("registration.signOut.confirm", isPresented: $isConfirmingSignOut, titleVisibility: .visible) {
            Button("registration.signOut") { Task { await viewModel.signOut() } }
        }
    }

    @ToolbarContentBuilder
    private var toolbar: some ToolbarContent {
        ToolbarItem(placement: .navigationBarLeading) {
            if viewModel.canGoBack {
                Button { viewModel.goBack() } label: { Image(systemName: "chevron.backward") }
                    .tint(.primary)
                    .accessibilityLabel(Text("common.back"))
            }
        }
        ToolbarItem(placement: .navigationBarTrailing) {
            if viewModel.step != .notifications {
                Menu {
                    Button { isConfirmingSignOut = true } label: {
                        Label("registration.signOut", systemImage: "rectangle.portrait.and.arrow.right")
                    }
                } label: { Image(systemName: "ellipsis") }
                .tint(.primary)
                .accessibilityLabel(Text("common.more"))
            }
        }
    }

    @ViewBuilder
    private var footer: some View {
        VStack(spacing: Theme.Spacing.s) {
            switch viewModel.step {
            case .personal:
                Button("registration.next") { viewModel.continueFromPersonal() }
                    .buttonStyle(PrimaryPillButtonStyle())
            case .donor:
                Button("registration.next") { viewModel.continueFromDonor() }
                    .buttonStyle(PrimaryPillButtonStyle())
            case .areas:
                AreasFooter(viewModel: viewModel, picker: viewModel.areaPicker)
            case .notifications:
                Button("registration.notifications.enable") { Task { await viewModel.enableNotifications() } }
                    .buttonStyle(PrimaryPillButtonStyle())
                Button("registration.notifications.later") { Task { await viewModel.skipNotifications() } }
                    .buttonStyle(.plain).font(.subheadline.weight(.semibold)).foregroundStyle(.secondary)
                    .frame(minHeight: Theme.Size.minTap)
            }
        }
        .disabled(viewModel.isFinishing)
        .padding(.horizontal, Theme.Spacing.xl)
        .padding(.top, Theme.Spacing.s)
        .padding(.bottom, Theme.Spacing.s)
        .background(Theme.Palette.background)
        .overlay(alignment: .top) { HairlineDivider() }
    }
}

// MARK: - 1. Kişisel bilgiler

private struct PersonalStepView: View {
    @ObservedObject var viewModel: RegistrationViewModel
    @FocusState private var focus: ProfileField?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Theme.Spacing.xl) {
                StepHeader(title: "registration.personal.title", subtitle: "registration.personal.subtitle")
                FormField(label: "profile.name", error: viewModel.error(for: .name)?.message) {
                    TextField("profile.name", text: $viewModel.name)
                        .textContentType(.givenName).textInputAutocapitalization(.words)
                        .submitLabel(.next).focused($focus, equals: .name)
                        .onSubmit { focus = .surname }
                }
                FormField(label: "profile.surname", error: viewModel.error(for: .surname)?.message) {
                    TextField("profile.surname", text: $viewModel.surname)
                        .textContentType(.familyName).textInputAutocapitalization(.words)
                        .submitLabel(.next).focused($focus, equals: .surname)
                        .onSubmit { focus = .phone }
                }
                FormField(label: "profile.phone", isOptional: true, error: viewModel.error(for: .phone)?.message,
                          hint: "registration.phone.hint") {
                    TextField("registration.phone.placeholder", text: $viewModel.phone)
                        .textContentType(.telephoneNumber).keyboardType(.phonePad)
                        .focused($focus, equals: .phone)
                }
            }
            .padding(Theme.Spacing.xl)
        }
        .scrollDismissesKeyboard(.interactively)
        .onAppear { if viewModel.name.isEmpty { focus = .name } }
    }
}

// MARK: - 2. Bağışçı bilgileri

private struct DonorStepView: View {
    @ObservedObject var viewModel: RegistrationViewModel
    @State private var isPickingBirthDate = false

    private let columns = Array(repeating: GridItem(.flexible(), spacing: Theme.Spacing.s), count: 4)

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Theme.Spacing.xl) {
                StepHeader(title: "registration.donor.title", subtitle: "registration.donor.subtitle")
                bloodTypeSection
                birthDateSection
                genderSection
                Label("registration.privacy", systemImage: "lock.fill")
                    .font(.footnote).foregroundStyle(.secondary)
            }
            .padding(Theme.Spacing.xl)
        }
    }

    private var bloodTypeSection: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.s) {
            Text("profile.bloodType").font(.subheadline.weight(.semibold))
            LazyVGrid(columns: columns, spacing: Theme.Spacing.s) {
                ForEach(BloodType.allCases) { type in
                    choiceTile(Text(type.displayName).font(.title3.weight(.bold)), isSelected: viewModel.bloodType == .known(type)) {
                        viewModel.bloodType = .known(type)
                    }
                    .accessibilityLabel(Text(verbatim: type.displayName))
                }
            }
            choiceTile(Text("registration.bloodType.unknown").font(.subheadline.weight(.semibold)), isSelected: viewModel.bloodType == .unknown) {
                viewModel.bloodType = .unknown
            }
            if viewModel.isBloodTypeMissing {
                Label("registration.bloodType.required", systemImage: "exclamationmark.circle.fill").font(.footnote).foregroundStyle(.red)
            } else if viewModel.bloodType == .unknown {
                Text("registration.bloodType.unknownHint").font(.footnote).foregroundStyle(.secondary)
            }
        }
    }

    private func choiceTile(_ label: Text, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button {
            Haptics.selection()
            action()
        } label: {
            label
                .foregroundStyle(isSelected ? Color.white : Color.primary)
                .frame(maxWidth: .infinity, minHeight: 52)
                .background(isSelected ? Theme.brand : Theme.Palette.surface,
                            in: RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous)
                    .stroke(viewModel.isBloodTypeMissing ? Color.red.opacity(0.6) : Color.clear, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    private var birthDateSection: some View {
        FormField(label: "profile.birthDate", error: viewModel.error(for: .birthDate)?.message, hint: "registration.birthDate.hint") {
            VStack(alignment: .leading, spacing: Theme.Spacing.s) {
                Button {
                    if viewModel.birthDate == nil { viewModel.birthDate = viewModel.suggestedBirthDate }
                    withAnimation { isPickingBirthDate.toggle() }
                } label: {
                    HStack {
                        if let date = viewModel.birthDate {
                            Text(date.formatted(date: .long, time: .omitted)).foregroundStyle(.primary)
                        } else {
                            Text("registration.select").foregroundStyle(.secondary)
                        }
                        Spacer()
                        Image(systemName: isPickingBirthDate ? "chevron.up" : "calendar").foregroundStyle(.secondary)
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                if isPickingBirthDate {
                    DatePicker("profile.birthDate",
                               selection: Binding(get: { viewModel.birthDate ?? viewModel.suggestedBirthDate },
                                                  set: { viewModel.birthDate = $0 }),
                               in: viewModel.birthDateRange, displayedComponents: .date)
                        .datePickerStyle(.wheel)
                        .labelsHidden()
                        .frame(maxWidth: .infinity)
                }
            }
        }
    }

    private var genderSection: some View {
        FormField(label: "profile.gender", isOptional: true) {
            Menu {
                Picker("profile.gender", selection: $viewModel.gender) {
                    Text("registration.gender.unspecified").tag(Gender?.none)
                    ForEach(Gender.allCases) { Text($0.localizedName).tag(Gender?.some($0)) }
                }
            } label: {
                HStack {
                    Text(viewModel.gender?.localizedName ?? L10n.string("registration.gender.unspecified"))
                        .foregroundStyle(viewModel.gender == nil ? Color.secondary : Color.primary)
                    Spacer()
                    Image(systemName: "chevron.up.chevron.down").font(.footnote).foregroundStyle(Color.secondary)
                }
                .contentShape(Rectangle())
            }
            .tint(.primary)
        }
    }
}

// MARK: - 3. Bildirim bölgeleri

private struct AreasStepView: View {
    @ObservedObject var picker: NotificationAreaPickerModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Theme.Spacing.xl) {
                StepHeader(title: "registration.areas.title", subtitle: "registration.areas.subtitle")
                NotificationAreaPicker(model: picker)
            }
            .padding(Theme.Spacing.xl)
        }
        .scrollDismissesKeyboard(.interactively)
    }
}

/// Seçim yoksa "Devam" pasif; "Şimdilik atla" bölge seçmeden (ilan bildirimi almadan) devam eder.
private struct AreasFooter: View {
    @ObservedObject var viewModel: RegistrationViewModel
    @ObservedObject var picker: NotificationAreaPickerModel
    @State private var isConfirmingSkip = false

    var body: some View {
        Button { Task { await viewModel.continueFromAreas() } } label: {
            HStack(spacing: Theme.Spacing.s) {
                if viewModel.isSaving { ProgressView().tint(.white) }
                Text("registration.save")
            }
        }
        .buttonStyle(PrimaryPillButtonStyle())
        .disabled(viewModel.isSaving || picker.selectedKeys.isEmpty)

        Button("registration.areas.skip") { isConfirmingSkip = true }
            .buttonStyle(.plain).font(.subheadline.weight(.semibold)).foregroundStyle(.secondary)
            .frame(minHeight: Theme.Size.minTap)
            .disabled(viewModel.isSaving)
            .confirmationDialog("registration.areas.skip.title", isPresented: $isConfirmingSkip, titleVisibility: .visible) {
                Button("registration.areas.skip.confirm") { Task { await viewModel.skipAreas() } }
            } message: {
                Text("registration.areas.skip.message")
            }
    }
}

// MARK: - 4. Bildirim izni (önce açıklama, sonra sistem penceresi)

private struct NotificationsStepView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Theme.Spacing.xl) {
                Image(systemName: "bell.badge.fill")
                    .font(.system(size: 44, weight: .semibold)).foregroundStyle(Theme.brand)
                    .frame(width: 96, height: 96).background(Theme.brand.opacity(0.12), in: Circle())
                    .accessibilityHidden(true)
                StepHeader(title: "registration.notifications.title", subtitle: "registration.notifications.subtitle")
                VStack(alignment: .leading, spacing: Theme.Spacing.m) {
                    FeatureRow(systemImage: "drop.fill", text: "registration.notifications.item1")
                    FeatureRow(systemImage: "heart.fill", text: "registration.notifications.item2")
                    FeatureRow(systemImage: "bubble.left.fill", text: "registration.notifications.item3")
                }
                Text("registration.notifications.footer").font(.footnote).foregroundStyle(.secondary)
            }
            .padding(Theme.Spacing.xl)
        }
    }
}
