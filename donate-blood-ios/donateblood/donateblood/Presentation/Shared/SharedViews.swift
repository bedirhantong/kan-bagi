import SwiftUI

/// Yüklenme durumu için ortak enum.
enum LoadState: Equatable {
    case idle, loading, loaded, failed(AppError)
}

/// Hata için standart uyarı penceresi.
extension View {
    func errorAlert(_ error: Binding<AppError?>) -> some View {
        alert(
            Text("common.error"),
            isPresented: Binding(get: { error.wrappedValue?.errorDescription != nil }, set: { if !$0 { error.wrappedValue = nil } }),
            presenting: error.wrappedValue
        ) { _ in
            Button("common.ok", role: .cancel) {}
        } message: { error in
            Text(error.errorDescription ?? "")
        }
    }
}

/// iOS 17+ `ContentUnavailableView`, öncesinde eşdeğer native görünüm.
struct EmptyStateView: View {
    let title: LocalizedStringKey
    let systemImage: String
    var message: LocalizedStringKey?
    var actionTitle: LocalizedStringKey?
    var action: (() -> Void)?

    var body: some View {
        if #available(iOS 17.0, *) {
            ContentUnavailableView {
                Label(title, systemImage: systemImage)
            } description: {
                if let message { Text(message) }
            } actions: {
                if let actionTitle, let action { Button(actionTitle, action: action).buttonStyle(.bordered) }
            }
        } else {
            VStack(spacing: 12) {
                Image(systemName: systemImage).font(.system(size: 44)).foregroundStyle(.secondary)
                Text(title).font(.headline)
                if let message { Text(message).font(.subheadline).foregroundStyle(.secondary).multilineTextAlignment(.center) }
                if let actionTitle, let action { Button(actionTitle, action: action).buttonStyle(.bordered) }
            }
            .padding()
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

/// Hata durumunda "tekrar dene" içeren tam ekran görünüm.
struct ErrorStateView: View {
    let error: AppError
    let retry: () -> Void

    var body: some View {
        EmptyStateView(
            title: "common.error", systemImage: "exclamationmark.triangle",
            message: error.errorDescription.map { LocalizedStringKey($0) },
            actionTitle: "common.retry", action: retry
        )
    }
}

/// Kan grubu rozeti; sistem renkleriyle, Dynamic Type uyumlu.
struct BloodTypeBadge: View {
    let bloodType: BloodType

    var body: some View {
        Text(bloodType.displayName)
            .font(.headline.weight(.bold))
            .foregroundStyle(.white)
            .padding(.horizontal, 10)
            .padding(.vertical, 6)
            .background(Theme.brand, in: RoundedRectangle(cornerRadius: 8, style: .continuous))
            .accessibilityLabel(Text("accessibility.bloodType \(bloodType.displayName)"))
    }
}

/// Çevrimdışı olunduğunda üstte gösterilen bilgi çubuğu. `safeAreaInset` yerine katman (overlay) kullanır:
/// bu sayede iç ekranların güvenli alanı ve kaydırma konumu hiçbir zaman etkilenmez.
struct OfflineBanner: ViewModifier {
    @EnvironmentObject private var network: NetworkMonitor

    func body(content: Content) -> some View {
        content.overlay(alignment: .top) {
            if !network.isConnected {
                Label("error.network", systemImage: "wifi.slash")
                    .font(.footnote.weight(.semibold)).foregroundStyle(.white)
                    .frame(maxWidth: .infinity).padding(.vertical, Theme.Spacing.s)
                    .background(Color.gray.opacity(0.95))
                    .transition(.move(edge: .top).combined(with: .opacity))
                    .accessibilityAddTraits(.isStaticText)
            }
        }
        .animation(.easeInOut, value: network.isConnected)
    }
}

extension View {
    func offlineBanner() -> some View { modifier(OfflineBanner()) }
}

extension Date {
    private static let relativeFormatter: RelativeDateTimeFormatter = {
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .short // "18 dk. önce" / "18 min. ago" (X/Instagram gibi kısa)
        return formatter
    }()

    /// "2 saat önce" gibi göreli metin.
    var relativeDescription: String {
        let formatter = Self.relativeFormatter
        formatter.locale = LocalizationOverride.locale
        return formatter.localizedString(for: self, relativeTo: .now)
    }
}
