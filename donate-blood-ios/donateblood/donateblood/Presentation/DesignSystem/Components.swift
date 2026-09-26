import SwiftUI

// MARK: - Avatar

/// Baş harfli renkli daire (sohbet, profil). Renk `seed`'den türetildiği için aynı kişi hep aynı renkte görünür.
struct AvatarView: View {
    let name: String
    let seed: UUID
    var size: CGFloat = Theme.Size.avatar

    private static let palette: [Color] = [
        Color(red: 0.95, green: 0.50, blue: 0.56), Color(white: 0.3), .orange, .teal, .indigo, .green,
    ]

    private var color: Color { Self.palette[Self.paletteIndex(for: seed, count: Self.palette.count)] }

    /// Aynı kişi (UUID) her zaman aynı renge düşer. `hashValue` süreçler arası değiştiği için UUID baytlarından türetilir.
    static func paletteIndex(for seed: UUID, count: Int) -> Int {
        precondition(count > 0)
        let sum = Int(seed.uuid.0) &+ Int(seed.uuid.7) &+ Int(seed.uuid.15)
        return sum % count
    }

    var body: some View {
        Text(String(name.first ?? "?").uppercased())
            .font(.system(size: size * 0.42, weight: .bold)).foregroundStyle(.white)
            .frame(width: size, height: size)
            .background(color, in: Circle())
            .accessibilityHidden(true)
    }
}

/// Kan grubunu gösteren marka renkli avatar (ilan satırlarında profil fotoğrafı yerine).
struct BloodTypeAvatar: View {
    let bloodType: BloodType
    var size: CGFloat = Theme.Size.avatar

    var body: some View {
        Text(bloodType.displayName)
            .font(.system(size: size * 0.34, weight: .heavy)).foregroundStyle(.white)
            .minimumScaleFactor(0.7)
            .frame(width: size, height: size)
            .background(Theme.brand, in: Circle())
            .accessibilityLabel(Text("accessibility.bloodType \(bloodType.displayName)"))
    }
}

/// Akıştaki büyük, kolay taranan kan grubu karosu (yuvarlak köşeli kare). Acil ilanda dolu marka rengi, diğerlerinde açık ton.
struct BloodTypeTile: View {
    let bloodType: BloodType
    var isEmergency = false
    var size: CGFloat = 60

    var body: some View {
        Text(bloodType.displayName)
            .font(.system(size: size * 0.36, weight: .heavy, design: .rounded))
            .minimumScaleFactor(0.7)
            .foregroundStyle(isEmergency ? Color.white : Theme.brand)
            .frame(width: size, height: size)
            .background(isEmergency ? Theme.brand : Theme.brand.opacity(0.12),
                        in: RoundedRectangle(cornerRadius: size * 0.28, style: .continuous))
            .accessibilityHidden(true) // özet, kartın erişilebilirlik etiketinde
    }
}

/// Renk + ikon + yazı ile durum etiketi (yalnızca renge güvenmez; WCAG 1.4.1).
struct StatusChip: View {
    let text: LocalizedStringKey
    let systemImage: String
    var color: Color
    /// Dolu (vurgulu) ya da açık zeminli.
    var filled = false

    var body: some View {
        Label(text, systemImage: systemImage)
            .font(.caption.weight(.semibold))
            .foregroundStyle(filled ? Color.white : color)
            .padding(.horizontal, Theme.Spacing.s + 2).padding(.vertical, Theme.Spacing.xs)
            .background(filled ? color : color.opacity(0.14), in: Capsule())
    }
}

// MARK: - Butonlar

/// Birincil hap düğme (dolu marka rengi). `fullWidth` ile satırı doldurur.
struct PrimaryPillButtonStyle: ButtonStyle {
    var fullWidth = true
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundStyle(.white)
            .frame(maxWidth: fullWidth ? .infinity : nil, minHeight: Theme.Size.minTap)
            .padding(.horizontal, Theme.Spacing.l)
            .background(Theme.brand.opacity(configuration.isPressed ? 0.8 : 1), in: Capsule())
            .opacity(isEnabled ? 1 : 0.45)
    }
}

/// İkincil hap düğme (çerçeveli, Instagram'daki "Profili Düzenle" gibi).
struct SecondaryPillButtonStyle: ButtonStyle {
    var fullWidth = true

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(.primary)
            .frame(maxWidth: fullWidth ? .infinity : nil, minHeight: 36)
            .padding(.horizontal, Theme.Spacing.m)
            .background(Theme.Palette.surface.opacity(configuration.isPressed ? 0.6 : 1), in: RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous))
    }
}

/// Çerçeveli hap düğme (Google ile giriş gibi üçüncü taraf yöntemler; marka kuralı: beyaz zemin + gri çerçeve).
struct OutlinePillButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundStyle(.primary)
            .frame(maxWidth: .infinity, minHeight: 50)
            .background(Theme.Palette.background.opacity(configuration.isPressed ? 0.7 : 1), in: Capsule())
            .overlay(Capsule().stroke(Color(.systemGray3), lineWidth: 1))
    }
}

/// Alt çubuğun ortasındaki "İlan ver" simgesi: marka renginde yuvarlatılmış kare + beyaz artı (Instagram'daki gibi).
/// Sekme simgeleri şablon olarak boyandığı için SwiftUI görünümü orijinal renkli bir resme çevrilir.
enum CreateTabIcon {
    static let size: CGFloat = 30

    @MainActor
    static func image(scale: CGFloat, colorScheme: ColorScheme) -> Image {
        let content = RoundedRectangle(cornerRadius: 9, style: .continuous)
            .fill(Theme.brand)
            .frame(width: size, height: size)
            .overlay(Image(systemName: "plus").font(.system(size: 16, weight: .bold)).foregroundStyle(.white))
            .environment(\.colorScheme, colorScheme)
        let renderer = ImageRenderer(content: content)
        renderer.scale = scale
        guard let uiImage = renderer.uiImage else { return Image(systemName: "plus.app.fill") }
        return Image(uiImage: uiImage.withRenderingMode(.alwaysOriginal))
    }
}

// MARK: - Rozetler

struct PillBadge: View {
    let text: LocalizedStringKey
    var color: Color = Theme.brand

    var body: some View {
        Text(text)
            .font(.caption2.weight(.bold)).foregroundStyle(.white)
            .padding(.horizontal, Theme.Spacing.s).padding(.vertical, 3)
            .background(color, in: Capsule())
    }
}

// MARK: - İkonlu sekme şeridi (Instagram profil sekmeleri)

/// Altı çizgili, ikonlu sekme şeridi. `Picker` yerine Instagram profilindeki gibi görünür.
struct IconTabStrip<Tab: Hashable>: View {
    struct Item {
        let tab: Tab
        let systemImage: String
        let label: LocalizedStringKey
    }

    let items: [Item]
    @Binding var selection: Tab
    @Namespace private var underline

    var body: some View {
        HStack(spacing: 0) {
            ForEach(items.indices, id: \.self) { index in
                let item = items[index]
                Button {
                    withAnimation(.easeInOut(duration: 0.2)) { selection = item.tab }
                } label: {
                    VStack(spacing: Theme.Spacing.s) {
                        Image(systemName: item.systemImage)
                            .font(.title3)
                            .foregroundStyle(selection == item.tab ? Color.primary : Color.secondary)
                        ZStack {
                            Rectangle().fill(Color.clear).frame(height: 2)
                            if selection == item.tab {
                                Rectangle().fill(Color.primary).frame(height: 2).matchedGeometryEffect(id: "underline", in: underline)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, minHeight: Theme.Size.minTap)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(item.label))
                .accessibilityAddTraits(selection == item.tab ? .isSelected : [])
            }
        }
    }
}

// MARK: - Dokunsal geri bildirim

enum Haptics {
    static func selection() { UISelectionFeedbackGenerator().selectionChanged() }
    static func success() { UINotificationFeedbackGenerator().notificationOccurred(.success) }
    static func impact() { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
    static func warning() { UINotificationFeedbackGenerator().notificationOccurred(.warning) }
}

// MARK: - Bilgi bölümleri (detay ekranları)

/// Başlıklı bilgi bölümü: küçük ikonlu başlık + satırlar. Detay ekranlarında ortak kullanılır.
struct InfoSection<Content: View>: View {
    let title: LocalizedStringKey
    let systemImage: String
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Label(title, systemImage: systemImage)
                .font(.footnote.weight(.semibold)).foregroundStyle(.secondary)
                .textCase(.uppercase)
                .padding(.horizontal, Theme.Spacing.l).padding(.top, Theme.Spacing.l).padding(.bottom, Theme.Spacing.s)
            content()
        }
    }
}

/// "Etiket ........ değer" satırı (detay ekranı).
struct InfoRow: View {
    let label: LocalizedStringKey
    let value: String
    var valueColor: Color = .secondary
    var systemImage: String?

    var body: some View {
        HStack(spacing: Theme.Spacing.m) {
            if let systemImage {
                Image(systemName: systemImage).frame(width: 24).foregroundStyle(Theme.brand).accessibilityHidden(true)
            }
            Text(label).font(.body)
            Spacer(minLength: Theme.Spacing.m)
            Text(value).font(.body).foregroundStyle(valueColor).multilineTextAlignment(.trailing)
        }
        .padding(.horizontal, Theme.Spacing.l).padding(.vertical, Theme.Spacing.m)
        .accessibilityElement(children: .combine)
    }
}
