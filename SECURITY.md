# Güvenlik Politikası

## Açık bildirmek

Bir güvenlik açığı bulduysanız **herkese açık issue açmayın.** GitHub'daki *Security → Report a vulnerability* (private advisory)
özelliğini kullanın; bu mümkün değilse depo sahibine e-posta ile ulaşın. 72 saat içinde yanıt vermeyi hedefliyoruz.

## Kapsam ve tasarım ilkeleri

- İstemci yalnızca Supabase **anon (publishable) key** kullanır. `service_role`/secret anahtarlar uygulamaya, repoya veya issue'lara **asla** konmaz.
  Yanlışlıkla paylaşıldıysa Supabase panelinden hemen yenileyin.
- Yetkilendirme sunucuda **Row Level Security** ile sağlanır (`supabase/schema.sql`). Misafir (anonim) kullanıcılar yazamaz, telefon numaralarını göremez.
- Kişisel/sağlık verileri (profil, sağlık formu) yalnızca sahibi tarafından okunabilir.
- `supabase/08_test_users.sql` genel bilinen şifreler içerir: **yalnızca geliştirme projelerinde** kullanın.

Bir RLS politikasında veya RPC'de yetki aşımı buluyorsanız lütfen adım adım nasıl yeniden üretileceğini belirtin.
