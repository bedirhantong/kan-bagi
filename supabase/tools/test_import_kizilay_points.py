"""import_kizilay_points.py için birim testleri:  python3 -m unittest discover -s supabase/tools -v"""
import importlib.util
import re
import unittest
from pathlib import Path

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location("importer", HERE / "import_kizilay_points.py")
importer = importlib.util.module_from_spec(spec)
spec.loader.exec_module(importer)


class TitleCaseTests(unittest.TestCase):
    def test_turkish_dotted_and_dotless_i(self):
        self.assertEqual(importer.title_tr("ADANA ŞEHİR HASTANESİ"), "Adana Şehir Hastanesi")
        self.assertEqual(importer.title_tr("KIZILAY İSTANBUL"), "Kızılay İstanbul")

    def test_already_mixed_case_is_untouched(self):
        self.assertEqual(importer.title_tr("Kızılay Caddesi 28a, 01960"), "Kızılay Caddesi 28a, 01960")

    def test_acronyms_are_kept_and_spaces_collapsed(self):
        self.assertEqual(importer.title_tr("ADÜ   FEN  FAKÜLTESİ"), "ADÜ Fen Fakültesi")


class PhoneTests(unittest.TestCase):
    def test_formats_ten_digits(self):
        self.assertEqual(importer.format_phone("3224546131"), "+90 322 454 61 31")
        self.assertEqual(importer.format_phone("03224546131"), "+90 322 454 61 31")

    def test_rejects_invalid(self):
        self.assertIsNone(importer.format_phone("123"))
        self.assertIsNone(importer.format_phone(None))
        self.assertIsNone(importer.format_phone(""))


class ProvinceTests(unittest.TestCase):
    def test_table_is_complete_and_known_codes_are_correct(self):
        self.assertEqual(sorted(importer.PROVINCES), list(range(1, 82)))
        self.assertEqual(importer.PROVINCES[6], "Ankara")
        self.assertEqual(importer.PROVINCES[7], "Antalya")
        self.assertEqual(importer.PROVINCES[34], "İstanbul")
        self.assertEqual(importer.PROVINCES[35], "İzmir")


class GeneratedSqlTests(unittest.TestCase):
    """Üretilen dosya yerinde durur: tekrar üretilince farklı çıkmamalı ve güvenli olmalı."""

    def setUp(self):
        self.sql = importer.OUTPUT.read_text(encoding="utf-8")

    def test_is_idempotent_and_single_statement(self):
        self.assertIn("on conflict (lat, lon) do nothing", self.sql)
        self.assertEqual(len(re.findall(r"^insert into", self.sql, re.M)), 1)

    def test_has_expected_row_count_and_kinds(self):
        rows = re.findall(r"^  \('", self.sql, re.M)
        self.assertEqual(len(rows), 163)
        self.assertIn("'FIXED')", self.sql)
        self.assertIn("'MOBILE')", self.sql)

    def test_quotes_are_escaped(self):
        self.assertEqual(importer.q("O'Brien"), "'O''Brien'")
        self.assertEqual(importer.q(None), "null")
        self.assertEqual(importer.q(""), "null")


if __name__ == "__main__":
    unittest.main()
