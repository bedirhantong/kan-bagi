-- Haritada en fazla 300 hastane döndür (bellek ve çizim maliyeti için).
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
grant execute on function public.hospitals_nearby(double precision, double precision, double precision) to anon, authenticated;
