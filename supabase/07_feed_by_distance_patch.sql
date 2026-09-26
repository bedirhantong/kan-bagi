-- Ana sayfa akışı: tüm kan grupları, kullanıcıya en yakın hastaneden en uzağa; aynı yerde en yeni önce.
-- Konum (user_lat/user_lon) verilmezse yalnızca en yeniden eskiye sıralanır.
-- schema.sql'i daha önce çalıştırdıysanız bunu çalıştırın.
--
-- Sıralama kuralını değiştirmek isterseniz yalnızca aşağıdaki ORDER BY'ı düzenleyin
-- (ör. önce tarih, sonra mesafe için iki ifadenin yerini değiştirin).
-- security invoker: blood_requests RLS'i (aktif ilanlar, engellenen kullanıcılar) çağıran için geçerlidir.
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
