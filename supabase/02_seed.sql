-- =====================================================================
-- schema.sql'den SONRA çalıştırın (tekrar çalıştırılabilir). İçerik:
--   1) stories tablosu (ana sayfadaki story şeridi)
--   2) örnek hastaneler   (KOORDİNATLAR YAKLAŞIKTIR - gerçek veriyle değiştirin)
--   3) story'ler (Android'deki Contentful içeriği)
--   4) örnek ilanlar (requester@test.dev (08_test_users.sql) ya da mevcut ilk profili sahip olarak kullanır; önce uygulamadan
--      en az bir kez misafir/Apple/Google ile giriş yapmış olmalısınız)
-- =====================================================================

-- 1) Stories
create table if not exists public.stories (
    id         uuid primary key default gen_random_uuid(),
    title      text not null,
    body       text,
    image_url  text,
    logo_url   text,
    link_url   text,
    sort_order integer not null default 0,
    is_active  boolean not null default true,
    created_at timestamptz not null default now()
);
alter table public.stories enable row level security;
revoke all on public.stories from anon, authenticated;
grant select on public.stories to anon, authenticated;
drop policy if exists stories_select on public.stories;
create policy stories_select on public.stories for select to anon, authenticated using (is_active);

-- 2) Hastaneler
with new_rows (name, address, phone_number, lat, lon, city_name, district_name) as (values
('Akdeniz Üniversitesi Hastanesi','Pınarbaşı, Akdeniz Ünv., 07070 Konyaaltı/Antalya','+90 242 249 60 00',36.8967,30.6473,'Antalya','Konyaaltı'),
('Antalya Atatürk Devlet Hastanesi','Üçgen, Anafartalar Cd. No:100, 07040 Muratpaşa/Antalya','+90 242 244 44 44',36.8895,30.7017,'Antalya','Muratpaşa'),
('Antalya Eğitim ve Araştırma Hastanesi','Varlık, Kazım Karabekir Cd., 07100 Muratpaşa/Antalya','+90 242 249 44 00',36.8877,30.6835,'Antalya','Muratpaşa'),
('Medical Park Antalya Hastanesi','Fener, Tekelioğlu Cd. No:7, 07160 Muratpaşa/Antalya','+90 242 314 15 15',36.8748,30.6912,'Antalya','Muratpaşa'),
('İstanbul Üniversitesi İstanbul Tıp Fakültesi','Turgut Özal Millet Cd., 34093 Fatih/İstanbul','+90 212 414 20 00',41.0128,28.9440,'İstanbul','Fatih'),
('Marmara Üniversitesi Pendik Eğitim ve Araştırma Hastanesi','Fevzi Çakmak, Muhsin Yazıcıoğlu Cd., 34899 Pendik/İstanbul','+90 216 625 45 45',40.9052,29.2143,'İstanbul','Pendik'),
('Ankara Şehir Hastanesi','Üniversiteler, 1604. Cd. No:9, 06800 Çankaya/Ankara','+90 312 552 60 00',39.9337,32.8115,'Ankara','Çankaya'),
('Hacettepe Üniversitesi Hastaneleri','Sıhhiye, 06230 Altındağ/Ankara','+90 312 305 10 00',39.9317,32.8619,'Ankara','Altındağ'),
('Ege Üniversitesi Tıp Fakültesi Hastanesi','Kazımdirik, Ege Ünv. Cd., 35100 Bornova/İzmir','+90 232 390 11 11',38.4552,27.2286,'İzmir','Bornova'),
('Dokuz Eylül Üniversitesi Hastanesi','Mithatpaşa Cd. No:1606, 35340 İnciraltı/İzmir','+90 232 412 22 22',38.3970,27.0424,'İzmir','Balçova'),
('Uludağ Üniversitesi Sağlık Uygulama ve Araştırma Hastanesi','Görükle Kampüsü, 16059 Nilüfer/Bursa','+90 224 295 00 00',40.2226,28.8737,'Bursa','Nilüfer'),
('Erciyes Üniversitesi Hastaneleri','Kayseri Yolu Üzeri, 38039 Melikgazi/Kayseri','+90 352 207 66 66',38.7089,35.5280,'Kayseri','Melikgazi'))
insert into public.hospitals (name, address, phone_number, lat, lon, city_name, district_name)
select * from new_rows n where not exists (select 1 from public.hospitals h where h.name = n.name);

-- 3) Story'ler (Android'deki Contentful içeriği; görseller Contentful CDN'de.
--    Supabase Storage'a taşıyınca image_url / logo_url alanlarını güncelleyin.)
delete from public.stories;
insert into public.stories (title, body, image_url, logo_url, link_url, sort_order) values
('Beşiktaş Seninle Yaşatmaya Geldik!','Kan acil değil, sürekli bir ihtiyaçtır. Tüm vatandaşlarımızı ve taraftarlarımızı kan bağışı yapmaya davet ediyoruz. 🩸','https://images.ctfassets.net/saknko8czctd/6S70J4gR04n40BcAZ0d9vy/44ff076aea9bb7f1a0326333de8b7ed2/bjk_content.jpeg','https://images.ctfassets.net/saknko8czctd/7AOhiMkeJfZIsTqVI3418Q/38e1e2039c0ae66ad65511d94b851677/bjk_logo.png','https://www.linkedin.com/posts/besiktasjk_be%C5%9Fikta%C5%9F-seninle-ya%C5%9Fatmaya-geldik-kan-acil-activity-7330938588169162752-cKhD?utm_source=share&utm_medium=member_desktop&rcm=ACoAADF9D-IBqdGo8VBsEM4HxvHZEG743kXrvYc',1),
('Kepez Belediyesi’nden Kızılay’a kan bağışı desteği','Kepez Belediyesi personelleri, “Bağışlanan bir ünite kan kurtarılan üç can” diyerek Kızılay’a kan bağışında bulundu.','https://images.ctfassets.net/saknko8czctd/eJq9iS0q1kJrW8JrKpjgk/e1d743e7a90461ccde3a76d7999691a1/ant.jpg','https://images.ctfassets.net/saknko8czctd/1kMdaJdrNRsUUTZfQZWRrv/3986f9ffe3665f948d8754fbd43d6ca8/kepez.png','https://www.kepez-bld.gov.tr/news_11332_kepez-belediyesi-nden-kizilay-a-kan-bagisi-destegi',2),
('Kızılay’dan Kan Bağışı Çağrısı','Kan bağışında bulunmak isteyen Üniversitemiz mensuplarının Kan Bağış Merkezi’ne ve Markantalya AVM önünde yer alan Kan Bağış noktasına başvurmaları önemle rica olunur.','https://images.ctfassets.net/saknko8czctd/2LMH2T5lw2ciHOGdF0FClK/ddd828dc6515ca150f0942fd85c94ae5/au-hast.jpeg','https://images.ctfassets.net/saknko8czctd/17oMy3od5zG27wqn6nfH07/a5f88c425ccac16b1cb0922132e1f491/akdeniz.png','https://www.akdeniz.edu.tr/tr/duyuru/kizilay%E2%80%99dan_kan_bagisi_cagrisi-5428',3),
('Bağışlanan Her Kan, Kurtarılan Üç Can','Kan bağışını teşvik etmek, kan bağışı hakkında gerekli bilgilendirme ve farkındalık yaratmak amacıyla düzenlenen kampanya 27-28-29 Nisan 2015 tarihlerinde Sütlüce Yerleşkemizde 29 Nisan tarihinde ise Küçükyalı ve Eminönü yerleşkelerinde düzenlenecek.','https://images.ctfassets.net/saknko8czctd/1tG5R6v88gASEBItrDNa2a/ef5e0789de5d61f525686520447e7717/kan.jpg','https://images.ctfassets.net/saknko8czctd/60Dj8SKaG0EcwiLdThzmRu/d10c1c23d6cbe818505e9d777246775a/ito.png','https://ticaret.edu.tr/bagislanan-her-kan-kurtarilan-uc-can-kan-bagisi-kampanyasi/',4),
('Kızılay Kan Bağışı Kampanyası','Kızılay''ın kan bağışı faaliyetlerinin sürdürülebilirliğinin sağlanması ve kan bağışçısı kazanımının artırılması sağlamak amacıyla " Okulumda ''KAN'' Panya Var"projesi kapsamında okulumuzda kan bağışı etkinliği düzenlendi.','https://images.ctfassets.net/saknko8czctd/5Uv2o2eZTp2chDmkJUBphM/d11e29db69059122be28debe5e18cb62/kampanya.jpg','https://images.ctfassets.net/saknko8czctd/6PBB499Vz7eFBaAFn2a9rH/23477fbf10e1a288833d357f988a196f/kizilay.jpg','https://sinopfatihilkokulu.meb.k12.tr/icerikler/kizilay-kan-bagisi-kampanyasi_15924528.html',5),
('Kızılay Gönüllülük','Kızılay, Hedef 2030 Stratejisi bağlamında tüm süreçlerinde gönüllü katılımını sağlamayı, gönüllülerinin aktif ve nitelikli katkılarının verimli kullanabilmeyi hedefleyen Kızılay, Gönüllü Yönetim Sistemi – gonulluol.org’u hayata geçirdi.','https://images.ctfassets.net/saknko8czctd/5MGXfaDQI8dixoVCeSvbnJ/404728bb3ae5febf647b867bfe7333c4/gonulu_ol.png','https://images.ctfassets.net/saknko8czctd/4nCPWY9FdzArMhW7XHW5Ku/16f9d8a6367fd50626f4a9c7590f3ec9/kan_bagisi_icon.png','https://www.kizilay.org.tr/kurumsal/gonulluol',6),
('İlk 6 Ayda 1.373.168 Ünite Kan Bağışı','Ülke kan ihtiyacını karşılamak için 7/24 çalışan Türk Kızılay''a 2024 yılının ilk 6 ayında 1 milyon 373 bin 168 ünite kan bağışı yapıldı','https://images.ctfassets.net/saknko8czctd/TWPMq2Gyf88lEOcD0JJQ0/bda2414d748e59e5442b4cda63dbe3f4/bayrak.jpg','https://images.ctfassets.net/saknko8czctd/6PBB499Vz7eFBaAFn2a9rH/23477fbf10e1a288833d357f988a196f/kizilay.jpg','https://www.kizilay.org.tr/Haber/HaberDetay/7776',7),
('Birbirimize Candan Bağlıyız','Kış şartlarının hayatı iyice zorlaştırmasından önce kan stoklarını belirli bir seviyenin üzerine çıkarmayı ve düzenli bağışı özendirmeyi hedefleyen Kızılay, ülke çapında “Birbirimize candan bağlıyız” sloganı ile büyük bir kan bağış kampanyası başlattı.','https://images.ctfassets.net/saknko8czctd/5OSYvCp6fyQKmCst4b6wsJ/60dad1076d6f0ea9f25cc314efb75698/candan_bagliyiz.jpg','https://images.ctfassets.net/saknko8czctd/6PBB499Vz7eFBaAFn2a9rH/23477fbf10e1a288833d357f988a196f/kizilay.jpg','https://www.kizilay.org.tr/Haber/KurumsalHaberDetay/7577',8),
('Bağış Yap','Düzenli kan bağışı sağlıklı yaşam demektir.','https://images.ctfassets.net/saknko8czctd/PFglaV5bk6YoOyoGXQ0XH/bf308acf049193730c68bb6fbfae1377/bagis_detay.jpg','https://images.ctfassets.net/saknko8czctd/4nCPWY9FdzArMhW7XHW5Ku/16f9d8a6367fd50626f4a9c7590f3ec9/kan_bagisi_icon.png','https://blood-app-ribufing.vercel.app/',9);

-- 4) Örnek ilanlar (tek bir profilin sahibi olarak)
do $$
declare
    -- Tercihen 08_test_users.sql'deki ilan sahibi; yoksa ilk profil.
    owner uuid := coalesce(
        (select id from auth.users where email = 'requester@test.dev'),
        (select id from public.profiles order by created_at limit 1));
    h1 integer := (select id from public.hospitals where name like 'Akdeniz%' limit 1);
    h2 integer := (select id from public.hospitals where name like 'Antalya Atatürk%' limit 1);
    h3 integer := (select id from public.hospitals where name like 'Ankara Şehir%' limit 1);
    h4 integer := (select id from public.hospitals where name like 'Ege Üniversitesi%' limit 1);
    rid uuid;
begin
    if exists (select 1 from public.blood_requests) then
        raise notice 'Zaten ilan var, örnek ilanlar eklenmedi.';
        return;
    end if;
    if owner is null then
        raise notice 'Profil yok: önce uygulamadan giriş yapın, sonra bu bloğu tekrar çalıştırın.';
        return;
    end if;

    insert into public.blood_requests (owner_id, patient_full_name, patient_age, title, description, blood_type, hospital_id, is_verified)
    values (owner, 'Ömer Faruk Özsoy', 23, 'Yardım Amaçlı', 'Hastane içerisinde iletişime geçmek için arayabilirsiniz.', 'A+', h1, true) returning id into rid;
    insert into public.blood_request_contacts values (rid, array['+90 532 000 00 01']);

    insert into public.blood_requests (owner_id, patient_full_name, patient_age, title, description, blood_type, hospital_id, is_emergency)
    values (owner, 'Hasan Almaz', 26, 'Acil AB- kan ihtiyacı', 'Ameliyat için acil ihtiyaç var, destek olabilecekler lütfen ulaşsın.', 'AB-', h2, true) returning id into rid;
    insert into public.blood_request_contacts values (rid, array['+90 532 000 00 02','+90 532 000 00 03']);

    insert into public.blood_requests (owner_id, patient_full_name, patient_age, title, description, blood_type, hospital_id)
    values (owner, 'Zeynep Kaya', 41, '0+ kan aranıyor', 'Yoğun bakımda tedavi görüyor.', '0+', h3) returning id into rid;
    insert into public.blood_request_contacts values (rid, array['+90 532 000 00 04']);

    insert into public.blood_requests (owner_id, patient_full_name, patient_age, title, description, blood_type, hospital_id)
    values (owner, 'Mehmet Demir', 8, 'Çocuk hasta için B+ kan', 'Lösemi tedavisi için trombosit ve kan bağışı gerekiyor.', 'B+', h4) returning id into rid;
    insert into public.blood_request_contacts values (rid, array['+90 532 000 00 05']);
end $$;
