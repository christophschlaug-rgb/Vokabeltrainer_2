package com.example.vokabeltrainer.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

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
            Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Tageslimit-Auswahl
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Tageslimit", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Wie viele fällige Karten möchtest du täglich abfragen lassen?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.limitOptions.forEach { opt ->
                            FilterChip(
                                selected = state.dailyLimit == opt,
                                onClick = { vm.setDailyLimit(opt) },
                                label = { Text(opt.toString()) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Aktuell: ${state.dailyLimit} pro Tag",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Datenquelle", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Die Wortliste umfasst rund 5.300 Vokabeln auf den Niveaus B1 bis C1, " +
                        "kuratiert für deutsche Lerner."
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Quellen: Eigene Kuration (CC0) und FreeDict eng-deu (GPLv3/AGPLv3), " +
                        "frequenzgefiltert über das wordfreq-Korpus.",
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
                "Version 1.2.0 · Für Android 8.0+ (läuft auf Android 11)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
