-- Zaten schema.sql'i çalıştırdıysanız BUNU çalıştırın ("Acil Durum" alanı).
alter table public.blood_requests add column if not exists is_emergency boolean not null default false;
grant insert (is_emergency) on public.blood_requests to authenticated;
grant update (is_emergency) on public.blood_requests to authenticated;

drop function if exists public.create_blood_request(text, integer, text, text, text, integer, text[]);
drop function if exists public.update_blood_request(uuid, text, integer, text, text, text, integer, text[]);

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

revoke all on function public.create_blood_request(text, integer, text, text, text, integer, text[], boolean) from public, anon;
revoke all on function public.update_blood_request(uuid, text, integer, text, text, text, integer, text[], boolean) from public, anon;
grant execute on function public.create_blood_request(text, integer, text, text, text, integer, text[], boolean) to authenticated;
grant execute on function public.update_blood_request(uuid, text, integer, text, text, text, integer, text[], boolean) to authenticated;

create or replace function public.notify_on_new_request()
returns trigger language plpgsql security definer set search_path = public as $$
begin
    insert into public.notifications (user_id, type, title, body, data)
    select p.id, 'BLOOD_REQUEST',
           case when new.is_emergency then 'ACİL: ' else '' end || new.blood_type || ' kan grubu ihtiyacı',
           new.title,
           jsonb_build_object('request_id', new.id)
      from public.profiles p
      join public.notification_preferences np on np.user_id = p.id
     where p.id <> new.owner_id
       and p.user_type = 'REGULAR_USER'
       and p.blood_type = any (public.compatible_donor_types(new.blood_type))
       and np.blood_type_alerts
       and not exists (select 1 from public.blocks b where b.blocker_id = p.id and b.blocked_id = new.owner_id);
    return new;
end $$;
