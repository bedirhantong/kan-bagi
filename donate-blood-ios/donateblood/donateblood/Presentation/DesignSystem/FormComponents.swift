import SwiftUI

/// Etiketli form alanı: üstte etiket, ortada alan, altta ipucu ya da hata (hata varsa kırmızı çerçeve).
/// Hata metni erişilebilirlik için alanın değerine eklenir.
struct FormField<Content: View>: View {
    let label: LocalizedStringKey
    var isOptional = false
    var error: LocalizedStringKey?
    var hint: LocalizedStringKey?
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.xs + 2) {
            HStack(spacing: Theme.Spacing.xs) {
                Text(label).font(.subheadline.weight(.semibold))
                if isOptional { Text("form.optional").font(.subheadline).foregroundStyle(.secondary) }
            }
            content()
                .padding(.horizontal, Theme.Spacing.m).padding(.vertical, Theme.Spacing.m)
                .frame(minHeight: Theme.Size.minTap + 4)
                .background(Theme.Palette.surface, in: RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: Theme.Radius.field, style: .continuous)
                    .stroke(error == nil ? Color.clear : Color.red, lineWidth: 1.5))
            if let error {
                Label(error, systemImage: "exclamationmark.circle.fill")
                    .font(.footnote).foregroundStyle(.red)
                    .transition(.opacity)
            } else if let hint {
                Text(hint).font(.footnote).foregroundStyle(.secondary)
            }
        }
        .animation(.easeInOut(duration: 0.15), value: error == nil)
    }
}

/// Akış adımlarının başlığı (Instagram/X kayıt ekranları gibi: büyük, kalın soru + kısa açıklama).
struct StepHeader: View {
    let title: LocalizedStringKey
    let subtitle: LocalizedStringKey

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.s) {
            Text(title).font(.title.weight(.bold)).fixedSize(horizontal: false, vertical: true)
            Text(subtitle).font(.body).foregroundStyle(.secondary).fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isHeader)
    }
}

/// İkonlu tek satırlık fayda/özellik maddesi (giriş ve izin ekranları).
struct FeatureRow: View {
    let systemImage: String
    let text: LocalizedStringKey

    var body: some View {
        HStack(spacing: Theme.Spacing.m) {
            Image(systemName: systemImage).font(.body.weight(.semibold)).foregroundStyle(Theme.brand)
                .frame(width: 40, height: 40).background(Theme.brand.opacity(0.12), in: Circle())
                .accessibilityHidden(true)
            Text(text).font(.subheadline).fixedSize(horizontal: false, vertical: true)
            Spacer(minLength: 0)
        }
    }
}

extension ProfileValidationError {
    /// Alan altında gösterilecek mesaj.
    var message: LocalizedStringKey {
        switch self {
        case .required: return "validation.required"
        case .invalidName: return "validation.name"
        case .invalidPhone: return "validation.phone"
        case .underage: return "validation.underage"
        }
    }
}
