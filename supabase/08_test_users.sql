-- =====================================================================
-- TEST KULLANICILARI (yalnızca geliştirme/test projelerinde çalıştırın!)
--
-- Apple/Google hesabı olmadan uygulamayı denemek için e-posta + şifre kullanıcıları oluşturur.
-- Şifre herkese açık bu dosyada yazdığı için ÜRETİM projesinde ASLA çalıştırmayın.
-- Tekrar çalıştırılabilir: var olan kullanıcıyı yeniden oluşturmaz, profilini günceller.
--
--   E-posta                 Şifre       Kan grubu   Rol
--   donor@test.dev          Test1234!   0+          bağışçı
--   donor2@test.dev         Test1234!   B-          bağışçı
--   requester@test.dev      Test1234!   A+          ilan sahibi (02_seed.sql ilanlarının sahibi)
--   newuser@test.dev        Test1234!   -           yeni kullanıcı: profili boş, girişte kayıt akışı açılır
--
-- Kayıt akışını yeniden denemek için bu dosyayı tekrar çalıştırın: newuser@test.dev profili yine boşaltılır.
--
-- Önkoşul: schema.sql (ve daha önce kurulduysa 12_notification_areas_patch.sql) çalıştırılmış olmalı.
-- Bildirim bölgeleri: bağışçılar Antalya'yı, ilan sahibi İstanbul'u seçmiş olarak gelir; newuser hiçbirini.
-- iOS uygulamasında "Test hesabıyla giriş" alanı Debug yapılandırmasında açıktır
-- (bkz. Config/Debug.xcconfig -> EMAIL_LOGIN_ENABLED).
-- =====================================================================

do $$
declare
    u record;
    uid uuid;
    pw text := 'Test1234!';
begin
    for u in
        select * from (values
            ('donor@test.dev',     'Test', 'Bağışçı',  '0+', 'MALE',   '+90 555 000 00 01', array['Antalya']),
            ('donor2@test.dev',    'Test', 'Bağışçı2', 'B-', 'OTHER',  '+90 555 000 00 02', array['Antalya/Muratpaşa']),
            ('requester@test.dev', 'Test', 'İlan Sahibi', 'A+', 'FEMALE', '+90 555 000 00 03', array['İstanbul']),
            ('newuser@test.dev',   null,   null,       null, null,     null,                 array[]::text[])
        ) as t(email, name, surname, blood_type, gender, phone, areas)
    loop
        select id into uid from auth.users where email = u.email;

        if uid is null then
            uid := gen_random_uuid();
            insert into auth.users (
                instance_id, id, aud, role, email, encrypted_password, email_confirmed_at,
                raw_app_meta_data, raw_user_meta_data, created_at, updated_at,
                confirmation_token, recovery_token, email_change, email_change_token_new, email_change_token_current,
                phone_change, phone_change_token, reauthentication_token)
            values (
                '00000000-0000-0000-0000-000000000000', uid, 'authenticated', 'authenticated', u.email,
                extensions.crypt(pw, extensions.gen_salt('bf')), now(),
                '{"provider":"email","providers":["email"]}'::jsonb,
                jsonb_strip_nulls(jsonb_build_object('full_name', u.name || ' ' || u.surname)), now(), now(),
                '', '', '', '', '', '', '', '');

            insert into auth.identities (id, provider_id, user_id, identity_data, provider, last_sign_in_at, created_at, updated_at)
            values (gen_random_uuid(), uid::text, uid,
                    jsonb_build_object('sub', uid::text, 'email', u.email, 'email_verified', true),
                    'email', now(), now(), now());
        end if;

        -- handle_new_user tetikleyicisi profili açar; burada doldurup tamamlıyoruz.
        -- Adı olmayan satır (newuser) boş ve tamamlanmamış bırakılır: girişte kayıt akışı açılır.
        update public.profiles
           set name = u.name, surname = u.surname, blood_type = u.blood_type, gender = u.gender,
               phone_number = u.phone,
               birth_date = case when u.name is null then null else date '1995-01-01' end,
               is_profile_completed = u.name is not null
         where id = uid;

        update public.notification_preferences
           set preferred_areas = u.areas, preferred_hospital_ids = '{}'
         where user_id = uid;
    end loop;
end $$;
