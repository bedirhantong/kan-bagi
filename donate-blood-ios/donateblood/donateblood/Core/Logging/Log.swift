import Foundation
import os

/// `os.Logger` üzerinden kategori bazlı loglama.
enum Log {
    private static let subsystem = Bundle.main.bundleIdentifier ?? "donateblood"

    static let auth = Logger(subsystem: subsystem, category: "auth")
    static let data = Logger(subsystem: subsystem, category: "data")
    static let realtime = Logger(subsystem: subsystem, category: "realtime")
    static let push = Logger(subsystem: subsystem, category: "push")
}
