-- Oturumsuz (anon) istemcilerin ilanları ve haritayı okuyabilmesi için.
-- schema.sql'i daha önce çalıştırdıysanız bunu çalıştırın.
grant select on public.blocks to anon;  -- RLS nedeniyle satır dönmez; blood_requests politikasının alt sorgusu için gerekli
grant execute on function public.hospitals_nearby(double precision, double precision, double precision) to anon, authenticated;
