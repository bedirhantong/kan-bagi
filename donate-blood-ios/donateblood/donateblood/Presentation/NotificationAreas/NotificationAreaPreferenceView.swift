import SwiftUI

/// Bildirim Ayarları → İl ve ilçeler. Her değişiklik hemen kaydedilir.
struct NotificationAreaPreferenceView: View {
    @StateObject private var model: NotificationAreaPickerModel

    init(model: @escaping @autoclosure () -> NotificationAreaPickerModel) {
        _model = StateObject(wrappedValue: model())
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: Theme.Spacing.xl) {
                Text("notificationAreas.intro").font(.subheadline).foregroundStyle(.secondary)
                    .fixedSize(horizontal: false, vertical: true)
                NotificationAreaPicker(model: model)
            }
            .padding(Theme.Spacing.xl)
        }
        .scrollDismissesKeyboard(.interactively)
        .navigationTitle("notificationAreas.title")
        .navigationBarTitleDisplayMode(.inline)
    }
}
