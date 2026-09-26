import SwiftUI

/// Karşılama (Android'deki üç sayfa ve metinler). Görseller yerel SF Symbols: ağ, telif ve yükleme sorunu yok.
struct OnboardingView: View {
    let onFinish: () -> Void
    @State private var page = 0

    private struct Page: Identifiable {
        let id: Int
        let systemImage: String
        let title: LocalizedStringKey
        let text: LocalizedStringKey
    }

    private let pages = [
        Page(id: 0, systemImage: "drop.fill", title: "onboarding.1.title", text: "onboarding.1.text"),
        Page(id: 1, systemImage: "mappin.and.ellipse", title: "onboarding.2.title", text: "onboarding.2.text"),
        Page(id: 2, systemImage: "bell.badge.fill", title: "onboarding.3.title", text: "onboarding.3.text"),
    ]

    private var isLastPage: Bool { page == pages.count - 1 }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                if !isLastPage {
                    Button("onboarding.skip", action: onFinish)
                        .font(.subheadline.weight(.semibold)).foregroundStyle(.secondary)
                        .frame(minHeight: Theme.Size.minTap)
                }
            }
            .padding(.horizontal, Theme.Spacing.xl)
            .frame(height: Theme.Size.minTap)

            TabView(selection: $page) {
                ForEach(pages) { item in
                    VStack(spacing: Theme.Spacing.xl) {
                        Spacer(minLength: 0)
                        Image(systemName: item.systemImage)
                            .font(.system(size: 72, weight: .semibold))
                            .foregroundStyle(Theme.brand)
                            .frame(width: 180, height: 180)
                            .background(Theme.brand.opacity(0.12), in: Circle())
                            .accessibilityHidden(true)
                        VStack(spacing: Theme.Spacing.m) {
                            Text(item.title).font(.largeTitle.weight(.bold)).multilineTextAlignment(.center)
                            Text(item.text).font(.body).foregroundStyle(.secondary).multilineTextAlignment(.center)
                        }
                        .accessibilityElement(children: .combine)
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal, Theme.Spacing.xl)
                    .tag(item.id)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            HStack(spacing: Theme.Spacing.s) {
                ForEach(pages) { item in
                    Capsule().fill(item.id == page ? Theme.brand : Theme.Palette.separator)
                        .frame(width: item.id == page ? 22 : 8, height: 8)
                }
            }
            .animation(.easeInOut(duration: 0.2), value: page)
            .accessibilityElement()
            .accessibilityLabel(Text("onboarding.page \(page + 1) \(pages.count)"))
            .padding(.bottom, Theme.Spacing.xl)

            Button(isLastPage ? "onboarding.start" : "onboarding.next") {
                if isLastPage { onFinish() } else { withAnimation { page += 1 } }
            }
            .buttonStyle(PrimaryPillButtonStyle())
            .padding(.horizontal, Theme.Spacing.xl)
            .padding(.bottom, Theme.Spacing.l)
        }
        .frame(maxWidth: 560)
        .frame(maxWidth: .infinity)
        .background(Theme.Palette.background.ignoresSafeArea())
    }
}
