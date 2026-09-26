import Foundation
import CryptoKit
import Security

/// Apple/Google girişlerinde replay saldırılarına karşı kullanılan nonce çifti.
/// Sağlayıcıya `hashed`, Supabase'e `raw` gönderilir.
struct Nonce: Sendable {
    let raw: String
    var hashed: String { SHA256.hash(data: Data(raw.utf8)).map { String(format: "%02x", $0) }.joined() }

    static func random(length: Int = 32) -> Nonce {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        while result.count < length {
            var bytes = [UInt8](repeating: 0, count: 16)
            guard SecRandomCopyBytes(kSecRandomDefault, bytes.count, &bytes) == errSecSuccess else {
                fatalError("Güvenli rastgele sayı üretilemedi")
            }
            for byte in bytes where result.count < length && byte < charset.count * (256 / charset.count) {
                result.append(charset[Int(byte) % charset.count])
            }
        }
        return Nonce(raw: result)
    }
}
