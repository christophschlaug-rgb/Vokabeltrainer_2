package com.example.vokabeltrainer.ui.units

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsScreen(
    onBack: () -> Unit,
    onOpenUnit: (String) -> Unit,
    vm: UnitsViewModel = viewModel()
) {
    val units by vm.units.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Units (${units.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Neue Unit")
            }
        }
    ) { pad ->
        if (units.isEmpty()) {
            Column(
                Modifier.padding(pad).padding(24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Noch keine Units angelegt.",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Lege eine Unit an, um Vokabeln aus deinem Englischbuch " +
                    "zugeordnet zu üben. Zum Beispiel \"Headway B1, Unit 3\".",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = units, key = { it.id }) { u ->
                    ElevatedCard(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUnit(u.id) }
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${u.wordCount} Vokabeln",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(onClick = { deleteCandidate = u.id }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Unit löschen"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreateUnitDialog(
            onCancel = { showCreate = false },
            onConfirm = { name ->
                vm.createUnit(name)
                showCreate = false
            }
        )
    }

    deleteCandidate?.let { unitId ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Unit löschen?") },
            text = {
                Text(
                    "Alle eigenen Vokabeln dieser Unit werden gelöscht. " +
                    "Standardvokabeln, die du in der Unit überschrieben hattest, " +
                    "verlieren die Unit-Zuordnung, bleiben aber erhalten."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteUnit(unitId)
                    deleteCandidate = null
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun CreateUnitDialog(
    onCancel: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Neue Unit") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(80) },
                label = { Text("Name (z.B. Buch, Kapitel 3)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Anlegen") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Abbrechen") }
        }
    )
}
