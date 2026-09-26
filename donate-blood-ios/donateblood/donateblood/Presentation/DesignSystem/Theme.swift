import SwiftUI

/// Uygulamanın tek tasarım sözleşmesi (design tokens). Renk, aralık ve boyutlar yalnızca buradan gelir;
/// ekranlarda sabit sayı/renk yerine bu değerler kullanılır. Görünüm Instagram/X (Twitter) düzenine yakındır:
/// kenardan kenara satırlar, ince ayırıcılar, yuvarlak avatarlar, hap biçimli düğmeler ve sade gezinme çubukları.
enum Theme {
    /// Marka rengi: derin kan kırmızısı (açık #D0213F, koyu #E5405A; `Brand` renk varlığı).
    /// Uygulama ikonuyla aynı ailedir; iOS'un yıkıcı eylem kırmızısından (systemRed) bilerek farklıdır.
    /// Beyaz metin kontrastı 5,3:1 (WCAG AA). İkon: Tools/GenerateAppIcon.swift.
    static let brand = Color("Brand")
    static let brandGradient = LinearGradient(
        colors: [Color(red: 0.45, green: 0.04, blue: 0.13), brand, Color(red: 0.90, green: 0.22, blue: 0.31)],
        startPoint: .top, endPoint: .bottom)

    /// Aralık ölçeği (pt). Her boşluk bu kademelerden biri olmalı.
    enum Spacing {
        static let xs: CGFloat = 4
        static let s: CGFloat = 8
        static let m: CGFloat = 12
        static let l: CGFloat = 16
        static let xl: CGFloat = 24
    }

    enum Radius {
        static let feedCard: CGFloat = 18
        static let card: CGFloat = 16
        static let field: CGFloat = 12
    }

    enum Size {
        /// Akıştaki ve listelerdeki avatar.
        static let avatar: CGFloat = 44
        static let profileAvatar: CGFloat = 80
        /// Apple HIG asgari dokunma alanı.
        static let minTap: CGFloat = 44
    }

    /// Anlamsal renkler: koyu/açık temada sistem renklerinden türetilir.
    enum Palette {
        static let background = Color(.systemBackground)
        static let groupedBackground = Color(.systemGroupedBackground)
        static let surface = Color(.secondarySystemBackground)
        /// Akış zemini ve üzerindeki kart yüzeyi (ortak bölge ilkesi: her ilan kendi sınırı içinde).
        static let feedBackground = Color(.systemGroupedBackground)
        static let card = Color(.secondarySystemGroupedBackground)
        static let compatible = Color.green
        static let separator = Color(.separator)
        static let verified = Color.blue // X'teki mavi tik gibi
        static let warning = Color.orange
    }
}

/// Satırlar arasında kenardan kenara ince ayırıcı (Instagram/X akışı).
struct HairlineDivider: View {
    var body: some View {
        Rectangle().fill(Theme.Palette.separator.opacity(0.6)).frame(height: 1 / UIScreen.main.scale)
            .accessibilityHidden(true)
    }
}
