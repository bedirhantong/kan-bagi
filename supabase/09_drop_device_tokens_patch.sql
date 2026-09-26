-- Push artık OneSignal ile yapılıyor: cihaz jetonları uygulamada/veritabanında tutulmaz
-- (kullanıcı, Supabase kullanıcı id'siyle OneSignal'e bağlanır). schema.sql'i daha önce çalıştırdıysanız çalıştırın.
drop table if exists public.device_tokens;
