#!/usr/bin/env python3
"""Yerelleştirme denetimi (CI'da çalışır).

- Kodda kullanılan her yerelleştirme anahtarı Localizable.xcstrings içinde olmalı
- Her anahtarın en (kaynak) ve tr çevirisi dolu olmalı
- Kullanılmayan anahtarlar uyarı olarak listelenir (hata değildir)

Anahtar sezgisi: "ad.alan" biçimli bir string, ilk parçası katalogdaki bir ad alanıysa (settings., profile. ...)
anahtar kabul edilir. Böylece SF Symbol adları ("heart.fill") ve bundle id'leri karıştırılmaz.
Yeni dil eklemek için REQUIRED_LANGUAGES listesini güncelleyin.
"""
import glob, json, re, sys

CATALOG = "donateblood/Resources/Localizable.xcstrings"
SOURCES = "donateblood/**/*.swift"
REQUIRED_LANGUAGES = ("en", "tr")

catalog = json.load(open(CATALOG, encoding="utf-8"))["strings"]
namespaces = {k.split(".")[0] for k in catalog if "." in k}
# Dinamik anahtar aileleri (kodda "health.q.\(key)" gibi üretilenler)
dynamic_prefixes = ("health.q.", "safety.reason.")

literal = re.compile(r'"([a-z][A-Za-z0-9]*(?:\.[A-Za-z0-9_]+)+)(?= \\\(| %|")')
# String(localized:) uygulama içi dil seçimini izlemez; koddan üretilen metinler L10n üzerinden gitmeli.
forbidden = re.compile(r'String\(localized:')
used = set()
forbidden_uses = []
for path in glob.glob(SOURCES, recursive=True):
    source = open(path, encoding="utf-8").read()
    # Erişilebilirlik tanımlayıcıları (UI testleri için) yerelleştirilmez.
    source = re.sub(r'\.accessibilityIdentifier\("[^"]*"\)', "", source)
    # SF Symbol adları ("map.fill") anahtar değildir.
    source = re.sub(r'(systemImage|systemName):\s*"[^"]*"', "", source)
    if forbidden.search(source) and not path.endswith(("L10n.swift", "LocalizationOverride.swift")):
        forbidden_uses.append(path)
    for m in literal.finditer(source):
        key = m.group(1)
        if key.split(".")[0] in namespaces:
            used.add(key)

def in_catalog(key):
    return any(k == key or k.split(" ")[0] == key for k in catalog)

errors = [f"String(localized:) kullanmayın, L10n.string/format kullanın: {p}" for p in forbidden_uses]
for key in sorted(used):
    if key.startswith(dynamic_prefixes) and key.endswith("."):
        continue
    if not in_catalog(key):
        errors.append(f"Katalogda yok: {key}")

def translated_values(localization):
    """Düz çeviri ya da çoğul (plural) biçimlerin tüm değerleri."""
    if "stringUnit" in localization:
        return [localization["stringUnit"].get("value", "")]
    plural = localization.get("variations", {}).get("plural", {})
    return [form.get("stringUnit", {}).get("value", "") for form in plural.values()]

for key, entry in catalog.items():
    locs = entry.get("localizations", {})
    for lang in REQUIRED_LANGUAGES:
        values = translated_values(locs.get(lang, {}))
        if not values or not all(v.strip() for v in values):
            errors.append(f"'{key}' için {lang} çevirisi eksik")

unused = sorted(k for k in catalog
                if k.split(" ")[0] not in used and not k.startswith(dynamic_prefixes)
                and not re.match(r"^(faq\.|health\.q\.)", k))
if unused:
    print(f"Uyarı: {len(unused)} kullanılmayan anahtar (ilk 10): {unused[:10]}")
if errors:
    print("\n".join(errors)); sys.exit(1)
print(f"Yerelleştirme tamam: {len(catalog)} anahtar, {len(REQUIRED_LANGUAGES)} dil.")
