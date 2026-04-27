package com.example.vokabeltrainer.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    onOpenDict: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenUnits: () -> Unit,
    onOpenTrash: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val msg = state.error ?: state.message
        if (!msg.isNullOrBlank()) {
            snackbar.showSnackbar(msg)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vokabeltrainer") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Heute lernen", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${state.dueToday} fällige Karten (Tageslimit ${state.dailyLimit})",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onStartQuiz,
                        enabled = state.dueToday > 0,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (state.dueToday > 0) "Lernen starten" else "Nichts fällig – später wiederkommen")
                    }
                }
            }

            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Fortschritt", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(8.dp))
                    StatRow("Gesamt im Pool", state.total.toString())
                    StatRow("Gemeistert (Level 5)", state.mastered.toString())
                    StatRow("Ø Level", "%.2f / 5".format(state.avgLevel))
                }
            }

            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Verteilung nach Level", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Level 0 = neu / falsch beantwortet, Level 5 = gemeistert",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    val maxCount = (state.levelDistribution.values.maxOrNull() ?: 0).coerceAtLeast(1)
                    for (level in 0..5) {
                        val count = state.levelDistribution[level] ?: 0
                        LevelBar(
                            level = level,
                            count = count,
                            maxCount = maxCount
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }

            // Hauptaktionen: 2x2-Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenDict,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Book, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Wörterbuch")
                }
                OutlinedButton(
                    onClick = onOpenUnits,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.School, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Units")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { vm.reloadFromAsset() },
                    enabled = !state.loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.CloudDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.loading) "Lade…" else "Aktualisieren")
                }
                OutlinedButton(
                    onClick = onOpenTrash,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Papierkorb")
                }
            }

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun LevelBar(level: Int, count: Int, maxCount: Int) {
    val ratio = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "L$level",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(36.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(ratio)
                        .background(
                            if (level == 5) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            count.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(56.dp)
        )
    }
}
