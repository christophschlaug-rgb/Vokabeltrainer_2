package com.example.vokabeltrainer.ui.dict

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vokabeltrainer.data.LearningState
import com.example.vokabeltrainer.data.Word
import com.example.vokabeltrainer.srs.SrsEngine
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    wordId: String,
    onBack: () -> Unit,
    vm: DictionaryViewModel = viewModel()
) {
    var word by remember { mutableStateOf<Word?>(null) }
    var state by remember { mutableStateOf<LearningState?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    LaunchedEffect(wordId, deleted) {
        word = vm.getWord(wordId)
        state = vm.getState(wordId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word?.en ?: "…") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    if (word != null && !deleted) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                        }
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val w = word
            if (w != null) {
                ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text(w.en, style = MaterialTheme.typography.headlineMedium)
                        if (w.pos.isNotBlank()) {
                            Text("(${w.pos}) · Niveau ${w.level}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (w.unitId != null) {
                            Text(
                                if (w.customTranslations) "Aus einer Unit (Standard ersetzt)"
                                else "Eigene Vokabel aus einer Unit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Übersetzung(en):", style = MaterialTheme.typography.labelLarge)
                        w.deList.forEach { de -> Text("• $de", style = MaterialTheme.typography.bodyLarge) }
                    }
                }
                if (deleted) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Gelöscht — wurde in den Papierkorb verschoben.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                val s = state
                if (s != null && !deleted) {
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Lernstatus", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(8.dp))
                            StatLine("Level", "${s.level} / 5")
                            StatLine(
                                "Nächste Abfrage",
                                DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(s.nextReviewDate))
                            )
                            StatLine("Gesehen", s.timesSeen.toString())
                            StatLine("Davon richtig", s.timesCorrect.toString())
                            StatLine("Intervall dieses Levels",
                                "${SrsEngine.intervalsDays.getOrNull(s.level) ?: "-"} Tage")
                            s.lastReviewedAt?.let {
                                StatLine(
                                    "Zuletzt geprüft",
                                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it))
                                )
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Vokabel löschen?") },
            text = {
                Text(
                    "\"${word?.en ?: ""}\" wird in den Papierkorb verschoben " +
                    "und nicht mehr abgefragt. Du kannst sie über den Papierkorb " +
                    "jederzeit wiederherstellen."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteWord(wordId)
                    deleted = true
                    showDeleteDialog = false
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}
