-- Bildirim tercihleri: tercih edilen kan grupları ve hastaneler.
-- schema.sql'i daha önce çalıştırdıysanız bunu çalıştırın.
alter table public.notification_preferences
    add column if not exists preferred_blood_types text[] not null default '{}',
    add column if not exists preferred_hospital_ids integer[] not null default '{}';
grant update (preferred_blood_types, preferred_hospital_ids) on public.notification_preferences to authenticated;

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
       and np.blood_type_alerts
       -- kan grubu: seçim varsa seçime göre, yoksa profildeki gruba uyumluluğa göre
       and (case when cardinality(np.preferred_blood_types) > 0
                 then new.blood_type = any (np.preferred_blood_types)
                 else p.blood_type = any (public.compatible_donor_types(new.blood_type)) end)
       -- hastane: seçim varsa yalnızca seçilenler
       and (cardinality(np.preferred_hospital_ids) = 0 or new.hospital_id = any (np.preferred_hospital_ids))
       and not exists (select 1 from public.blocks b
                        where b.blocker_id = p.id and b.blocked_id = new.owner_id);
    return new;
end $$;
