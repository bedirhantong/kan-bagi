-- =====================================================================
-- Blood Donation App - Supabase schema
-- Supabase Dashboard > SQL Editor'e tek parça olarak yapıştırıp çalıştırın.
-- Sıfır bir projeye göre yazıldı (idempotent değildir).
-- Ön koşul: Authentication > Providers > "Allow anonymous sign-ins" AÇIK olmalı.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. Yardımcılar
-- ---------------------------------------------------------------------
create or replace function public.set_updated_at()
returns trigger language plpgsql as $$
begin
    new.updated_at = now();
    return new;
end $$;

-- Anonim (misafir) olmayan, giriş yapmış kullanıcı mı?
create or replace function public.is_registered()
returns boolean language sql stable as $$
    select auth.uid() is not null
       and coalesce((auth.jwt() ->> 'is_anonymous')::boolean, false) = false
$$;

-- Alıcı kan grubuna bağış yapabilecek kan grupları.
create or replace function public.compatible_donor_types(recipient text)
returns text[] language sql immutable as $$
    select case recipient
        when 'A+'  then array['A+','A-','0+','0-']
        when 'A-'  then array['A-','0-']
        when 'B+'  then array['B+','B-','0+','0-']
        when 'B-'  then array['B-','0-']
        when 'AB+' then array['A+','A-','B+','B-','AB+','AB-','0+','0-']
        when 'AB-' then array['A-','B-','AB-','0-']
        when '0+'  then array['0+','0-']
        when '0-'  then array['0-']
        else array[]::text[]
    end
$$;

-- ---------------------------------------------------------------------
-- 1. Tablolar
-- ---------------------------------------------------------------------
create table public.profiles (
    id                   uuid primary key references auth.users(id) on delete cascade,
    name                 text,
    surname              text,
    email                text,
    phone_number         text,
    birth_date           date,
    blood_type           text check (blood_type in ('A+','A-','B+','B-','AB+','AB-','0+','0-')),
    gender               text check (gender in ('MALE','FEMALE','OTHER')),
    user_type            text not null default 'REGULAR_USER' check (user_type in ('REGULAR_USER','GUEST')),
    is_profile_completed boolean not null default false,
    created_at           timestamptz not null default now(),
    updated_at           timestamptz not null default now()
);
create trigger profiles_updated_at before update on public.profiles
    for each row execute function public.set_updated_at();

create table public.hospitals (
    id            integer generated always as identity primary key,
    name          text not null,
    address       text,
    phone_number  text,
    email         text,
    website       text,
    lat           double precision,
    lon           double precision,
    city_id       integer,
    city_name     text,
    district_id   integer,
    district_name text,
    country_id    integer not null default 1,
    country_name  text not null default 'Türkiye',
    hospital_icon text
);
create index hospitals_city_idx on public.hospitals (city_name);

-- Kızılay kan bağış noktaları (sabit birimler ve gezici ekipler; harita katmanı). Veri: 11_blood_donation_points_data.sql
create table public.blood_donation_points (
    id             integer generated always as identity primary key,
    external_id    text,
    name           text not null,
    address        text,
    neighborhood   text,
    district       text,
    province_code  integer,
    province_name  text,
    lat            double precision not null,
    lon            double precision not null,
    phone_number   text,
    kind           text not null default 'FIXED' check (kind in ('FIXED', 'MOBILE')),  -- sabit birim / gezici ekip
    unique (lat, lon)
);
create index blood_donation_points_province_idx on public.blood_donation_points (province_name);

create table public.blood_requests (
    id                uuid primary key default gen_random_uuid(),
    owner_id          uuid not null default auth.uid() references public.profiles(id) on delete cascade,
    owner_name        text,                       -- trigger ile doldurulur (profil gizli olduğu için)
    patient_full_name text not null,
    patient_age       integer check (patient_age between 0 and 120),
    title             text not null,
    description       text,
    blood_type        text not null check (blood_type in ('A+','A-','B+','B-','AB+','AB-','0+','0-')),
    is_active         boolean not null default true,
    is_verified       boolean not null default false,
    is_emergency      boolean not null default false,
    hospital_id       integer references public.hospitals(id),
    created_at        timestamptz not null default now(),
    updated_at        timestamptz not null default now()
);
create index blood_requests_feed_idx on public.blood_requests (is_active, created_at desc);
create index blood_requests_owner_idx on public.blood_requests (owner_id);
create index blood_requests_blood_type_idx on public.blood_requests (blood_type);
create trigger blood_requests_updated_at before update on public.blood_requests
    for each row execute function public.set_updated_at();

-- İlan sınırları (spam / bildirim yağmuru koruması): kullanıcı başına en fazla 3 aktif ilan,
-- 24 saatte en fazla 5 yeni ilan. SQL Editor / service role (auth.uid() boş) muaftır.
-- Sayılar uygulamadaki BloodRequestLimits ile aynı olmalı.
create or replace function public.enforce_request_limits()
returns trigger language plpgsql security definer set search_path = public as $$
declare
    max_active constant integer := 3;
    max_daily  constant integer := 5;
begin
    if auth.uid() is null then return new; end if;

    -- Aynı kullanıcının eşzamanlı istekleri sırayla sayılsın (yarış durumunda sınır aşılmasın).
    perform pg_advisory_xact_lock(hashtext('blood_request_limits:' || new.owner_id::text));

    if tg_op = 'INSERT' and (
        select count(*) from public.blood_requests
         where owner_id = new.owner_id and created_at > now() - interval '24 hours') >= max_daily then
        raise exception 'daily_request_limit' using errcode = 'P0001', hint = max_daily::text;
    end if;

    if new.is_active and (tg_op = 'INSERT' or not old.is_active) and (
        select count(*) from public.blood_requests
         where owner_id = new.owner_id and is_active and id <> new.id) >= max_active then
        raise exception 'active_request_limit' using errcode = 'P0001', hint = max_active::text;
    end if;

    return new;
end $$;

create trigger blood_requests_limits before insert or update of is_active on public.blood_requests
    for each row execute function public.enforce_request_limits();

create index blood_requests_owner_created_idx on public.blood_requests (owner_id, created_at desc);

-- İletişim numaraları ayrı tabloda: misafirler göremesin diye.
create table public.blood_request_contacts (
    request_id    uuid primary key references public.blood_requests(id) on delete cascade,
    phone_numbers text[] not null default '{}'
);

create table public.chat_rooms (
    id         uuid primary key default gen_random_uuid(),
    user1_id   uuid not null references public.profiles(id) on delete cascade,
    user2_id   uuid not null references public.profiles(id) on delete cascade,
    created_at timestamptz not null default now(),
    check (user1_id < user2_id),
    unique (user1_id, user2_id)
);

create table public.chat_messages (
    id         uuid primary key default gen_random_uuid(),
    room_id    uuid not null references public.chat_rooms(id) on delete cascade,
    sender_id  uuid not null default auth.uid() references public.profiles(id) on delete cascade,
    content    text not null check (char_length(content) between 1 and 2000),
    created_at timestamptz not null default now()
);
create index chat_messages_room_idx on public.chat_messages (room_id, created_at desc);

-- Bağışçı sağlık ön değerlendirme formu (kullanıcı başına bir kayıt).
create table public.health_forms (
    donor_id   uuid primary key references public.profiles(id) on delete cascade,
    answers    jsonb not null default '{}'::jsonb,   -- { "SAGLIKLI_MI": true, ... }
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
create trigger health_forms_updated_at before update on public.health_forms
    for each row execute function public.set_updated_at();

-- "Bağış yapmak istiyorum" niyet bildirimi (doğrulama yok, teşvik amaçlı).
create table public.donation_pledges (
    id         uuid primary key default gen_random_uuid(),
    request_id uuid not null references public.blood_requests(id) on delete cascade,
    donor_id   uuid not null default auth.uid() references public.profiles(id) on delete cascade,
    created_at timestamptz not null default now(),
    unique (request_id, donor_id)
);
create index donation_pledges_donor_idx on public.donation_pledges (donor_id);

create table public.notification_preferences (
    user_id           uuid primary key references public.profiles(id) on delete cascade,
    push_enabled      boolean not null default true,
    blood_type_alerts boolean not null default true,
    chat_notifications boolean not null default true,
    preferred_blood_types text[] not null default '{}',   -- boşsa profildeki kan grubuna uyumlu ilanlar
    -- Bildirim bölgesi: seçilen il/ilçeler (area_key) ve hastaneler. İkisi de boşsa ilan bildirimi gitmez.
    preferred_areas   text[] not null default '{}',
    preferred_hospital_ids integer[] not null default '{}',
    updated_at        timestamptz not null default now(),
    -- Seçim üst sınırı (uygulamadaki NotificationScope.maxSelections ile aynı olmalı).
    constraint notification_scope_limit
        check (cardinality(preferred_areas) + cardinality(preferred_hospital_ids) <= 10)
);
create index notification_preferences_areas_idx on public.notification_preferences using gin (preferred_areas);
create index notification_preferences_hospitals_idx on public.notification_preferences using gin (preferred_hospital_ids);

-- Bölge anahtarı: il için "Antalya", ilçe için "Antalya/Muratpaşa".
create or replace function public.area_key(p_city text, p_district text default null)
returns text language sql immutable as $$
    select case when p_city is null then null
                when p_district is null then p_city
                else p_city || '/' || p_district end
$$;
create trigger notification_preferences_updated_at before update on public.notification_preferences
    for each row execute function public.set_updated_at();

-- Uygulama içi bildirim kutusu. Push gönderimi bu tabloya bağlanan
-- Database Webhook -> Edge Function ile yapılır (bkz. supabase/README.md).
create table public.notifications (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid not null references public.profiles(id) on delete cascade,
    type       text not null check (type in ('BLOOD_REQUEST','DONATION_PLEDGE','CHAT_MESSAGE')),
    title      text not null,
    body       text not null,
    data       jsonb not null default '{}'::jsonb,
    is_read    boolean not null default false,
    created_at timestamptz not null default now()
);
create index notifications_user_idx on public.notifications (user_id, created_at desc);

create table public.blocks (
    blocker_id uuid not null default auth.uid() references public.profiles(id) on delete cascade,
    blocked_id uuid not null references public.profiles(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (blocker_id, blocked_id),
    check (blocker_id <> blocked_id)
);

create table public.reports (
    id          uuid primary key default gen_random_uuid(),
    reporter_id uuid not null default auth.uid() references public.profiles(id) on delete cascade,
    target_type text not null check (target_type in ('BLOOD_REQUEST','USER','CHAT_MESSAGE')),
    target_id   text not null,
    reason      text not null check (char_length(reason) between 1 and 500),
    created_at  timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- 2. Trigger'lar
-- ---------------------------------------------------------------------

-- Yeni auth kullanıcısı -> profil + bildirim tercihi.
create or replace function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = public as $$
declare
    full_name text := coalesce(new.raw_user_meta_data ->> 'full_name', new.raw_user_meta_data ->> 'name', '');
begin
    insert into public.profiles (id, name, surname, email, user_type)
    values (
        new.id,
        nullif(split_part(full_name, ' ', 1), ''),
        nullif(trim(substr(full_name, length(split_part(full_name, ' ', 1)) + 1)), ''),
        new.email,
        case when coalesce(new.is_anonymous, false) then 'GUEST' else 'REGULAR_USER' end
    );
    insert into public.notification_preferences (user_id) values (new.id);
    return new;
end $$;

create trigger on_auth_user_created after insert on auth.users
    for each row execute function public.handle_new_user();

-- Misafir hesap Apple/Google'a bağlanınca (linkIdentity) profili yükselt.
create or replace function public.handle_user_upgraded()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    if coalesce(old.is_anonymous, false) and not coalesce(new.is_anonymous, false) then
        update public.profiles
           set user_type = 'REGULAR_USER',
               email = coalesce(email, new.email)
         where id = new.id;
    end if;
    return new;
end $$;

create trigger on_auth_user_upgraded after update of is_anonymous on auth.users
    for each row execute function public.handle_user_upgraded();

-- İlan sahibi adını "Ad S." olarak denormalize et.
create or replace function public.fill_request_owner_name()
returns trigger language plpgsql security definer set search_path = public as $$
declare p public.profiles;
begin
    select * into p from public.profiles where id = new.owner_id;
    new.owner_name := nullif(trim(coalesce(p.name, '') || ' ' || coalesce(left(p.surname, 1) || '.', '')), '');
    return new;
end $$;

create trigger blood_requests_owner_name before insert on public.blood_requests
    for each row execute function public.fill_request_owner_name();

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

create trigger blood_requests_notify after insert on public.blood_requests
    for each row execute function public.notify_on_new_request();

-- Bağış niyeti -> ilan sahibine bildirim.
create or replace function public.notify_on_pledge()
returns trigger language plpgsql security definer set search_path = public as $$
declare r public.blood_requests;
begin
    select * into r from public.blood_requests where id = new.request_id;
    insert into public.notifications (user_id, type, title, body, data)
    values (r.owner_id, 'DONATION_PLEDGE', 'Bir bağışçı yardım etmek istiyor',
            '"' || r.title || '" ilanınız için bir kullanıcı bağış yapmak istediğini belirtti.',
            jsonb_build_object('request_id', r.id));
    return new;
end $$;

create trigger donation_pledges_notify after insert on public.donation_pledges
    for each row execute function public.notify_on_pledge();

-- Yeni mesaj -> karşı tarafa bildirim.
create or replace function public.notify_on_message()
returns trigger language plpgsql security definer set search_path = public as $$
declare
    room public.chat_rooms;
    receiver uuid;
    sender_name text;
begin
    select * into room from public.chat_rooms where id = new.room_id;
    receiver := case when room.user1_id = new.sender_id then room.user2_id else room.user1_id end;
    select coalesce(name, 'Yeni mesaj') into sender_name from public.profiles where id = new.sender_id;

    insert into public.notifications (user_id, type, title, body, data)
    select receiver, 'CHAT_MESSAGE', sender_name, left(new.content, 140),
           jsonb_build_object('room_id', new.room_id)
     where exists (select 1 from public.notification_preferences np
                    where np.user_id = receiver and np.chat_notifications);
    return new;
end $$;

create trigger chat_messages_notify after insert on public.chat_messages
    for each row execute function public.notify_on_message();

-- ---------------------------------------------------------------------
-- 3. RPC'ler
-- ---------------------------------------------------------------------

-- Bildirim için seçilebilir bölgeler: hastanesi olan iller ve ilçeler (hastane sayısıyla).
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

-- İki kullanıcı arasında oda getir / oluştur.
create or replace function public.get_or_create_chat_room(other_user uuid)
returns uuid language plpgsql security definer set search_path = public as $$
declare
    me uuid := auth.uid();
    a uuid; b uuid; room uuid;
begin
    if not public.is_registered() then raise exception 'guest_not_allowed' using errcode = '42501'; end if;
    if other_user is null or other_user = me then raise exception 'invalid_user'; end if;
    if exists (select 1 from public.blocks
                where (blocker_id = me and blocked_id = other_user)
                   or (blocker_id = other_user and blocked_id = me)) then
        raise exception 'blocked' using errcode = '42501';
    end if;
    a := least(me, other_user); b := greatest(me, other_user);
    insert into public.chat_rooms (user1_id, user2_id) values (a, b)
    on conflict (user1_id, user2_id) do nothing;
    select id into room from public.chat_rooms where user1_id = a and user2_id = b;
    return room;
end $$;

-- Kullanıcının odaları + karşı taraf + son mesaj.
create or replace function public.get_chat_rooms()
returns table (
    room_id uuid, other_user_id uuid, other_user_name text,
    last_message text, last_message_at timestamptz
) language sql stable security definer set search_path = public as $$
    select r.id,
           o.id,
           nullif(trim(coalesce(o.name, '') || ' ' || coalesce(o.surname, '')), ''),
           lm.content,
           coalesce(lm.created_at, r.created_at)
      from public.chat_rooms r
      join public.profiles o on o.id = case when r.user1_id = auth.uid() then r.user2_id else r.user1_id end
      left join lateral (
            select m.content, m.created_at from public.chat_messages m
             where m.room_id = r.id order by m.created_at desc limit 1) lm on true
     where auth.uid() in (r.user1_id, r.user2_id)
       and not exists (select 1 from public.blocks b
                        where b.blocker_id = auth.uid() and b.blocked_id = o.id)
     order by 5 desc
$$;

-- Yakındaki hastaneler (haversine, km).
create or replace function public.hospitals_nearby(user_lat double precision, user_lon double precision, max_km double precision default 50)
returns setof public.hospitals language sql stable as $$
    select h.* from public.hospitals h
     where h.lat is not null and h.lon is not null
       and 6371 * 2 * asin(sqrt(
             power(sin(radians(h.lat - user_lat) / 2), 2) +
             cos(radians(user_lat)) * cos(radians(h.lat)) *
             power(sin(radians(h.lon - user_lon) / 2), 2))) <= max_km
     order by 6371 * 2 * asin(sqrt(
             power(sin(radians(h.lat - user_lat) / 2), 2) +
             cos(radians(user_lat)) * cos(radians(h.lat)) *
             power(sin(radians(h.lon - user_lon) / 2), 2)))
     limit 300
$$;

-- İlan + iletişim numaralarını atomik oluştur (security invoker: RLS ve column grant'ler geçerli).
create or replace function public.create_blood_request(
    p_patient_full_name text, p_patient_age integer, p_title text, p_description text,
    p_blood_type text, p_hospital_id integer, p_phone_numbers text[], p_is_emergency boolean default false)
returns uuid language plpgsql security invoker set search_path = public as $$
declare new_id uuid;
begin
    if coalesce(cardinality(p_phone_numbers), 0) not between 1 and 3 then
        raise exception 'invalid_phone_numbers';
    end if;
    insert into public.blood_requests (patient_full_name, patient_age, title, description, blood_type, hospital_id, is_emergency)
    values (p_patient_full_name, p_patient_age, p_title, p_description, p_blood_type, p_hospital_id, p_is_emergency)
    returning id into new_id;
    insert into public.blood_request_contacts (request_id, phone_numbers) values (new_id, p_phone_numbers);
    return new_id;
end $$;

create or replace function public.update_blood_request(
    p_id uuid, p_patient_full_name text, p_patient_age integer, p_title text, p_description text,
    p_blood_type text, p_hospital_id integer, p_phone_numbers text[], p_is_emergency boolean default false)
returns void language plpgsql security invoker set search_path = public as $$
begin
    if coalesce(cardinality(p_phone_numbers), 0) not between 1 and 3 then
        raise exception 'invalid_phone_numbers';
    end if;
    update public.blood_requests
       set patient_full_name = p_patient_full_name, patient_age = p_patient_age, title = p_title,
           description = p_description, blood_type = p_blood_type, hospital_id = p_hospital_id,
           is_emergency = p_is_emergency
     where id = p_id;
    if not found then raise exception 'not_found_or_forbidden' using errcode = '42501'; end if;
    insert into public.blood_request_contacts (request_id, phone_numbers) values (p_id, p_phone_numbers)
    on conflict (request_id) do update set phone_numbers = excluded.phone_numbers;
end $$;

-- Yakındaki kan bağış noktaları (haversine, km); en yakın önce, en fazla 300.
create or replace function public.blood_donation_points_nearby(
    user_lat double precision, user_lon double precision, max_km double precision default 50)
returns setof public.blood_donation_points language sql stable as $$
    select p.* from public.blood_donation_points p
     where 6371 * 2 * asin(sqrt(
             power(sin(radians(p.lat - user_lat) / 2), 2) +
             cos(radians(user_lat)) * cos(radians(p.lat)) *
             power(sin(radians(p.lon - user_lon) / 2), 2))) <= max_km
     order by 6371 * 2 * asin(sqrt(
             power(sin(radians(p.lat - user_lat) / 2), 2) +
             cos(radians(user_lat)) * cos(radians(p.lat)) *
             power(sin(radians(p.lon - user_lon) / 2), 2)))
     limit 300
$$;

-- Hesap silme (App Store zorunluluğu). auth.users silinince her şey cascade olur.
create or replace function public.delete_my_account()
returns void language plpgsql security definer set search_path = public, auth as $$
begin
    if auth.uid() is null then raise exception 'not_authenticated' using errcode = '42501'; end if;
    delete from auth.users where id = auth.uid();
end $$;

-- ---------------------------------------------------------------------
-- 4. Yetkiler (column-level) ve RLS
-- ---------------------------------------------------------------------
revoke all on all tables in schema public from anon, authenticated;
revoke all on all functions in schema public from anon, authenticated, public;

grant usage on schema public to anon, authenticated;

-- RPC'ler
grant execute on function public.blood_donation_points_nearby(double precision, double precision, double precision) to anon, authenticated;
grant execute on function public.get_or_create_chat_room(uuid) to authenticated;
grant execute on function public.get_chat_rooms() to authenticated;
grant execute on function public.hospitals_nearby(double precision, double precision, double precision) to anon, authenticated;
grant execute on function public.delete_my_account() to authenticated;
grant execute on function public.create_blood_request(text, integer, text, text, text, integer, text[], boolean) to authenticated;
grant execute on function public.update_blood_request(uuid, text, integer, text, text, text, integer, text[], boolean) to authenticated;
-- RLS politikalarında kullanılan yardımcılar
grant execute on function public.is_registered() to anon, authenticated;

-- profiles: sadece sahibi okur; user_type/email istemciden değiştirilemez.
alter table public.profiles enable row level security;
grant select on public.profiles to authenticated;
grant update (name, surname, phone_number, birth_date, blood_type, gender, is_profile_completed) on public.profiles to authenticated;
create policy profiles_select_own on public.profiles for select to authenticated using (id = auth.uid());
create policy profiles_update_own on public.profiles for update to authenticated
    using (id = auth.uid()) with check (id = auth.uid());

-- hospitals: herkes okur (misafir dahil), yazma yalnızca service role.
alter table public.hospitals enable row level security;
grant select on public.hospitals to anon, authenticated;
create policy hospitals_select_all on public.hospitals for select to anon, authenticated using (true);

-- blood_donation_points: herkes okur (misafir dahil), yazma yalnızca service role.
alter table public.blood_donation_points enable row level security;
grant select on public.blood_donation_points to anon, authenticated;
create policy blood_donation_points_select_all on public.blood_donation_points for select to anon, authenticated using (true);

-- blood_requests: aktif ilanları herkes okur, sahibi kendi ilanlarını (pasif dahil) görür.
alter table public.blood_requests enable row level security;
grant select on public.blood_requests to anon, authenticated;
grant insert (patient_full_name, patient_age, title, description, blood_type, hospital_id, is_emergency) on public.blood_requests to authenticated;
grant update (patient_full_name, patient_age, title, description, blood_type, hospital_id, is_active, is_emergency) on public.blood_requests to authenticated;
grant delete on public.blood_requests to authenticated;
create policy requests_select on public.blood_requests for select to anon, authenticated using (
    (is_active or owner_id = auth.uid())
    and not exists (select 1 from public.blocks b
                     where b.blocker_id = auth.uid() and b.blocked_id = blood_requests.owner_id)
);
create policy requests_insert on public.blood_requests for insert to authenticated
    with check (public.is_registered() and owner_id = auth.uid());
create policy requests_update on public.blood_requests for update to authenticated
    using (owner_id = auth.uid()) with check (owner_id = auth.uid());
create policy requests_delete on public.blood_requests for delete to authenticated
    using (owner_id = auth.uid());

-- blood_request_contacts: yalnızca kayıtlı kullanıcılar okur, sahibi yazar.
alter table public.blood_request_contacts enable row level security;
grant select, insert, update, delete on public.blood_request_contacts to authenticated;
create policy contacts_select on public.blood_request_contacts for select to authenticated using (
    public.is_registered() and exists (select 1 from public.blood_requests r where r.id = request_id)
);
create policy contacts_write on public.blood_request_contacts for all to authenticated
    using (exists (select 1 from public.blood_requests r where r.id = request_id and r.owner_id = auth.uid()))
    with check (exists (select 1 from public.blood_requests r where r.id = request_id and r.owner_id = auth.uid()));

-- chat: yalnızca oda katılımcıları. Oda oluşturma RPC ile.
alter table public.chat_rooms enable row level security;
grant select on public.chat_rooms to authenticated;
create policy rooms_select on public.chat_rooms for select to authenticated
    using (auth.uid() in (user1_id, user2_id));

alter table public.chat_messages enable row level security;
grant select on public.chat_messages to authenticated;
grant insert (room_id, content) on public.chat_messages to authenticated;
create policy messages_select on public.chat_messages for select to authenticated using (
    exists (select 1 from public.chat_rooms r where r.id = room_id and auth.uid() in (r.user1_id, r.user2_id))
);
create policy messages_insert on public.chat_messages for insert to authenticated with check (
    public.is_registered() and sender_id = auth.uid() and exists (
        select 1 from public.chat_rooms r
         where r.id = room_id and auth.uid() in (r.user1_id, r.user2_id)
           and not exists (select 1 from public.blocks b
                            where (b.blocker_id = r.user1_id and b.blocked_id = r.user2_id)
                               or (b.blocker_id = r.user2_id and b.blocked_id = r.user1_id)))
);

-- health_forms: yalnızca sahibi, misafir yazamaz.
alter table public.health_forms enable row level security;
grant select, insert, update, delete on public.health_forms to authenticated;
create policy health_select_own on public.health_forms for select to authenticated using (donor_id = auth.uid());
create policy health_write_own on public.health_forms for all to authenticated
    using (donor_id = auth.uid()) with check (public.is_registered() and donor_id = auth.uid());

-- donation_pledges: bağışçı kendi niyetlerini, ilan sahibi ilanına gelenleri (sayı için) görür.
alter table public.donation_pledges enable row level security;
grant select on public.donation_pledges to authenticated;
grant insert (request_id) on public.donation_pledges to authenticated;
grant delete on public.donation_pledges to authenticated;
create policy pledges_select on public.donation_pledges for select to authenticated using (
    donor_id = auth.uid()
    or exists (select 1 from public.blood_requests r where r.id = request_id and r.owner_id = auth.uid())
);
create policy pledges_insert on public.donation_pledges for insert to authenticated
    with check (public.is_registered() and donor_id = auth.uid());
create policy pledges_delete on public.donation_pledges for delete to authenticated
    using (donor_id = auth.uid());

-- Bir ilana kaç kişi niyet bildirdi (herkese açık sayı, kimlik yok).
create or replace function public.pledge_count(p_request_id uuid)
returns integer language sql stable security definer set search_path = public as $$
    select count(*)::integer from public.donation_pledges where request_id = p_request_id
$$;
grant execute on function public.pledge_count(uuid) to anon, authenticated;

-- notification_preferences
alter table public.notification_preferences enable row level security;
grant select on public.notification_preferences to authenticated;
grant update (push_enabled, blood_type_alerts, chat_notifications, preferred_blood_types, preferred_areas, preferred_hospital_ids) on public.notification_preferences to authenticated;
create policy prefs_select_own on public.notification_preferences for select to authenticated using (user_id = auth.uid());
create policy prefs_update_own on public.notification_preferences for update to authenticated
    using (user_id = auth.uid()) with check (user_id = auth.uid());

-- notifications
alter table public.notifications enable row level security;
grant select on public.notifications to authenticated;
grant update (is_read) on public.notifications to authenticated;
grant delete on public.notifications to authenticated;
create policy notifications_select_own on public.notifications for select to authenticated using (user_id = auth.uid());
create policy notifications_update_own on public.notifications for update to authenticated
    using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy notifications_delete_own on public.notifications for delete to authenticated using (user_id = auth.uid());

-- blocks
alter table public.blocks enable row level security;
-- anon'un select yetkisi, blood_requests politikasındaki blocks alt sorgusu içindir (RLS ile satır görmez).
grant select on public.blocks to anon;
grant select, insert, delete on public.blocks to authenticated;
create policy blocks_own on public.blocks for all to authenticated
    using (blocker_id = auth.uid()) with check (public.is_registered() and blocker_id = auth.uid());

-- reports: yalnızca yazma (inceleme dashboard'dan yapılır).
alter table public.reports enable row level security;
grant insert (target_type, target_id, reason) on public.reports to authenticated;
create policy reports_insert on public.reports for insert to authenticated
    with check (public.is_registered() and reporter_id = auth.uid());

-- Ana sayfa akışı (yakınlığa göre sıralı; ayrıntı için 07_feed_by_distance_patch.sql)
create or replace function public.blood_request_feed(
    user_lat double precision default null,
    user_lon double precision default null,
    p_limit integer default 20,
    p_offset integer default 0)
returns setof public.blood_requests
language sql stable security invoker set search_path = public as $$
    select r.*
      from public.blood_requests r
      left join public.hospitals h on h.id = r.hospital_id
     where r.is_active
     order by
        case when user_lat is not null and user_lon is not null and h.lat is not null and h.lon is not null
             then 6371 * 2 * asin(sqrt(
                    power(sin(radians(h.lat - user_lat) / 2), 2) +
                    cos(radians(user_lat)) * cos(radians(h.lat)) *
                    power(sin(radians(h.lon - user_lon) / 2), 2)))
        end asc nulls last,
        r.created_at desc
     limit least(greatest(p_limit, 1), 50)
    offset greatest(p_offset, 0)
$$;

revoke all on function public.blood_request_feed(double precision, double precision, integer, integer) from public;
grant execute on function public.blood_request_feed(double precision, double precision, integer, integer) to anon, authenticated;

-- ---------------------------------------------------------------------
-- 5. Realtime
-- ---------------------------------------------------------------------
alter publication supabase_realtime add table public.chat_messages;
alter publication supabase_realtime add table public.notifications;
