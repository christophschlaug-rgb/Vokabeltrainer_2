# 🚀 EIN-KLICK-SETUP

Das Projekt ist so aufgebaut, dass du nur **einen einzigen kleinen Schritt**
manuell machen musst. Den Rest erledigt ein Bootstrap-Workflow automatisch.

## Schritt 1: ZIP entpacken und auf GitHub hochladen

1. Du hast `VokabelTrainer.zip` entpackt — dieser Ordner hier.
2. Lege ein neues GitHub-Repository an (z. B. "vokabeltrainer").
   Wichtig: ohne README, ohne .gitignore, ohne License — komplett leer.
3. **Markiere alle Dateien und Ordner aus diesem Verzeichnis** und ziehe sie
   per Drag & Drop in den leeren Repo-Bereich auf GitHub. Punkt-Pfade gibt es
   hier keine, alles wird übernommen.
4. Unten **Commit changes** klicken.

Du solltest jetzt im Repo unter anderem die Datei `_payload.zip` sehen
(klein, ca. 1 KB). Die wird gleich automatisch verarbeitet.

## Schritt 2: Bootstrap-Workflow anlegen (das ist der einzige manuelle Schritt)

1. Im Repo: oben **Add file** → **Create new file** klicken.
2. Im Dateinamen-Feld diesen Pfad **genau so** eintippen:

   ```
   .github/workflows/bootstrap.yml
   ```

   Sobald du `/` tippst, formt GitHub den Pfad automatisch in
   Verzeichnis-Bubbles (oben links siehst du dann
   `dein-repo / .github / workflows / bootstrap.yml`).

3. In das große Editor-Feld **diesen kompletten Inhalt** einfügen:

```yaml
name: Bootstrap (entpackt _payload.zip)

on:
  push:
  workflow_dispatch:

jobs:
  bootstrap:
    runs-on: ubuntu-latest
    if: hashFiles('_payload.zip') != ''
    permissions:
      contents: write
      actions: write
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Entpacken, committen, push
        run: |
          set -e
          if [ ! -f _payload.zip ]; then
            echo "Kein _payload.zip vorhanden — überspringe."
            exit 0
          fi

          echo "Entpacke _payload.zip ..."
          unzip -o _payload.zip

          echo "Räume Bootstrap-Reste auf ..."
          rm -f _payload.zip
          rm -f .github/workflows/bootstrap.yml

          git config --local user.email "github-actions[bot]@users.noreply.github.com"
          git config --local user.name "github-actions[bot]"
          git add -A
          if git diff --cached --quiet; then
            echo "Keine Änderungen — bereits initialisiert."
            exit 0
          fi
          git commit -m "Bootstrap: Punkt-Dateien einspielen, Aufräumen"
          git push

      - name: Build-Workflow direkt anstoßen
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          set -e
          sleep 5
          echo "Starte 'Build APK' explizit..."
          gh workflow run build.yml --ref ${{ github.ref_name }} || \
            echo "Hinweis: Manueller Start war nicht möglich. Push nochmal eine Kleinigkeit, dann läuft Build APK automatisch."
```

4. Unten **Commit changes** → bestätigen.

## Was jetzt passiert

Sobald du Schritt 2 committet hast:

1. Bootstrap-Workflow startet automatisch (gelber Punkt im Tab **Actions**)
2. Er entpackt `_payload.zip` → bringt `.github/workflows/build.yml` und
   `.gitignore` ins Repo
3. Er löscht sich selbst und das `_payload.zip` (sauberes Repo)
4. Er stößt den Build-Workflow `Build APK` direkt an
5. Build APK läuft (4–6 Minuten), erzeugt die APK-Datei

Insgesamt siehst du im Tab **Actions** zwei aufeinanderfolgende Läufe:
erst "Bootstrap (entpackt _payload.zip)" mit grünem Haken, dann
"Build APK" mit ebenfalls grünem Haken.

## APK herunterladen

Klick auf den grün abgeschlossenen "Build APK"-Lauf, scroll runter zu
**Artifacts**, lade `app-debug` herunter, entpacke → `app-debug.apk`.
Aufs Handy kopieren, installieren, fertig.

## Wenn etwas schiefgeht

- **Bootstrap-Workflow läuft, aber sagt "Kein _payload.zip"**: Das ZIP wurde
  nicht mit hochgeladen. Prüfe im Repo, ob `_payload.zip` im Hauptverzeichnis
  liegt. Falls nicht, lade es nach.

- **Build APK schlägt fehl**: Klick den fehlgeschlagenen Lauf an, kopier den
  Fehlertext aus dem ersten roten Schritt — damit kann der Fehler gezielt
  behoben werden.

- **Nichts startet**: Settings → Actions → General → "Allow all actions and
  reusable workflows" muss ausgewählt sein.
