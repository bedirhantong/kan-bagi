-- =====================================================================
-- BİLDİRİM BÖLGELERİ
--
-- Kullanıcı yalnızca seçtiği il, ilçe ve hastanelerdeki ilanlar için bildirim alır.
-- Hiçbir şey seçmemişse ilan bildirimi GÖNDERİLMEZ (tüm Türkiye'ye yayın yapılmaz).
-- Toplam seçim sayısı sınırlıdır: bir ilan en fazla, o bölgeyi seçmiş kullanıcılara gider.
--
-- schema.sql'i bu dosyadan önce çalıştırdıysanız bunu çalıştırın (tekrar çalıştırılabilir).
-- Test kullanıcılarını (08_test_users.sql) bundan SONRA çalıştırın.
-- =====================================================================

-- Bölge anahtarı: il için "Antalya", ilçe için "Antalya/Muratpaşa".
-- Uygulama anahtarı kendisi üretmez; notification_areas() sonucundaki değeri saklar.
create or replace function public.area_key(p_city text, p_district text default null)
returns text language sql immutable as $$
    select case when p_city is null then null
                when p_district is null then p_city
                else p_city || '/' || p_district end
$$;

alter table public.notification_preferences
    add column if not exists preferred_areas text[] not null default '{}';

-- Seçim üst sınırı (uygulamadaki NotificationScope.maxSelections ile aynı olmalı).
alter table public.notification_preferences drop constraint if exists notification_scope_limit;
alter table public.notification_preferences add constraint notification_scope_limit
    check (cardinality(preferred_areas) + cardinality(preferred_hospital_ids) <= 10);

-- Yeni ilanda "bu bölgeyi seçenler" araması dizinden yapılır (&& ve @> işleçleri).
create index if not exists notification_preferences_areas_idx
    on public.notification_preferences using gin (preferred_areas);
create index if not exists notification_preferences_hospitals_idx
    on public.notification_preferences using gin (preferred_hospital_ids);

grant update (preferred_areas) on public.notification_preferences to authenticated;

-- Seçilebilir bölgeler: hastanesi olan iller ve ilçeler (ilan yalnızca hastanede açılabildiği için).
-- İl satırlarında district boştur; her satırda o bölgedeki hastane sayısı vardır.
create or replace function public.notification_areas()
returns table (area_key text, city text, district text, hospital_count integer)
language sql stable security invoker set search_path = public as $$
    select public.area_key(h.city_name), h.city_name, null::text, count(*)::integer
      from public.hospitals h
     where h.city_name is not null
     group by h.city_name
    union all
    select public.area_key(h.city_name, h.district_name), h.city_name, h.district_name, count(*)::integer
      from public.hospitals h
     where h.city_name is not null and h.district_name is not null
     group by h.city_name, h.district_name
     order by 2, 3 nulls first
$$;

revoke all on function public.notification_areas() from public, anon;
grant execute on function public.notification_areas() to authenticated;

-- Yeni ilan -> kan grubu uyan VE ilanın hastanesi/ilçesi/ili seçili olan kullanıcılara bildirim.
create or replace function public.notify_on_new_request()
returns trigger language plpgsql security definer set search_path = public as $$
declare
    h public.hospitals;
    keys text[];
begin
    select * into h from public.hospitals where id = new.hospital_id;
    keys := array_remove(array[public.area_key(h.city_name), public.area_key(h.city_name, h.district_name)], null);

    insert into public.notifications (user_id, type, title, body, data)
    select p.id, 'BLOOD_REQUEST',
           case when new.is_emergency then 'ACİL: ' else '' end || new.blood_type || ' kan grubu ihtiyacı',
           new.title,
           jsonb_build_object('request_id', new.id)
      from public.notification_preferences np
      join public.profiles p on p.id = np.user_id
     where np.blood_type_alerts
       -- bölge: seçilen il/ilçe ya da hastane (seçim yoksa hiç eşleşmez)
       and (np.preferred_areas && keys or np.preferred_hospital_ids @> array[new.hospital_id])
       and p.id <> new.owner_id
       and p.user_type = 'REGULAR_USER'
       -- kan grubu: seçim varsa seçime göre, yoksa profildeki gruba uyumluluğa göre
       and (case when cardinality(np.preferred_blood_types) > 0
                 then new.blood_type = any (np.preferred_blood_types)
                 else p.blood_type = any (public.compatible_donor_types(new.blood_type)) end)
       and not exists (select 1 from public.blocks b
                        where b.blocker_id = p.id and b.blocked_id = new.owner_id);
    return new;
end $$;
