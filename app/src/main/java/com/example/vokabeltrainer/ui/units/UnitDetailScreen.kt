package com.example.vokabeltrainer.ui.units

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailScreen(
    unitId: String,
    onBack: () -> Unit,
    onStartUnitQuiz: (String) -> Unit
) {
    val vm: UnitDetailViewModel = viewModel(
        key = "unit_$unitId",
        factory = UnitDetailViewModel.factory(unitId)
    )
    val state by vm.state.collectAsState()
    val words by vm.words.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showAdd by remember { mutableStateOf(false) }

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
                title = { Text(state.unit?.name ?: "Unit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Vokabel hinzufügen")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
        ) {
            // Lernen-Button
            Button(
                onClick = { onStartUnitQuiz(unitId) },
                enabled = words.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (words.isEmpty()) "Noch keine Vokabeln in dieser Unit"
                    else "Diese Unit lernen (${words.size})"
                )
            }

            HorizontalDivider()

            // Wortliste
            if (words.isEmpty()) {
                Column(
                    Modifier.padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Tipp das + unten rechts an, um deine erste Vokabel " +
                        "in dieser Unit anzulegen.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(items = words, key = { it.id }) { w ->
                        ListItem(
                            headlineContent = { Text(w.en, style = MaterialTheme.typography.bodyLarge) },
                            supportingContent = { Text(w.deList.joinToString(", ")) },
                            trailingContent = {
                                Column(horizontalAlignment = Alignment.End) {
                                    if (w.pos.isNotBlank()) {
                                        Text(w.pos, style = MaterialTheme.typography.labelMedium)
                                    }
                                    if (w.customTranslations) {
                                        Text(
                                            "ersetzt",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddWordDialog(
            onCancel = { showAdd = false },
            onConfirm = { en, de, pos ->
                vm.addWord(en, de, pos)
                showAdd = false
            }
        )
    }
}

@Composable
private fun AddWordDialog(
    onCancel: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var en by remember { mutableStateOf("") }
    var de by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("noun") }
    val posOptions = listOf("noun", "verb", "adj", "adv")

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Vokabel hinzufügen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = en,
                    onValueChange = { en = it.take(100) },
                    label = { Text("Englisch (Verben mit \"to \")") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = de,
                    onValueChange = { de = it.take(200) },
                    label = { Text("Deutsch (mehrere mit Komma)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Wortart", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    posOptions.forEach { p ->
                        FilterChip(
                            selected = pos == p,
                            onClick = { pos = p },
                            label = { Text(p) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(en, de, pos) },
                enabled = en.isNotBlank() && de.isNotBlank()
            ) { Text("Hinzufügen") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Abbrechen") }
        }
    )
}
