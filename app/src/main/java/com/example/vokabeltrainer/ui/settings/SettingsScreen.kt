package com.example.vokabeltrainer.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info & Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Datenquelle", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Die mitgelieferte Wortliste ist eine eigene Kuration (CC0, gemeinfrei). " +
                        "Sie wird beim ersten Start aus dem App-Asset geladen. " +
                        "Per Aktualisieren-Button kannst du sie optional aus dem Netz nachladen."
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Beim Download werden Inhalte validiert und bereinigt (Kontrollzeichen entfernt, Längen begrenzt).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("SRS-Intervalle", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Richtig: 1 / 3 / 10 / 30 / 90 / 180 Tage")
                    Text("Falsch: morgen (Level zurück auf 0)")
                    Text("Tageslimit: 100 fällige Karten")
                }
            }
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Sicherheit", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("• Nur INTERNET-Berechtigung")
                    Text("• Nur HTTPS (Cleartext verboten)")
                    Text("• Keine WebView, kein JavaScript-Bridging")
                    Text("• Parametrisierte DB-Queries (Room)")
                    Text("• Kein Cloud-Backup der App-Daten")
                }
            }
            Text(
                "Version 1.0.0 · Für Android 8.0+ (läuft auf Android 11)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
