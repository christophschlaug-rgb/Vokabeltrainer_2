# Vokabeltrainer – Englisch B1 → C1

Android-App zum Aufbau von Englisch-Wortschatz auf den Niveaus B1 bis C1 mit Spaced Repetition.
Läuft auf Android 8.0+ (getestet auf Android 11).

## Features

- **5357 Vokabeln** sind fest eingebaut: 91 A2 (Aufwärmen), 763 B1, 1191 B2, 3312 C1
- Sofort offline einsatzbereit
- Konfigurierbares Tageslimit (25 / 50 / 75 / 100 / 150 fällige Karten)
- Spaced Repetition mit festen Intervallen (richtig: 1 / 3 / 10 / 30 / 90 / 180 Tage; falsch: morgen)
- Zufällige Abfragerichtung DE↔EN pro Karte
- Automatische Bewertung: Groß/Kleinschreibung egal, optionales "to" bei Verben, mehrere Übersetzungen erlaubt
- **Enter mit leerem Feld = "Lösung zeigen"** — du musst nichts eingeben, wenn du es nicht weißt
- **Units anlegen** für Vokabeln aus deinem Englischbuch — jede Unit kann separat oder gemischt mit dem Standard-Wörterbuch geübt werden
- **Vokabeln löschen** (Soft-Delete mit Papierkorb zum Wiederherstellen)
- **Inkrementelles Update**: "Wortschatz aktualisieren" lädt nur neue Vokabeln nach, dein Lernfortschritt bleibt erhalten
- Wörterbuch alphabetisch + Live-Suche über EN und DE
- Level-Übersicht: wie viele Karten in welchem SRS-Level (0-5)
- Keine sensiblen Berechtigungen (nur INTERNET), nur HTTPS, kein Cleartext

## So baust du die APK – mit GitHub Actions (empfohlen)

Du brauchst **kein Android Studio** und musst nichts lokal installieren.
Das gesamte Build läuft in der Cloud auf GitHub.

### 1. GitHub-Repository anlegen
1. Gehe zu https://github.com/new
2. Lege ein neues Repository an, z. B. `vokabeltrainer`
3. Wähle **Privat** wenn du es nicht öffentlich teilen willst (Build funktioniert trotzdem)

### 2. Code hochladen
Im einfachsten Fall:
1. Auf der GitHub-Seite deines neuen Repos: **uploading an existing file** klicken
2. Den entpackten Inhalt dieses ZIPs (alles aus dem `VokabelTrainer/`-Ordner)
   per Drag & Drop hochladen
3. Commit-Nachricht: "initial" → **Commit changes**

Alternativ über die Kommandozeile (falls du `git` installiert hast):
```bash
cd VokabelTrainer
git init
git add .
git commit -m "initial"
git branch -M main
git remote add origin https://github.com/DEIN-USERNAME/vokabeltrainer.git
git push -u origin main
```

### 3. Auf den Build warten
1. In deinem Repo: Tab **Actions** öffnen
2. Du siehst einen Workflow-Lauf "Build APK". Klicke ihn an.
3. Der Build dauert ca. 4–6 Minuten beim ersten Mal.
4. Wenn alles grün ist: scrolle nach unten zu **Artifacts**.
5. Lade die Datei `app-debug.zip` herunter, entpacke sie → `app-debug.apk`

### 4. APK aufs Handy kopieren und installieren
1. APK per USB / E-Mail / Cloud auf das Handy übertragen
2. Auf dem Handy: APK öffnen
3. Falls Android meckert "Installation aus unbekannten Quellen":
   **Einstellungen → Sicherheit → Unbekannte Quellen** für die nutzende App
   (z. B. Dateien-App) erlauben
4. Installieren, fertig

### Versionierte Releases (optional)
Wenn du eine versionierte APK haben willst, die dauerhaft auf GitHub liegt:
```bash
git tag v1.0.0
git push origin v1.0.0
```
Der Workflow erzeugt automatisch ein **Release** unter `Releases/` mit der
Datei `VokabelTrainer-v1.0.0.apk`.

### Wenn der Build nicht startet
Wenn unter dem **Actions**-Tab kein Workflow-Lauf erscheint:

1. **Actions ist deaktiviert.** Settings → Actions → General → "Allow all actions"
   auswählen. Bei privaten Repos auf neuen Konten ist das manchmal aus.
2. **Datei am falschen Ort.** Der Workflow muss exakt unter
   `.github/workflows/build.yml` liegen, alles kleingeschrieben, beide
   Verzeichnisse mit Punkt vorne. Wenn du das ZIP über die GitHub-Web-UI
   hochgeladen hast: Prüfe, ob der `.github`-Ordner (mit Punkt) auch
   wirklich dort liegt — manche Browser blenden Punkt-Ordner aus.
3. **Workflow-Lauf manuell starten.** Im Tab Actions → "Build APK" wählen →
   rechts "Run workflow" Button. Das funktioniert dank `workflow_dispatch:`
   im Workflow.

## Lokal bauen (optional, falls du Android Studio nutzt)

1. Android Studio installieren: https://developer.android.com/studio
2. **File → Open** → den entpackten Projekt-Ordner auswählen
3. Auf den Gradle-Sync warten (5–10 Min beim ersten Mal)
4. **Run** drücken → fertig
5. Die APK liegt anschließend unter `app/build/outputs/apk/debug/`

## Vokabelliste erweitern

Die Liste liegt in `app/src/main/assets/c1_vocab.json`. Du kannst sie:

**Variante A: direkt von Hand editieren.** Format:
```json
{
  "id": "ubiquitous",
  "en": "ubiquitous",
  "de": ["allgegenwärtig"],
  "pos": "adj",
  "level": "C1"
}
```
Verben bekommen "to " im `en`-Feld; das `id` enthält das Wort ohne "to".

**Variante B: mit dem mitgelieferten Skript.** Siehe `scripts/extend_vocab.py`.
Du brauchst dafür einen Wörterbuch-Dump (freedict eng-deu oder Wiktionary
über kaikki.org). Das Skript dokumentiert das Vorgehen ausführlich:

```bash
python3 scripts/extend_vocab.py --help
```

Nach Änderung: committen → GitHub Actions baut die neue APK automatisch.

## Sicherheits-Notizen (kurz)

- Manifest deklariert nur `INTERNET`. Keine Standortrechte, keine Kamera, keine Kontakte.
- `usesCleartextTraffic="false"` und `network_security_config.xml` erzwingen HTTPS.
- `allowBackup="false"`, `data_extraction_rules.xml` schließt Cloud-Backups aus.
- Kein WebView, kein dynamisches Code-Laden.
- Eingaben aus dem Netz werden serverseitig auf Steuerzeichen, Längen und
  Pflichtfelder gefiltert (`VocabRepository.sanitizeAndFilter`).

## Tests laufen lassen
Im Build-Workflow laufen sie automatisch. Lokal:
```bash
./gradlew test
```
Es sind Unit-Tests für den Grader (Verben, Mehrfach-Übersetzungen,
Groß/Klein) und die SRS-Engine (alle Level-Übergänge).

## Verzeichnisstruktur
```
VokabelTrainer/
├── .github/workflows/build.yml      ← CI-Build
├── app/
│   └── src/main/
│       ├── assets/c1_vocab.json     ← Die 1789 Vokabeln
│       ├── java/.../                ← Kotlin-Quelltexte
│       └── res/                     ← Layout, Themes, Icons
├── gradle/wrapper/                  ← Gradle Wrapper (JAR + Properties)
├── gradlew, gradlew.bat             ← Wrapper-Skripte
├── scripts/extend_vocab.py          ← Tool zum Erweitern der Liste
├── build.gradle.kts                 ← Root-Build
├── settings.gradle.kts
└── README.md                        ← Dieses Dokument
```

## Lizenz

Der Quelltext steht unter MIT.
Die mitgelieferte Vokabelliste ist eine eigene Kuration und steht unter
CC0-1.0 (gemeinfrei) – du darfst sie verwenden, ändern und weiterverteilen.
