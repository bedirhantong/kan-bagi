#!/usr/bin/env python3
"""Android uygulamasındaki Kızılay kan bağış noktaları verisinden SQL üretir.

Kaynak : donate-blood-android/app/src/main/assets/kizilay_blood_points.json
         (iç içe liste; her satır bir günlük ekip/nokta kaydıdır, aynı nokta birçok günde tekrar eder)
Çıktı  : supabase/11_blood_donation_points_data.sql  (tekrar çalıştırılabilir: on conflict do nothing)

Kullanım (repo kökünden):
    python3 supabase/tools/import_kizilay_points.py

Kurallar (Android'deki gibi): koordinata göre tekilleştirilir; koordinatı olmayan kayıtlar atlanır.
Ek olarak: bir nokta herhangi bir kayıtta gezici ekip (geziciEkipSebebi dolu) ise MOBILE, değilse FIXED sayılır.
"""
import json
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "donate-blood-android/app/src/main/assets/kizilay_blood_points.json"
OUTPUT = ROOT / "supabase/11_blood_donation_points_data.sql"

# Plaka kodu -> il adı (ilId).
PROVINCES = {
    1: "Adana", 2: "Adıyaman", 3: "Afyonkarahisar", 4: "Ağrı", 5: "Amasya", 6: "Ankara", 7: "Antalya", 8: "Artvin",
    9: "Aydın", 10: "Balıkesir", 11: "Bilecik", 12: "Bingöl", 13: "Bitlis", 14: "Bolu", 15: "Burdur", 16: "Bursa",
    17: "Çanakkale", 18: "Çankırı", 19: "Çorum", 20: "Denizli", 21: "Diyarbakır", 22: "Edirne", 23: "Elazığ",
    24: "Erzincan", 25: "Erzurum", 26: "Eskişehir", 27: "Gaziantep", 28: "Giresun", 29: "Gümüşhane", 30: "Hakkari",
    31: "Hatay", 32: "Isparta", 33: "Mersin", 34: "İstanbul", 35: "İzmir", 36: "Kars", 37: "Kastamonu", 38: "Kayseri",
    39: "Kırklareli", 40: "Kırşehir", 41: "Kocaeli", 42: "Konya", 43: "Kütahya", 44: "Malatya", 45: "Manisa",
    46: "Kahramanmaraş", 47: "Mardin", 48: "Muğla", 49: "Muş", 50: "Nevşehir", 51: "Niğde", 52: "Ordu", 53: "Rize",
    54: "Sakarya", 55: "Samsun", 56: "Siirt", 57: "Sinop", 58: "Sivas", 59: "Tekirdağ", 60: "Tokat", 61: "Trabzon",
    62: "Tunceli", 63: "Şanlıurfa", 64: "Uşak", 65: "Van", 66: "Yozgat", 67: "Zonguldak", 68: "Aksaray", 69: "Bayburt",
    70: "Karaman", 71: "Kırıkkale", 72: "Batman", 73: "Şırnak", 74: "Bartın", 75: "Ardahan", 76: "Iğdır",
    77: "Yalova", 78: "Karabük", 79: "Kilis", 80: "Osmaniye", 81: "Düzce",
}

KEEP_UPPER = {"KBM", "KAB", "ADÜ", "AVM", "PTT", "SGK", "TC", "İÖO", "MYO", "TEM", "TOKİ", "AŞ", "A.Ş."}


def tr_lower(text: str) -> str:
    return text.replace("I", "ı").replace("İ", "i").lower()


def tr_upper_first(text: str) -> str:
    if not text:
        return text
    first = {"i": "İ", "ı": "I"}.get(text[0], text[0].upper())
    return first + text[1:]


def title_tr(text: str) -> str:
    """'ADANA ŞEHİR HASTANESİ' -> 'Adana Şehir Hastanesi' (Türkçe i/ı kurallarıyla, kısaltmalar korunur).

    Yalnızca TAMAMI büyük harf olan metinler dönüştürülür; zaten karışık yazılmış metne dokunulmaz.
    Sınırlama: büyük harfli kaynakta 'I' hem 'ı' (KIZILAY) hem 'i' (MITHAT) olabilir; ikisi ayırt edilemediği için
    nadiren 'Mıthat' gibi kozmetik hatalar çıkabilir. Düzeltmek için 12_... ile verinin elle düzeltilmesi yeterlidir.
    """
    text = " ".join(text.split())
    if not text.isupper():
        return text
    words = []
    for word in text.split(" "):
        words.append(word if word in KEEP_UPPER else tr_upper_first(tr_lower(word)))
    return " ".join(words)


def format_phone(raw: str | None) -> str | None:
    digits = "".join(ch for ch in (raw or "") if ch.isdigit())
    if len(digits) == 11 and digits.startswith("0"):
        digits = digits[1:]
    if len(digits) != 10:
        return None
    return f"+90 {digits[:3]} {digits[3:6]} {digits[6:8]} {digits[8:]}"


def q(value) -> str:
    if value is None or value == "":
        return "null"
    return "'" + str(value).replace("'", "''") + "'"


def main() -> int:
    groups = json.loads(SOURCE.read_text(encoding="utf-8"))
    rows = [row for group in groups for row in group]
    skipped = [r for r in rows if not r.get("koordinatLatitude") or not r.get("koordinatLongitude")]

    points: dict[tuple[float, float], dict] = {}
    mobile: defaultdict[tuple[float, float], bool] = defaultdict(bool)
    for row in rows:
        if row in skipped:
            continue
        key = (round(float(row["koordinatLatitude"]), 6), round(float(row["koordinatLongitude"]), 6))
        mobile[key] = mobile[key] or bool(row.get("geziciEkipSebebi"))
        points.setdefault(key, row)  # ilk kayıt (Android'deki distinctBy ile aynı)

    unknown = sorted({r["ilId"] for r in points.values()} - set(PROVINCES))
    if unknown:
        print(f"HATA: bilinmeyen il kodları: {unknown}", file=sys.stderr)
        return 1

    values = []
    for (lat, lon), row in sorted(points.items(), key=lambda kv: (kv[1]["ilId"], kv[1]["ekipAdi"], kv[0])):
        province = PROVINCES[row["ilId"]]
        values.append(
            "  ({external}, {name}, {address}, {neighborhood}, {district}, {code}, {province}, {lat}, {lon}, {phone}, {kind})".format(
                external=q(row["ekipID"]), name=q(title_tr(row["ekipAdi"])), address=q(title_tr(row["adres"]) if row.get("adres") else None),
                neighborhood=q(title_tr(row["mahalle"]) if row.get("mahalle") else None),
                district=q(title_tr(row["ilceAd"]) if row.get("ilceAd") else None),
                code=row["ilId"], province=q(province), lat=lat, lon=lon, phone=q(format_phone(row.get("telefon"))),
                kind=q("MOBILE" if mobile[(lat, lon)] else "FIXED")))

    body = ",\n".join(values)
    fixed = sum(1 for k in points if not mobile[k])
    header = (
        "-- Kızılay kan bağış noktaları (otomatik üretildi; elle düzenlemeyin).\n"
        "-- Üreten: supabase/tools/import_kizilay_points.py  |  Kaynak: donate-blood-android/.../assets/kizilay_blood_points.json\n"
        f"-- {len(rows)} ekip kaydı -> {len(points)} benzersiz nokta ({fixed} sabit, {len(points) - fixed} gezici); "
        f"{len(skipped)} kayıt koordinatsız olduğu için atlandı.\n"
        "-- NOT: Veri Mayıs 2025 haftasına ait bir anlık görüntüdür ve 24 ili kapsar; gezici ekiplerin yerleri/tarihleri değişir.\n"
        "-- Önkoşul: schema.sql (veya 10_blood_donation_points_patch.sql). Tekrar çalıştırılabilir.\n"
    )
    statement = (
        "insert into public.blood_donation_points\n"
        "  (external_id, name, address, neighborhood, district, province_code, province_name, lat, lon, phone_number, kind)\n"
        "values\n" + body + "\non conflict (lat, lon) do nothing;\n"
    )
    OUTPUT.write_text(header + statement, encoding="utf-8")
    print(f"{len(rows)} kayıt -> {len(points)} nokta ({fixed} sabit, {len(points) - fixed} gezici); atlanan koordinatsız: {[r['ekipAdi'] for r in skipped]}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
