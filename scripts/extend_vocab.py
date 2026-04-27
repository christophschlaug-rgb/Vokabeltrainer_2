#!/usr/bin/env python3
"""
Vokabel-Erweiterungs-Skript
============================

Zweck
-----
Erweitert die mitgelieferte Liste `app/src/main/assets/c1_vocab.json` um
weitere englische Wörter. Übersetzungen werden aus einem von dir
heruntergeladenen Wörterbuch-Dump bezogen, NICHT aus dem Netz.

Warum lokal?
------------
Die App ist offline-first. Erweiterungen werden in das Asset eingepflegt
und mit dem nächsten APK-Build mitgebaut. Du committest die geänderte
JSON, GitHub Actions baut, fertig.

Unterstützte Eingabe-Wörterbücher
---------------------------------
1. **freedict eng-deu** (TEI-XML, GPL-Lizenz)
   Download (außerhalb der Sandbox):
   https://download.freedict.org/dictionaries/eng-deu/
   Datei: `eng-deu.tei` (entpackt aus dem .tar.xz)

2. **kaikki.org Wiktionary-Export** (JSON Lines, CC BY-SA 3.0)
   Download:
   https://kaikki.org/dictionary/English/words.json
   (oder die deutsche Variante mit englischen glosses)

Beide Formate sind unten implementiert. Du wählst per CLI-Option.

Beispiel
--------
    # 1) Wortliste vorbereiten - eine pro Zeile
    cat > new_words.txt <<EOF
    ubiquitous
    quintessential
    juxtaposition
    EOF

    # 2) Skript laufen lassen
    python3 extend_vocab.py \
        --input  app/src/main/assets/c1_vocab.json \
        --output app/src/main/assets/c1_vocab.json \
        --dict   ~/Downloads/eng-deu.tei \
        --format freedict \
        --words  new_words.txt

    # Die JSON wird in-place aktualisiert. Anschließend:
    git add app/src/main/assets/c1_vocab.json
    git commit -m "vocab: +3 Einträge"
    git push   # GitHub Actions baut das neue APK
"""

import argparse
import json
import re
import sys
from datetime import date
from pathlib import Path
from typing import Iterable

# ---------- Hilfsfunktionen ----------

def make_id(en: str) -> str:
    s = en.lower().strip()
    if s.startswith("to "):
        s = s[3:]
    s = re.sub(r"[^a-z0-9]+", "_", s).strip("_")
    return s


def is_verb_form(en: str) -> bool:
    return en.lower().startswith("to ")


# ---------- Parser für freedict TEI-XML ----------

def parse_freedict_tei(path: Path) -> dict[str, dict]:
    """
    Liest freedict eng-deu.tei und gibt eine Map zurück: en_lemma -> {pos, translations}.
    Bewusst ohne externe Libs (kein lxml) - reines Streaming via stdlib.
    """
    import xml.etree.ElementTree as ET
    ns = {"tei": "http://www.tei-c.org/ns/1.0"}
    result = {}
    print(f"[freedict] parse {path} ...", file=sys.stderr)
    for event, elem in ET.iterparse(path, events=("end",)):
        tag = elem.tag.split("}")[-1]
        if tag != "entry":
            continue
        # Headword
        orth = elem.find(".//tei:form/tei:orth", ns)
        if orth is None or not orth.text:
            elem.clear()
            continue
        en = orth.text.strip().lower()

        # Wortart
        pos_el = elem.find(".//tei:gramGrp/tei:pos", ns)
        pos_raw = (pos_el.text.strip().lower() if pos_el is not None and pos_el.text else "")
        pos = _normalize_pos(pos_raw)

        # Übersetzungen: alle <quote> in <cit type="trans">
        trans = []
        for cit in elem.findall(".//tei:cit", ns):
            if cit.get("type") != "trans":
                continue
            q = cit.find("tei:quote", ns)
            if q is not None and q.text:
                t = q.text.strip()
                if t and t not in trans:
                    trans.append(t)

        if trans:
            result[en] = {"pos": pos, "translations": trans}
        elem.clear()
    print(f"[freedict] {len(result)} Lemmata gelesen.", file=sys.stderr)
    return result


# ---------- Parser für kaikki.org JSON Lines ----------

def parse_kaikki_jsonl(path: Path) -> dict[str, dict]:
    """
    Liest kaikki.org-Wiktionary-Export (eine JSON pro Zeile, ein Wort pro Zeile).
    Vergleichsweise üppig - nimmt nur Einträge mit deutschen Übersetzungen.
    """
    print(f"[kaikki] parse {path} ...", file=sys.stderr)
    result = {}
    count = 0
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                continue
            count += 1
            word = obj.get("word", "").strip().lower()
            if not word:
                continue
            pos = _normalize_pos(obj.get("pos", ""))
            translations = obj.get("translations", [])
            de_trans = []
            for t in translations:
                if t.get("lang") == "German" or t.get("code") == "de":
                    word_t = t.get("word", "").strip()
                    if word_t and word_t not in de_trans:
                        de_trans.append(word_t)
            if de_trans:
                # mehrere Einträge je Wort möglich (mehrere POS) - hier nehmen wir den ersten Treffer
                if word not in result:
                    result[word] = {"pos": pos, "translations": de_trans}
                else:
                    # POS unterschiedlich? eigenen Eintrag mit POS-Suffix anlegen
                    key = f"{word}__{pos}"
                    result[key] = {"pos": pos, "translations": de_trans}
    print(f"[kaikki] {count} Zeilen gelesen, {len(result)} mit deutschen Übersetzungen.", file=sys.stderr)
    return result


def _normalize_pos(raw: str) -> str:
    raw = raw.lower().strip()
    if raw in {"v", "verb", "vt", "vi"}:
        return "verb"
    if raw in {"n", "noun", "subst"}:
        return "noun"
    if raw in {"adj", "adjective"}:
        return "adj"
    if raw in {"adv", "adverb"}:
        return "adv"
    return raw or "unknown"


# ---------- Hauptlogik ----------

def main():
    parser = argparse.ArgumentParser(description="Erweitert c1_vocab.json um neue Wörter.")
    parser.add_argument("--input", required=True, help="Pfad zur bestehenden c1_vocab.json")
    parser.add_argument("--output", required=True, help="Pfad zur Ausgabe (i.d.R. gleiche Datei)")
    parser.add_argument("--dict", required=True, help="Pfad zum Wörterbuch-Dump")
    parser.add_argument("--format", choices=["freedict", "kaikki"], required=True,
                        help="Format des Wörterbuchs")
    parser.add_argument("--words", required=True, help="Textdatei: ein englisches Wort pro Zeile")
    parser.add_argument("--max-translations", type=int, default=3,
                        help="Wieviele deutsche Übersetzungen pro Wort maximal aufnehmen")
    args = parser.parse_args()

    in_path = Path(args.input)
    out_path = Path(args.output)
    dict_path = Path(args.dict)
    words_path = Path(args.words)

    if not in_path.exists():
        sys.exit(f"FEHLER: Eingabe nicht gefunden: {in_path}")
    if not dict_path.exists():
        sys.exit(f"FEHLER: Wörterbuch-Dump nicht gefunden: {dict_path}")
    if not words_path.exists():
        sys.exit(f"FEHLER: Wortliste nicht gefunden: {words_path}")

    # Bestehende Liste laden
    with open(in_path, "r", encoding="utf-8") as f:
        existing = json.load(f)
    existing_ids = {w["id"] for w in existing.get("words", [])}
    print(f"Bestand: {len(existing_ids)} Einträge.")

    # Wortliste lesen
    new_words = []
    with open(words_path, "r", encoding="utf-8") as f:
        for line in f:
            w = line.strip().lower()
            if w and not w.startswith("#"):
                new_words.append(w)
    print(f"Anzufragende Wörter: {len(new_words)}")

    # Wörterbuch laden
    if args.format == "freedict":
        dict_map = parse_freedict_tei(dict_path)
    else:
        dict_map = parse_kaikki_jsonl(dict_path)

    # Anfragen + Hinzufügen
    added = 0
    skipped_known = 0
    skipped_no_match = 0
    failures = []

    for w in new_words:
        # Verben mit "to "-Prefix tolerieren
        lookup_key = w[3:] if w.startswith("to ") else w
        match = dict_map.get(lookup_key)
        if not match:
            skipped_no_match += 1
            failures.append(w)
            continue

        pos = match["pos"]
        translations = match["translations"][:args.max_translations]
        # Verben kriegen "to" vorne dran
        en_form = f"to {lookup_key}" if pos == "verb" and not lookup_key.startswith("to ") else lookup_key
        wid = make_id(en_form)
        if wid in existing_ids:
            skipped_known += 1
            continue

        existing["words"].append({
            "id": wid,
            "en": en_form,
            "de": translations,
            "pos": pos,
            "level": "C1",
        })
        existing_ids.add(wid)
        added += 1

    # Sortieren
    existing["words"].sort(key=lambda e: (e["pos"], e["id"]))
    # Versions-Stempel aktualisieren
    existing["version"] = f"c1-curated-{date.today().isoformat()}"

    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(existing, f, ensure_ascii=False, indent=2)

    print(f"\n--- Bilanz ---")
    print(f"Hinzugefügt:           {added}")
    print(f"Bereits bekannt:       {skipped_known}")
    print(f"Kein Wörterbuch-Treffer: {skipped_no_match}")
    if failures:
        print(f"Nicht gefunden: {failures[:20]}{'...' if len(failures) > 20 else ''}")
    print(f"Neue Gesamtgröße:      {len(existing['words'])}")
    print(f"\n[OK] geschrieben: {out_path}")


if __name__ == "__main__":
    main()
