import { assertEquals } from "jsr:@std/assert@1";
import {
  buildRequest,
  classifyResponse,
  externalId,
  type NotificationRecord,
  type OneSignalRequest,
  ONESIGNAL_URL,
  sendNotification,
} from "./onesignal.ts";

const record: NotificationRecord = {
  user_id: "ABCDEF12-0000-4000-8000-000000000001",
  type: "BLOOD_REQUEST",
  title: "ACİL: A+ kan grubu ihtiyacı",
  body: "Acil kan",
  data: { request_id: "r-1" },
};

Deno.test("external id küçük harfe çevrilir (istemciyle aynı biçim)", () => {
  assertEquals(externalId("ABCDEF12-0000-4000-8000-000000000001"), "abcdef12-0000-4000-8000-000000000001");
});

Deno.test("istek: external_id ile hedefleme, başlık/içerik, data ve rozet", () => {
  const req = buildRequest("app-1", record, 4);
  assertEquals(req.app_id, "app-1");
  assertEquals(req.target_channel, "push");
  assertEquals(req.include_aliases, { external_id: ["abcdef12-0000-4000-8000-000000000001"] });
  assertEquals(req.headings, { en: record.title });
  assertEquals(req.contents, { en: record.body });
  assertEquals(req.data, { request_id: "r-1", type: "BLOOD_REQUEST" });
  assertEquals([req.ios_badgeType, req.ios_badgeCount], ["SetTo", 4]);
});

Deno.test("istek: rozet yoksa ios_badge alanları eklenmez, data yoksa yalnızca type", () => {
  const req = buildRequest("app-1", { ...record, data: null }, undefined);
  assertEquals("ios_badgeType" in req, false);
  assertEquals(req.data, { type: "BLOOD_REQUEST" });
});

Deno.test("yanıt sınıflandırma", () => {
  assertEquals(classifyResponse(200, { id: "abc" }), "sent");
  // Alıcı yok: 200 ama id boş ve errors dolu
  assertEquals(classifyResponse(200, { id: "", errors: ["All included players are not subscribed"] }), "no_recipient");
  assertEquals(classifyResponse(200, { errors: { invalid_aliases: { external_id: ["x"] } } }), "no_recipient");
  assertEquals(classifyResponse(400, { errors: ["All included players are not subscribed"] }), "no_recipient");
  assertEquals(classifyResponse(429, null), "retryable");
  assertEquals(classifyResponse(503, null), "retryable");
  assertEquals(classifyResponse(401, { errors: ["Invalid API key"] }), "failed");
  assertEquals(classifyResponse(400, { errors: ["app_id is invalid"] }), "failed");
});

Deno.test("gönderim: URL, Key yetkilendirmesi ve gövde", async () => {
  let seen: { url: string; auth: string | null; body: OneSignalRequest } | null = null;
  const fetchFn = (url: string | URL | Request, init?: RequestInit) => {
    seen = {
      url: String(url),
      auth: new Headers(init?.headers).get("authorization"),
      body: JSON.parse(String(init?.body)),
    };
    return Promise.resolve(Response.json({ id: "notif-1" }));
  };
  const result = await sendNotification("app-1", "secret-key", record, 2, fetchFn as typeof fetch);
  assertEquals(result, "sent");
  assertEquals(seen!.url, ONESIGNAL_URL);
  assertEquals(seen!.auth, "Key secret-key");
  assertEquals(seen!.body.app_id, "app-1");
});

Deno.test("gönderim: alıcı yok ve sunucu hatası", async () => {
  const noRecipient = () => Promise.resolve(Response.json({ id: "", errors: ["All included players are not subscribed"] }));
  assertEquals(await sendNotification("a", "k", record, undefined, noRecipient as typeof fetch), "no_recipient");
  const boom = () => Promise.resolve(new Response("oops", { status: 500 }));
  assertEquals(await sendNotification("a", "k", record, undefined, boom as typeof fetch), "retryable");
});
