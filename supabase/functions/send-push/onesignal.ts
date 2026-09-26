// OneSignal REST API istemcisi (saf fonksiyonlar + enjekte edilebilir fetch: Deno testleriyle doğrulanır).
// Cihaz jetonu tutulmaz: kullanıcı, uygulamada `OneSignal.login(<supabase user id>)` ile bağlanır ve
// bildirim `external_id` ile hedeflenir.

/** `notifications` tablosundaki satır (Database Webhook `record`). */
export interface NotificationRecord {
  user_id: string;
  type: string;
  title: string;
  body: string;
  data?: Record<string, unknown> | null;
}

export type FetchFn = typeof fetch;
/** sent: gönderildi · no_recipient: kullanıcının abone cihazı yok (hata değil) · retryable: geçici · failed: kalıcı hata */
export type SendResult = "sent" | "no_recipient" | "retryable" | "failed";

export const ONESIGNAL_URL = "https://api.onesignal.com/notifications?c=push";

export interface OneSignalRequest {
  app_id: string;
  target_channel: "push";
  include_aliases: { external_id: string[] };
  headings: Record<string, string>;
  contents: Record<string, string>;
  data: Record<string, unknown>;
  ios_badgeType?: "SetTo";
  ios_badgeCount?: number;
}

/** Supabase kullanıcı id'leri istemcide küçük harfle bağlanır; burada da aynı biçim kullanılır. */
export function externalId(userId: string): string {
  return userId.toLowerCase();
}

export function buildRequest(appId: string, record: NotificationRecord, badge?: number): OneSignalRequest {
  return {
    app_id: appId,
    target_channel: "push",
    include_aliases: { external_id: [externalId(record.user_id)] },
    headings: { en: record.title },
    contents: { en: record.body },
    // İstemci derin bağlantı için request_id / room_id okur (additionalData).
    data: { ...(record.data ?? {}), type: record.type },
    ...(badge !== undefined ? { ios_badgeType: "SetTo" as const, ios_badgeCount: badge } : {}),
  };
}

/** OneSignal yanıtını sınıflandırır. 200 dönse bile alıcı yoksa `errors` dolu gelir. */
export function classifyResponse(status: number, body: unknown): SendResult {
  const json = body as { id?: string; errors?: unknown } | null;
  if (status >= 200 && status < 300) {
    const hasId = typeof json?.id === "string" && json.id.length > 0;
    if (hasId) return "sent";
    return json?.errors ? "no_recipient" : "sent";
  }
  if (status === 429 || status >= 500) return "retryable";
  // 400: "All included players are not subscribed" gibi alıcı yok durumu
  if (status === 400 && JSON.stringify(json?.errors ?? "").toLowerCase().match(/subscri|not found|invalid_aliases/)) {
    return "no_recipient";
  }
  return "failed";
}

export async function sendNotification(
  appId: string,
  restApiKey: string,
  record: NotificationRecord,
  badge: number | undefined,
  fetchFn: FetchFn = fetch,
): Promise<SendResult> {
  const res = await fetchFn(ONESIGNAL_URL, {
    method: "POST",
    headers: { authorization: `Key ${restApiKey}`, "content-type": "application/json" },
    body: JSON.stringify(buildRequest(appId, record, badge)),
  });
  let body: unknown = null;
  try {
    body = await res.json();
  } catch { /* gövde JSON değil */ }
  return classifyResponse(res.status, body);
}
