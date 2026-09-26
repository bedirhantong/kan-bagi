-- =====================================================================
-- İLAN SINIRLARI (spam / bildirim yağmuru koruması)
--
-- Uygulama kullanıcısı başına:
--   - aynı anda en fazla 3 aktif ilan (kapatılan ilanı yeniden açmak da sayılır),
--   - son 24 saatte en fazla 5 yeni ilan.
-- Sınırlar yalnızca uygulamadan gelen isteklere uygulanır; SQL Editor / service role (auth.uid() boş) muaftır.
-- Sayılar uygulamadaki BloodRequestLimits ile aynı olmalı.
--
-- schema.sql'i bu dosyadan önce çalıştırdıysanız bunu çalıştırın (tekrar çalıştırılabilir).
-- =====================================================================

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

drop trigger if exists blood_requests_limits on public.blood_requests;
create trigger blood_requests_limits before insert or update of is_active on public.blood_requests
    for each row execute function public.enforce_request_limits();

create index if not exists blood_requests_owner_created_idx on public.blood_requests (owner_id, created_at desc);
