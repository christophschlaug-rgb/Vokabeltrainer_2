package com.example.vokabeltrainer.ui.dict

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    onBack: () -> Unit,
    onOpenWord: (String) -> Unit,
    vm: DictionaryViewModel = viewModel()
) {
    val q by vm.query.collectAsState()
    val words by vm.words.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wörterbuch (${words.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).fillMaxSize()
        ) {
            OutlinedTextField(
                value = q,
                onValueChange = vm::setQuery,
                label = { Text("Suche (EN/DE)") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(items = words, key = { it.id }) { w ->
                    ListItem(
                        headlineContent = { Text(w.en, style = MaterialTheme.typography.bodyLarge) },
                        supportingContent = { Text(w.deList.joinToString(", ")) },
                        trailingContent = { Text(w.pos, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.clickable { onOpenWord(w.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
