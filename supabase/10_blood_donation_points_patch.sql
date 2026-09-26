-- Kızılay kan bağış noktaları: tablo, RLS ve yakınlık fonksiyonu.
-- schema.sql'i daha önce çalıştırdıysanız bunu, ardından 11_blood_donation_points_data.sql dosyasını çalıştırın.
-- Tekrar çalıştırılabilir.
create table if not exists public.blood_donation_points (
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
create index if not exists blood_donation_points_province_idx on public.blood_donation_points (province_name);

alter table public.blood_donation_points enable row level security;
grant select on public.blood_donation_points to anon, authenticated;
drop policy if exists blood_donation_points_select_all on public.blood_donation_points;
create policy blood_donation_points_select_all on public.blood_donation_points for select to anon, authenticated using (true);

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

grant execute on function public.blood_donation_points_nearby(double precision, double precision, double precision) to anon, authenticated;
