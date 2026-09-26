# BloodApp (iOS)

Kan bağışı ihtiyaçlarını duyuran, bağışçıları bilgilendiren ve teşvik eden native iOS uygulaması.
Android uygulamasının (`donate-blood-android`) SwiftUI karşılığıdır; backend olarak **Supabase** kullanır.

- Minimum iOS **16.0**, saf SwiftUI, Swift 5 dil modu (strict concurrency: complete)
- Giriş: Apple, Google, **misafir** (anonim). Misafir hesap sonradan Apple/Google'a bağlanınca verisi korunur.
- Diller: Türkçe, İngilizce (String Catalog)

## Mimari

Clean Architecture; bağımlılıklar yalnızca içe doğru akar.

```
Presentation (SwiftUI + MVVM)  ──►  Domain  ◄──  Data (Supabase)
                                      ▲
                               App (kompozisyon kökü)
```

| Katman | İçerik | Kural |
|---|---|---|
| `Domain/` | Entity'ler, use case'ler (`struct`, tek sorumluluk), repository **protokolleri** | Framework import etmez (Supabase/SwiftUI yok) |
| `Data/` | Supabase repository implementasyonları, DTO'lar ve eşleyiciler, platform servisleri (Apple/Google kimlik) | Domain protokollerini uygular; hataları `AppError`'a çevirir |
| `Presentation/` | View'lar, ViewModel'ler (`@MainActor ObservableObject`) | Yalnızca use case'lere bağımlıdır, `AppContainer`'ı bilmez |
| `App/` | `AppContainer` (DI), `AppContainer+ViewModels` (fabrikalar), AppDelegate, push (`App/Push`: OneSignal yalnızca `OneSignalPushService`'te) | Tek yer: somut sınıflar burada bağlanır |
| `Core/` | `AppError`, `Log`, `AppConfig`, `NetworkMonitor`, `ImageLoader` | Katmanlardan bağımsız yardımcılar |

SOLID uygulaması:
- **S** – her use case tek bir iş yapar; ViewModel'ler tek ekranı yönetir.
- **O** – yeni özellik = yeni use case/ViewModel; mevcut kod değişmez (ör. yeni `Route` durumu).
- **L/I** – repository protokolleri küçük ve alana özeldir (`ChatRepository`, `StoryRepository`...).
- **D** – ViewModel'ler ve `SessionStore` soyutlamalara (use case + `PushCoordinating`) bağlıdır; testlerde sahte repository verilir.

Navigasyon: her sekme kendi `NavigationStack`'ini kullanır; hedefler tek bir `Route` enum'ında toplanır (`routeDestinations`).
Misafir kısıtı: `SessionStore.requireAccount` misafirlerde giriş sayfasını açar; sunucuda da RLS ile zorunludur.

## Lisans ve atıf

[Apache License 2.0](../../LICENSE). Projeyi kullanabilir, değiştirebilir, çatallayabilir ve ticari olarak yayınlayabilirsiniz.
Tek koşul (Apache-2.0 §4): kopyanızda [LICENSE](../../LICENSE) ve [NOTICE](../../NOTICE) dosyalarını koruyun, yani orijinal projeye atıf yapın
(ör. README'nizde veya uygulamanızın "Hakkında" ekranında: *"BloodApp tabanlıdır — Bedirhan Tong"*).
Örnek veri ve dış görseller lisansa dahil değildir, ayrıntı için [NOTICE](../../NOTICE).

## Katkı vermek

Topluluk katkılarına açıktır: [CONTRIBUTING.md](../../CONTRIBUTING.md) (5 dakikalık kurulum, test kullanıcısıyla giriş, mimari kuralları, yerelleştirme), [Davranış Kuralları](../../CODE_OF_CONDUCT.md), [Güvenlik](../../SECURITY.md).
Apple/Google hesabı **gerekmez**: Debug yapılandırmasında e-posta/şifre ile test girişi vardır.

## Kurulum

1. **Supabase**: [`supabase/README.md`](../../supabase/README.md) adımlarını uygulayın (SQL dosyaları bu repodadır).
2. `Config/Secrets.example.xcconfig` dosyasını `Config/Secrets.xcconfig` olarak kopyalayıp doldurun
   (`SUPABASE_URL`, `SUPABASE_ANON_KEY`; isteğe bağlı Google iOS client ID ve imzalama: `APP_BUNDLE_ID`, `APP_DEVELOPMENT_TEAM`). Bu dosya `.gitignore`'dadır.
   > **Asla** `service_role` anahtarını uygulamaya koymayın; yalnızca anon key kullanılır, güvenlik RLS ile sağlanır.
3. Xcode 16+ ile `donateblood.xcodeproj` dosyasını açın. Paketler (supabase-swift, GoogleSignIn) otomatik çözülür.
4. Apple girişi ve push için ücretli bir geliştirici ekibi gerekir (`Secrets.xcconfig` → `APP_DEVELOPMENT_TEAM`, `APP_ENTITLEMENTS`). Bunlar olmadan da simülatörde test hesabıyla çalışır.
5. Push bildirimleri (OneSignal) için [docs/PUSH_NOTIFICATIONS.md](../../docs/PUSH_NOTIFICATIONS.md).

Yapılandırma eksikse uygulama çökmek yerine "Supabase yapılandırması eksik" ekranı gösterir.

## Test

```bash
xcodebuild test -project donateblood.xcodeproj -scheme donateblood \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' CODE_SIGNING_ALLOWED=NO
```

Swift Testing kullanılır. `donatebloodTests/Support/` altında sahte repository'ler vardır; ağ ve Supabase gerekmez.
CI: kök dizindeki `.github/workflows/ci.yml` (iOS build + test, yerelleştirme denetimi, Edge Function testleri).

## Araçlar

- `Tools/GenerateAppIcon.swift` – logo yolundan ikonu (açık/koyu/tinted) üretir:
  `swift Tools/GenerateAppIcon.swift donateblood/Assets.xcassets/AppIcon.appiconset`

## Görseller

Giriş, tanıtım ve kayıt ekranları uzak görsel kullanmaz (SF Symbols + marka rengi). Hikâye gibi uzak görselleri
`RemoteImage`/`ImageLoader` ekran boyutuna küçülterek açar (8000 px'lik bir PNG'yi 250 MB yerine ~200 KB tutar).

## Yayın öncesi kontrol listesi

- [ ] `Secrets.xcconfig` üretim değerleriyle dolu; Release yapılandırması test edildi
- [ ] Apple/Google sağlayıcıları Supabase'de açık; Google iOS client ID girildi
- [ ] Push (OneSignal): OneSignal uygulaması, APNs anahtarı, `send-push` Edge Function deploy, Database Webhook (bkz. [docs/PUSH_NOTIFICATIONS.md](../../docs/PUSH_NOTIFICATIONS.md))
- [ ] Örnek hikâye görselleri kendi içeriğinizle değiştirildi; hastane listesi gerçek veriyle dolduruldu
- [ ] Gizlilik politikası ve kullanım koşulları URL'leri (`AppLinks`) doğrulandı
- [ ] App Store Connect: gizlilik "nutrition label" (bkz. `PrivacyInfo.xcprivacy`), ekran görüntüleri, hesap silme (Ayarlar → Hesabımı Sil)
- [ ] TestFlight ile gerçek cihaz testi
