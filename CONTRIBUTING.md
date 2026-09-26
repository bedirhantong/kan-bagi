# Katkı Rehberi

Kan Bağı bir topluluk projesidir: kan bağışı ihtiyaçlarını duyurmak, bağışçıları bilgilendirmek ve teşvik etmek için.
Hata düzeltmeleri, yeni özellikler, çeviriler ve dokümantasyon katkıları çok değerlidir. **İlk kez katkı veriyorsanız `good first issue` etiketli konulara bakın.**

## Hızlı başlangıç (5 dakika, Apple/Google hesabı gerekmez)

1. Repoyu fork'layıp klonlayın. Xcode 16 veya üzeri gerekir (iOS 16+ hedefler).
2. Ücretsiz bir [Supabase](https://supabase.com) projesi açın ve [`supabase/README.md`](supabase/README.md) adımlarını uygulayın.
   Özellikle şu SQL'leri sırayla çalıştırın: `schema.sql` → yamalar (`03`–`07`) → `08_test_users.sql` → `02_seed.sql`.
3. `donate-blood-ios/donateblood/Config/Secrets.example.xcconfig` dosyasını aynı klasörde `Secrets.xcconfig` olarak kopyalayıp `SUPABASE_URL` ve `SUPABASE_ANON_KEY` değerlerini yazın.
   (Bu dosya git'e girmez. **`service_role` anahtarını asla kullanmayın.**)
4. `donate-blood-ios/donateblood/donateblood.xcodeproj` dosyasını açıp simülatörde çalıştırın.
5. Giriş ekranındaki **"Geliştirici girişi"** bölümünde bir test hesabına dokunun (`requester@test.dev`, `donor@test.dev`, şifre `Test1234!`).
   **"Yeni kullanıcı (kayıt)"** (`newuser@test.dev`) profili boş gelir ve kayıt akışını açar; `08_test_users.sql` tekrar çalıştırılınca yine boşalır.
   Bu bölüm yalnızca Debug yapılandırmasında görünür.

Apple ile giriş, Google ile giriş ve push bildirimleri **opsiyoneldir**; katkı vermek için gerekmez.
Bunları denemek isterseniz [Supabase kurulumuna](supabase/README.md) ve [push belgesine](docs/PUSH_NOTIFICATIONS.md) bakın.

## Mimari (kısaca)

Clean Architecture; bağımlılık yönü **Presentation → Domain ← Data**. Ayrıntı için [iOS README](donate-blood-ios/donateblood/README.md#mimari).

| Katman | Kural |
|---|---|
| `Domain/` | Saf Swift. `Foundation` dışında framework import etmez (SwiftUI, Supabase, CoreLocation yok). Use case'ler tek iş yapan `struct`'lardır. |
| `Data/` | Domain'deki repository protokollerini uygular. Supabase/DTO/platform kodu yalnızca burada. |
| `Presentation/` | SwiftUI View + `@MainActor` ViewModel. ViewModel yalnızca ihtiyaç duyduğu use case'leri init'te alır (`AppContainer`'ı bilmez). |
| `App/` | Kompozisyon kökü. Somut sınıfları yalnızca burada bağlarız (`AppContainer`, `AppContainer+ViewModels`). |

### Yeni bir özellik eklemek

1. **Domain:** entity + repository protokolü + use case (`Domain/`).
2. **Data:** Supabase implementasyonu + DTO (`Data/`). Hataları `SupabaseErrorMapper` ile `AppError`'a çevirin.
3. **Presentation:** ViewModel (use case'leri init'te alsın) + View. Navigasyon için `Route` enum'ına durum ekleyin.
4. **App:** `AppContainer`'a use case'i, `AppContainer+ViewModels`'a fabrika metodunu ekleyin.
5. **Testler:** `donatebloodTests/Support/Fakes.swift` içindeki sahte repository'lerle ViewModel/use case testi yazın.
6. **Backend gerekiyorsa:** `supabase/` altına numaralı bir yama SQL'i ekleyin ve `schema.sql`'i de güncelleyin.
   Her tabloda RLS zorunludur; misafir (anonim) kullanıcılar yazamaz.

## Kod standartları

- Swift 5 dil modu, strict concurrency `complete`; uyarı bırakmayın.
- **Yeni Domain kodu framework import etmemeli.** Platform gereksinimi varsa Domain'de protokol, Data'da implementasyon yazın.
- Ağ isteklerini yalnızca repository'ler yapar; ViewModel'ler Supabase'i tanımaz.
- Yerel yardımcılar için önce mevcut olanlara bakın (`Core/`, `Presentation/Shared/`).
- Erişilebilirlik: etkileşimli elemanlara etiket verin, Dynamic Type'ı bozan sabit boyutlardan kaçının.
- Native SwiftUI bileşenlerini tercih edin; özel bileşen yalnızca gerçekten gerekliyse.
- iOS 16'yı destekliyoruz: iOS 17+ API'leri `if #available` arkasında kullanın.

## Tasarım standardı (Instagram / X'ten esinli)

Tüm ekranlar aynı tasarım sözleşmesini kullanır: `donate-blood-ios/donateblood/donateblood/Presentation/DesignSystem/`.

| Kural | Uygulama |
|---|---|
| Renk, aralık, boyut | Yalnızca `Theme` içinden (`Theme.brand`, `Theme.Spacing.*`, `Theme.Size.*`). Ekranlarda sabit renk/sayı yazmayın. |
| Liste/akış | Kenardan kenara satırlar + `HairlineDivider`; kartlı/gölgeli görünüm yok. İlan satırı: `RequestRow`. |
| Avatar | `AvatarView` (kişi), `BloodTypeAvatar` (ilan). |
| Düğmeler | `PrimaryPillButtonStyle` (tek birincil eylem), `SecondaryPillButtonStyle`. Yeni ilan: alt çubuğun ortasındaki sekme ve akış başındaki `RequestComposerCard` (X'teki paylaşma alanı; kısayollar formu `RequestPrefill` ile önceden doldurur). |
| Gezinme | Alt çubuk: Ana Sayfa, Harita, **İlan ver** (ortada; ekran değil, formu açar), Mesajlar, Profil. Bildirimler ana sayfanın üst çubuğunda (zil + okunmamış sayısı). **Alt sekme çubuğu yalnızca kök ekranlarda görünür**; itilen her ekran `routeDestinations` içinde çubuğu gizler. Yeni bir ekranı `Route` olarak ekleyin, sekme çubuğunu ayrıca gizlemeyin. |
| Gezinme çubuğu | Standart (sistem) ve `inline` başlıklı; araç çubuğu ikonları nötr (`.tint(.primary)`), vurgu rengi yalnızca seçili sekme ve birincil eylemlerde. |
| Harita | `PlacesMapView` (MKMapView: yerel kümeleme, işaret yeniden kullanımı). Veri görünür alana göre istenir (`MapViewport`); "Bu bölgede ara" yalnızca harita anlamlı oynadığında çıkar. Harita ekranı CoreLocation/MapKit'e değil use case'lere bağlıdır. |
| Bekleme durumları | `EmptyStateView`, `ErrorStateView`; çevrimdışı uyarısı `offlineBanner()` (katman: düzeni bozmaz). |
| Erişilebilirlik | Dokunma alanı ≥ 44 pt (`Theme.Size.minTap`), etiketler, Dynamic Type; ikonlu düğmelere `accessibilityLabel`. |
| Dokunsal geri bildirim | `Haptics.impact()/selection()/success()` (birincil eylemlerde). |

Simülatörde hızlı deneme (yalnızca Debug): `xcrun simctl launch <cihaz> <bundle-id> -debugSignIn donor -debugTab messages -debugNoPush YES`
(`-debugRoute notifications|settings|healthForm|faq|about|notificationSettings|languageSettings|request:<uuid>|chat:<uuid>`, `-debugTab map -debugMapSelect hospital|point`).
Kayıt adımlarını doğrudan açmak için: `-debugRegistration personal|donor|areas|notifications` (oturum açıksa kaydet düğmeleri o hesaba yazar).
Simülatör konumu: `xcrun simctl location <cihaz> set 36.8969,30.7133`. İmzasız (`CODE_SIGNING_ALLOWED=NO`) derlemede
Keychain yazılamadığı için oturum tutulmaz; ad-hoc imza için `CODE_SIGNING_ALLOWED=YES CODE_SIGN_IDENTITY=- DEVELOPMENT_TEAM= APP_ENTITLEMENTS=Minimal.entitlements`.

## Yerelleştirme (yeni dil eklemek)

Metinler `donate-blood-ios/donateblood/donateblood/Resources/Localizable.xcstrings` içindedir (String Catalog).

1. Xcode'da kataloğu açıp **+** ile yeni dili ekleyin ve tüm metinleri çevirin.
2. `donate-blood-ios/donateblood/Tools/check_localization.py` içindeki `REQUIRED_LANGUAGES` listesine dil kodunu ekleyin.
3. `python3 Tools/check_localization.py` (iOS klasöründen) çalıştırın: eksik/kullanılmayan anahtarları listeler (CI'da da çalışır).

Koddan metin üretirken (hata mesajı, erişilebilirlik etiketi vb.) `String(localized:)` değil **`L10n.string("anahtar")` / `L10n.format("anahtar %@", değer)`** kullanın: `String(localized:)` uygulama içi dil seçimini izlemez (denetim script'i bunu yakalar). SwiftUI `Text("anahtar")` doğrudan çalışır.

Kodda yeni bir metin kullanırsanız anahtarı (`ad.alan` biçiminde) kataloğa **hem `en` hem `tr`** ekleyin.

## Test ve CI

```bash
cd donate-blood-ios/donateblood
python3 Tools/check_localization.py
xcodebuild test -project donateblood.xcodeproj -scheme donateblood \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' CODE_SIGNING_ALLOWED=NO
```

Pull request açmadan önce ikisinin de geçtiğinden emin olun. Yeni davranış = yeni test.

## Pull request süreci

1. Konuyu (issue) açın veya var olana yorum yapın; büyük değişikliklerde önce tasarımı konuşalım.
2. Küçük, odaklı bir dal açın (`feat/...`, `fix/...`). Bir PR = bir amaç.
3. Commit mesajları: `feat:`, `fix:`, `refactor:`, `test:`, `docs:` öneklerini kullanın.
4. PR şablonundaki kontrol listesini doldurun; arayüz değişikliklerinde ekran görüntüsü ekleyin.
5. Bir bakımcı inceleyene kadar tartışmaya açık kalın; nazik ve yapıcı olun ([Davranış Kuralları](CODE_OF_CONDUCT.md)).

## Kendi kopyanızı yayınlamak isterseniz

Projeyi çatallayıp kendi topluluğunuz için yayınlayabilirsiniz ([Apache-2.0](LICENSE): `LICENSE` ve `NOTICE` dosyalarını koruyup orijinal projeye atıf yapın). Değiştirmeniz gerekenler:

- `donate-blood-ios/donateblood/Config/Secrets.xcconfig`: kendi Supabase projeniz, `APP_BUNDLE_ID`, `APP_DEVELOPMENT_TEAM`, (ücretli hesap için) `APP_ENTITLEMENTS = donateblood.entitlements`
- Supabase'de Apple/Google sağlayıcıları ve bildirimler ([iOS README](donate-blood-ios/donateblood/README.md#yayın-öncesi-kontrol-listesi) içindeki yayın kontrol listesi)
- Uygulama adı, ikon (`donate-blood-ios/donateblood/Tools/GenerateAppIcon.swift`), yasal bağlantılar (`Config/Secrets.xcconfig` → `SOURCE_CODE_URL`, `TERMS_URL`, `PRIVACY_URL`)
- **Üretim projesinde `08_test_users.sql`'i çalıştırmayın** ve `EMAIL_LOGIN_ENABLED` değerini `NO` bırakın.

## Lisans

Katkı vererek, katkınızın [Apache-2.0](LICENSE) altında lisanslanmasını kabul etmiş olursunuz (Apache-2.0 §5).

## Güvenlik

Bir güvenlik açığı bulursanız herkese açık issue açmayın; [SECURITY.md](SECURITY.md) adımlarını izleyin.
