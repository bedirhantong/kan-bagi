# Push bildirimleri (OneSignal)

Bildirimler **OneSignal** ile gönderilir. Push **opsiyoneldir**: `ONESIGNAL_APP_ID` boşsa uygulama sorunsuz çalışır,
sadece push devre dışı kalır (izin penceresi de gösterilmez). Katkı vermek için OneSignal hesabı gerekmez.

## Nasıl çalışır?

```
Yeni ilan / mesaj / bağış niyeti
   └─► Postgres tetikleyicisi `notifications` tablosuna satır ekler (uygulama içi bildirim kutusu)
          └─► Database Webhook ──► Edge Function `send-push` ──► OneSignal REST API ──► APNs ──► cihaz
```

- Kimlere bildirim gideceğine **veritabanı** karar verir (tetikleyici; kullanıcının kan grubu, bildirim bölgesi ve tercihlerine göre).
- İlan bildirimi yalnızca ilanın hastanesini, ilçesini ya da ilini seçmiş kullanıcılara gider; bölge seçmeyene gitmez (tüm ülkeye yayın yoktur). Toplam seçim en fazla 10'dur ([12_notification_areas_patch.sql](../supabase/12_notification_areas_patch.sql)).
  Ana sayfa ise tüm ilanları listeler; tercihler yalnızca bildirimi etkiler.
- **Cihaz jetonu saklanmaz.** Kullanıcı giriş yapınca uygulama `OneSignal.login(<Supabase kullanıcı id'si>)` çağırır
  (OneSignal buna *external id* der). Sunucu bildirimi bu id'ye gönderir; cihazlar ve jeton yenilemesi OneSignal'de yönetilir.
- Çıkış yapınca `OneSignal.logout()`: cihaz artık önceki kullanıcının bildirimlerini almaz. Misafirler bildirim almaz.
- Bildirime dokunma: bildirimdeki `request_id` / `room_id` verisi ilgili ilana veya sohbete götürür.
- Uygulama simgesi rozeti = okunmamış bildirim sayısı (sunucu her gönderimde `ios_badgeCount` ayarlar).

## Kurulum (adım adım)

### 1. OneSignal uygulaması
1. [OneSignal](https://onesignal.com) → **New App/Website** → platform olarak **Apple iOS (APNs)** seçin.
2. **Settings → Keys & IDs** sayfasından şunları alın:
   - **OneSignal App ID** → `donate-blood-ios/donateblood/Config/Secrets.xcconfig` içine `ONESIGNAL_APP_ID = ...`
   - **REST API Key** → yalnızca Edge Function secret'ına (aşağıda). **Uygulamaya veya repoya koymayın.**

### 2. APNs anahtarı (ücretli Apple Developer hesabı ister)
1. Apple Developer → Keys → **Apple Push Notifications service (APNs)** anahtarı oluşturun (.p8).
2. OneSignal → **Settings → Platforms → Apple iOS**: .p8 dosyası, Key ID, Team ID ve bundle id'yi girin.
3. Xcode imzalaması için ücretli ekibi seçin ve `Secrets.xcconfig` içinde `APP_ENTITLEMENTS = donateblood.entitlements` yapın
   (Push Notifications yeteneği + `aps-environment`).

### 3. Edge Function
```bash
cd supabase
supabase secrets set ONESIGNAL_APP_ID="<App ID>" ONESIGNAL_REST_API_KEY="<REST API Key>" WEBHOOK_SECRET="uzun-rastgele-bir-deger"
supabase functions deploy send-push --no-verify-jwt
```

### 4. Database Webhook
Supabase Dashboard → **Database → Webhooks → Create**:
- Table: `notifications`, Event: **Insert**
- Type: HTTP Request (POST), URL: `https://<proje>.supabase.co/functions/v1/send-push`
- Header: `x-webhook-secret: <WEBHOOK_SECRET ile aynı değer>`

### 5. Deneme
Gerçek cihazda (simülatör APNs almaz) uygulamayı açıp test hesabıyla giriş yapın ve profili tamamlayın → bildirim izni istenir.
OneSignal → **Audience → Subscriptions/Users**: kullanıcıyı `External ID` ile görmelisiniz.
Başka bir hesapla, kan grubunuza uyan bir ilan oluşturun.

## Sorun giderme

| Belirti | Olası neden |
|---|---|
| İzin penceresi hiç çıkmıyor | `ONESIGNAL_APP_ID` boş/yer tutucu (push kapalı) veya izin daha önce reddedilmiş (iOS Ayarlar'dan açın) |
| OneSignal'de kullanıcı görünmüyor | Giriş yapılmamış/misafir; ya da APNs anahtarı OneSignal'e yüklenmemiş |
| Edge Function 401 | `x-webhook-secret` başlığı `WEBHOOK_SECRET` ile aynı değil |
| Edge Function 500 | `ONESIGNAL_APP_ID` / `ONESIGNAL_REST_API_KEY` secret'ları tanımlı değil |
| Fonksiyon `no_recipient` diyor | O kullanıcının abone cihazı yok (izin verilmemiş veya çıkış yapılmış) |
| 502 `failed` | REST API Key veya App ID hatalı |

## Opsiyonel: zengin bildirimler
Görsel eklemek veya "teslim edildi" istatistikleri için OneSignal'in *Notification Service Extension* hedefini ekleyebilirsiniz
(OneSignal dokümantasyonu: iOS setup). Temel bildirimler bunsuz çalışır.

## Testler
```bash
cd supabase/functions/send-push && deno test -A onesignal_test.ts
```
