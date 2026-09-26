// notifications tablosuna INSERT olunca (Database Webhook) kullanıcının cihazlarına OneSignal ile push gönderir.
// Kullanıcı, uygulamada Supabase kullanıcı id'siyle OneSignal'e bağlanır (external_id); bu yüzden cihaz jetonu saklanmaz.
//
// Gerekli secret'lar:
//   ONESIGNAL_APP_ID        OneSignal > Settings > Keys & IDs
//   ONESIGNAL_REST_API_KEY  OneSignal > Settings > Keys & IDs (gizli!)
//   WEBHOOK_SECRET          Database Webhook'un `x-webhook-secret` başlığıyla göndereceği paylaşılan sır
// (SUPABASE_URL ve SUPABASE_SERVICE_ROLE_KEY Edge Function ortamında otomatik gelir.)
import { createClient } from "npm:@supabase/supabase-js@2";
import { type NotificationRecord, sendNotification } from "./onesignal.ts";

function timingSafeEqual(a: string, b: string): boolean {
  const enc = new TextEncoder();
  const x = enc.encode(a), y = enc.encode(b);
  if (x.length !== y.length) return false;
  let diff = 0;
  for (let i = 0; i < x.length; i++) diff |= x[i] ^ y[i];
  return diff === 0;
}

Deno.serve(async (req) => {
  const secret = Deno.env.get("WEBHOOK_SECRET");
  const provided = req.headers.get("x-webhook-secret") ?? "";
  if (!secret || !timingSafeEqual(provided, secret)) return new Response("unauthorized", { status: 401 });

  const payload = await req.json().catch(() => null);
  const record = payload?.record as NotificationRecord | undefined;
  if (!record?.user_id) return new Response("no record", { status: 400 });

  const appId = Deno.env.get("ONESIGNAL_APP_ID");
  const apiKey = Deno.env.get("ONESIGNAL_REST_API_KEY");
  if (!appId || !apiKey) return new Response("OneSignal secret'ları tanımlı değil", { status: 500 });

  const supabase = createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

  const { data: prefs } = await supabase
    .from("notification_preferences").select("push_enabled").eq("user_id", record.user_id).maybeSingle();
  if (prefs && !prefs.push_enabled) return Response.json({ skipped: "push_disabled" });

  // Uygulama simgesindeki rozet: okunmamış bildirim sayısı.
  const { count } = await supabase.from("notifications")
    .select("id", { count: "exact", head: true }).eq("user_id", record.user_id).eq("is_read", false);

  const result = await sendNotification(appId, apiKey, record, count ?? undefined);
  // Geçici hata: 503 dön, webhook yeniden denesin.
  if (result === "retryable") return Response.json({ result }, { status: 503 });
  return Response.json({ result }, { status: result === "failed" ? 502 : 200 });
});
