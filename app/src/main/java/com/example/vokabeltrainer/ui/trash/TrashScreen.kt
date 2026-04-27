package com.example.vokabeltrainer.ui.trash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    vm: TrashViewModel = viewModel()
) {
    val deleted by vm.deletedWords.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Papierkorb (${deleted.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { pad ->
        if (deleted.isEmpty()) {
            Column(
                Modifier.padding(pad).padding(24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Papierkorb ist leer.",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hier landen Vokabeln, die du im Wörterbuch löschst. " +
                    "Du kannst sie jederzeit wiederherstellen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                Modifier.padding(pad).fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items = deleted, key = { it.id }) { w ->
                    ListItem(
                        headlineContent = { Text(w.en, style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text(w.deList.joinToString(", ")) },
                        trailingContent = {
                            IconButton(onClick = { vm.restore(w.id) }) {
                                Icon(
                                    Icons.Filled.Refresh,
                                    contentDescription = "Wiederherstellen"
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
