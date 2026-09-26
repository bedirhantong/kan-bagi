import Foundation
import ImageIO
import UIKit

/// Uzak görselleri indirir ve **ekran boyutuna küçülterek** çözer (ImageIO thumbnail).
/// `AsyncImage`'in aksine 8000x8000'lik bir PNG'yi 250 MB yerine birkaç yüz KB'lık bitmap olarak tutar.
/// Sonuçlar bellekte `NSCache` ile (maliyet sınırlı), ağ yanıtları diskte `URLCache` ile saklanır.
final class ImageLoader: @unchecked Sendable {
    static let shared = ImageLoader()

    private let cache = NSCache<NSString, UIImage>()
    private let session: URLSession
    private let lock = NSLock()
    private var inflight: [String: Task<UIImage?, Never>] = [:]

    init() {
        cache.totalCostLimit = 48 * 1024 * 1024 // 48 MB çözülmüş bitmap
        let configuration = URLSessionConfiguration.default
        configuration.urlCache = URLCache(memoryCapacity: 8 * 1024 * 1024, diskCapacity: 128 * 1024 * 1024)
        configuration.requestCachePolicy = .returnCacheDataElseLoad
        configuration.timeoutIntervalForRequest = 20
        session = URLSession(configuration: configuration)
    }

    /// `maxPixelSize`: görselin ekranda kaplayacağı en büyük kenar (piksel).
    func image(for url: URL, maxPixelSize: CGFloat) async -> UIImage? {
        let bucket = Int((maxPixelSize / 64).rounded(.up)) * 64 // benzer boyutlar aynı önbelleği paylaşsın
        let key = "\(url.absoluteString)|\(bucket)"
        if let cached = cache.object(forKey: key as NSString) { return cached }

        let task: Task<UIImage?, Never> = lock.withLock {
            if let existing = inflight[key] { return existing }
            let created = Task<UIImage?, Never> { [session] in
                guard let (data, response) = try? await session.data(from: url),
                      (response as? HTTPURLResponse).map({ (200..<300).contains($0.statusCode) }) ?? true
                else { return nil }
                return Self.downsample(data: data, maxPixelSize: CGFloat(bucket))
            }
            inflight[key] = created
            return created
        }

        let image = await task.value
        lock.withLock { inflight[key] = nil }
        if let image { cache.setObject(image, forKey: key as NSString, cost: Self.cost(of: image)) }
        return image
    }

    /// Çözülmüş bitmap'i `maxPixelSize`'a sığacak şekilde küçültür. Kaynak tam boyutta belleğe alınmaz.
    static func downsample(data: Data, maxPixelSize: CGFloat) -> UIImage? {
        let sourceOptions = [kCGImageSourceShouldCache: false] as CFDictionary
        guard let source = CGImageSourceCreateWithData(data as CFData, sourceOptions) else { return nil }
        let options = [
            kCGImageSourceCreateThumbnailFromImageAlways: true,
            kCGImageSourceCreateThumbnailWithTransform: true,
            kCGImageSourceShouldCacheImmediately: true,
            kCGImageSourceThumbnailMaxPixelSize: max(maxPixelSize, 1),
        ] as CFDictionary
        guard let cgImage = CGImageSourceCreateThumbnailAtIndex(source, 0, options) else { return nil }
        return UIImage(cgImage: cgImage)
    }

    private static func cost(of image: UIImage) -> Int {
        guard let cgImage = image.cgImage else { return 0 }
        return cgImage.bytesPerRow * cgImage.height
    }
}

/// CDN'lerin sunucu tarafı yeniden boyutlandırmasını kullanır: dosya hiç büyük inmez (ör. 13 MB -> ~100 KB).
/// Görseller Supabase Storage'a taşındığında burası genişletilebilir.
enum ImageVariants {
    /// Contentful Images API: `w` genişlik (px), `q` kalite.
    static func optimized(_ url: URL, width: CGFloat) -> URL {
        guard url.host?.hasSuffix("ctfassets.net") == true,
              var components = URLComponents(url: url, resolvingAgainstBaseURL: false) else { return url }
        var items = components.queryItems ?? []
        items.removeAll { ["w", "h", "q", "fit"].contains($0.name) }
        items.append(URLQueryItem(name: "w", value: String(Int(width.rounded(.up)))))
        items.append(URLQueryItem(name: "q", value: "80"))
        components.queryItems = items
        return components.url ?? url
    }
}
