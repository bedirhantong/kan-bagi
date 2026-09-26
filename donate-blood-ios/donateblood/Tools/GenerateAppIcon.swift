#!/usr/bin/env swift
// Uygulama ikonunu üretir (1024x1024: açık, koyu ve tinted varyantlar).
// Kullanım: swift Tools/GenerateAppIcon.swift donateblood/Assets.xcassets/AppIcon.appiconset
import Foundation
import CoreGraphics
import ImageIO
import UniformTypeIdentifiers

// Android splash/logo damlasının SVG yolu (yalnızca mutlak M/C/Z).
let pathData = "M167.3,161.65C168.02,173.12 165.68,185.09 158.88,194.35C146.95,210.61 123.62,215.04 104.08,210.01C82.33,204.42 59.53,189.35 46.42,171.21C40.86,163.52 36.51,154.32 37.36,144.86C38.32,134.23 46.48,124.62 56.81,121.95C67.15,119.28 78.94,123.74 84.93,132.58C89.63,121.6 104.76,117.42 115.36,122.92C125.96,128.42 131.37,141.47 129.72,153.3C128.08,165.13 120.33,175.52 110.58,182.42C109.88,182.92 109.14,183.41 108.3,183.62C105.07,184.42 102.35,180.43 102.93,177.15C103.52,173.88 106.07,171.38 108.35,168.95C112.95,164.07 117.02,158.37 118.36,151.79C119.7,145.22 117.78,137.65 112.33,133.73C98.97,124.12 95.84,143.26 85.59,144.59C77.98,145.58 74.23,135.25 67.36,131.83C58.19,127.26 47.35,137.93 47.86,148.16C48.38,158.39 55.93,166.76 63.45,173.71C77.24,186.46 93.37,198.09 112.01,200.4C130.64,202.7 151.87,192.74 156.71,174.59C159.83,162.87 155.82,150.53 151.43,139.21C134.65,95.95 111.76,55.06 83.64,18.14C58.86,50.82 38.37,86.75 22.86,124.72C16.27,140.86 10.49,157.99 11.9,175.37C13.97,200.81 32.47,223.71 56.34,232.75C80.2,241.79 108.27,237.4 129.33,222.99C131,221.85 133.06,220.69 135.04,220.16C137.17,219.59 139.21,219.75 140.57,221.44C143.17,224.67 139.69,229.23 136.32,231.66C110.82,250.07 75.16,254.17 46.63,240.45C21.76,228.49 3.74,203.18 0.71,175.75C-2.71,144.81 13.45,112.35 27.68,85.87C43.29,56.81 61.07,28.92 80.82,2.51C81.61,1.46 82.59,0.3 83.91,0.26C85.41,0.21 86.51,1.59 87.34,2.83C114.33,42.86 141.55,83.33 158.87,128.39C163,139.11 166.57,150.19 167.3,161.65Z"
let dropBounds = CGRect(x: 0, y: 0, width: 168, height: 254)

func parse(_ data: String) -> [(Character, [CGPoint])] {
    var result: [(Character, [CGPoint])] = []
    var kind: Character = "M"
    var numbers: [Double] = []
    var token = ""
    func push() { if let v = Double(token) { numbers.append(v) }; token = "" }
    func flush() {
        let stride = kind == "C" ? 6 : 2
        var i = 0
        while i + stride <= numbers.count {
            var pts: [CGPoint] = []
            var j = i
            while j < i + stride { pts.append(CGPoint(x: numbers[j], y: numbers[j + 1])); j += 2 }
            result.append((kind, pts)); i += stride
        }
        numbers.removeAll()
    }
    for ch in data {
        if ch.isLetter { push(); flush(); kind = ch; if ch == "Z" { result.append(("Z", [])) } }
        else if ch == "," || ch == " " { push() }
        else if ch == "-" && !token.isEmpty && !token.hasSuffix("e") { push(); token = "-" }
        else { token.append(ch) }
    }
    push(); flush()
    return result
}

func dropPath(in rect: CGRect) -> CGPath {
    let scale = min(rect.width / dropBounds.width, rect.height / dropBounds.height)
    let ox = rect.minX + (rect.width - dropBounds.width * scale) / 2
    let oy = rect.minY + (rect.height - dropBounds.height * scale) / 2
    func map(_ p: CGPoint) -> CGPoint { CGPoint(x: ox + p.x * scale, y: oy + p.y * scale) }
    let path = CGMutablePath()
    for (kind, pts) in parse(pathData) {
        switch kind {
        case "M": path.move(to: map(pts[0]))
        case "C": path.addCurve(to: map(pts[2]), control1: map(pts[0]), control2: map(pts[1]))
        case "Z": path.closeSubpath()
        default: break
        }
    }
    return path
}

func rgb(_ hex: UInt32) -> CGColor {
    CGColor(red: CGFloat((hex >> 16) & 0xFF) / 255, green: CGFloat((hex >> 8) & 0xFF) / 255, blue: CGFloat(hex & 0xFF) / 255, alpha: 1)
}

enum Variant { case light, dark, tinted }

// Marka rengi (Theme.brand / Brand.colorset ile aynı aile). Derin kan kırmızısı.
let brandTop: UInt32 = 0xE63950
let brandBottom: UInt32 = 0xB5123A
let brandLight: UInt32 = 0xFF5A73 // koyu ikonda çizim rengi

/// iOS 18 ikon varyantları:
/// - light: marka gradyanı + beyaz çizim (saydamlık yok, App Store gereği)
/// - dark: saydam zemin + açık marka rengi çizim (sistem koyu zemini kendisi ekler)
/// - tinted: siyah zemin + beyaz çizim (sistem gri tonlayıp renklendirir)
func render(_ variant: Variant, size: Int = 1024) -> CGImage {
    let space = CGColorSpaceCreateDeviceRGB()
    let alpha: CGImageAlphaInfo = variant == .dark ? .premultipliedLast : .noneSkipLast
    let ctx = CGContext(data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: 0, space: space,
                        bitmapInfo: alpha.rawValue)!
    let s = CGFloat(size)
    switch variant {
    case .light:
        let gradient = CGGradient(colorsSpace: space, colors: [rgb(brandTop), rgb(brandBottom)] as CFArray, locations: [0, 1])!
        ctx.drawLinearGradient(gradient, start: CGPoint(x: 0, y: s), end: CGPoint(x: 0, y: 0), options: [])
    case .dark:
        ctx.clear(CGRect(x: 0, y: 0, width: s, height: s))
    case .tinted:
        ctx.setFillColor(rgb(0x000000)); ctx.fill(CGRect(x: 0, y: 0, width: s, height: s))
    }

    // Çizim: orijinal logo, biraz büyük ve kalın (küçük boyutlarda okunaklı).
    let h = s * 0.66, w = h * dropBounds.width / dropBounds.height
    let rect = CGRect(x: (s - w) / 2, y: (s - h) / 2 - s * 0.01, width: w, height: h)
    ctx.translateBy(x: 0, y: s); ctx.scaleBy(x: 1, y: -1)
    let path = dropPath(in: rect)
    let color = variant == .dark ? rgb(brandLight) : rgb(0xFFFFFF)
    ctx.addPath(path); ctx.setFillColor(color); ctx.fillPath()
    ctx.addPath(path); ctx.setStrokeColor(color); ctx.setLineWidth(s * 0.013)
    ctx.setLineJoin(.round); ctx.setLineCap(.round); ctx.strokePath()
    return ctx.makeImage()!
}

func write(_ image: CGImage, to url: URL) {
    let dest = CGImageDestinationCreateWithURL(url as CFURL, UTType.png.identifier as CFString, 1, nil)!
    CGImageDestinationAddImage(dest, image, nil)
    guard CGImageDestinationFinalize(dest) else { fatalError("PNG yazılamadı: \(url.path)") }
}

let outDir = URL(fileURLWithPath: CommandLine.arguments.count > 1 ? CommandLine.arguments[1] : ".")
for (name, variant) in [("AppIcon-Light.png", Variant.light), ("AppIcon-Dark.png", .dark), ("AppIcon-Tinted.png", .tinted)] {
    write(render(variant), to: outDir.appendingPathComponent(name))
    print("yazıldı:", name)
}
