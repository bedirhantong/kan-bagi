import SwiftUI

/// `AsyncImage` yerine: görseli gösterileceği boyuta küçülterek yükler ve önbellekler.
/// - Parameter pointSize: görselin ekranda kaplayacağı en büyük kenar (pt).
struct RemoteImage<Placeholder: View>: View {
    let url: URL?
    let pointSize: CGFloat
    var contentMode: ContentMode = .fill
    @ViewBuilder var placeholder: () -> Placeholder

    @Environment(\.displayScale) private var displayScale
    @State private var image: UIImage?

    var body: some View {
        ZStack {
            if let image {
                Image(uiImage: image).resizable().aspectRatio(contentMode: contentMode)
            } else {
                placeholder()
            }
        }
        .task(id: url) {
            guard let url else { image = nil; return }
            let pixels = pointSize * displayScale
            let source = ImageVariants.optimized(url, width: pixels)
            image = await ImageLoader.shared.image(for: source, maxPixelSize: pixels)
        }
    }
}
