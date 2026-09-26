import SwiftUI

/// Android'deki açılış animasyonu: logo çizgisi çizilir, ardından dolgu belirir.
struct SplashView: View {
    @State private var progress: CGFloat = 0
    @State private var filled = false

    private let brandRed = Theme.brand

    var body: some View {
        ZStack {
            Color(.systemBackground).ignoresSafeArea()
            ZStack {
                BloodDropShape().fill(brandRed).opacity(filled ? 1 : 0)
                BloodDropShape().trim(from: 0, to: progress).stroke(brandRed, style: StrokeStyle(lineWidth: 2, lineCap: .round, lineJoin: .round))
            }
            .frame(width: 130, height: 196)
            .accessibilityLabel(Text(verbatim: "Kan Bağı"))
        }
        .onAppear {
            withAnimation(.easeInOut(duration: 1.3)) { progress = 1 }
            withAnimation(.easeIn(duration: 0.4).delay(1.1)) { filled = true }
        }
    }
}
