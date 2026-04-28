package com.example.vokabeltrainer.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vokabeltrainer.grading.Grader
import com.example.vokabeltrainer.ui.theme.Correct
import com.example.vokabeltrainer.ui.theme.Wrong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(onFinished: () -> Unit, vm: QuizViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var input by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.card?.word?.id) {
        input = ""
        if (state.card != null) focus.requestFocus()
    }

    LaunchedEffect(state.deletedNotice) {
        val msg = state.deletedNotice
        if (!msg.isNullOrBlank()) {
            snackbar.showSnackbar(msg)
            vm.clearDeletedNotice()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz – ${state.done}/${state.done + state.remaining + if (state.card != null) 1 else 0}") },
                navigationIcon = {
                    TextButton(onClick = onFinished) { Text("Schluss") }
                },
                actions = {
                    if (state.card != null && !state.finished) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Vokabel löschen")
                        }
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.loading -> {
                    CircularProgressIndicator()
                }
                state.finished -> {
                    FinishedCard(
                        done = state.sessionCorrect + state.sessionWrong,
                        correct = state.sessionCorrect,
                        wrong = state.sessionWrong,
                        onBack = onFinished
                    )
                }
                state.card != null -> {
                    val card = state.card!!
                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(24.dp)) {
                            Text(
                                text = if (card.direction == Grader.Direction.EN_TO_DE) "Englisch → Deutsch" else "Deutsch → Englisch",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                card.prompt,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (card.word.pos.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "(${card.word.pos})",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(if (card.direction == Grader.Direction.EN_TO_DE) "Deutsche Übersetzung" else "Englische Übersetzung") },
                        singleLine = true,
                        enabled = state.lastAnswerCorrect == null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (state.lastAnswerCorrect == null) vm.submit(input)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focus)
                    )

                    if (state.lastAnswerCorrect == null) {
                        Button(
                            onClick = { vm.submit(input) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (input.isBlank()) "Lösung zeigen" else "Prüfen")
                        }
                    } else {
                        val ok = state.lastAnswerCorrect == true
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (ok) Correct else Wrong
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    if (ok) "Richtig!" else "Falsch",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Lösung: " + (state.lastSolutionShown?.joinToString(" / ") ?: ""),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                        Button(
                            onClick = { vm.next() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Weiter") }
                    }
                }
                else -> {
                    FinishedCard(
                        done = 0, correct = 0, wrong = 0, onBack = onFinished
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        val word = state.card?.word
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Vokabel löschen?") },
            text = {
                Text(
                    "\"${word?.en ?: ""}\" wird in den Papierkorb verschoben " +
                    "und nicht mehr abgefragt. Wiederherstellen jederzeit über " +
                    "Home → Papierkorb möglich."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteCurrent()
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
private fun FinishedCard(done: Int, correct: Int, wrong: Int, onBack: () -> Unit) {
    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
        Column(
            Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Fertig!", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("$done beantwortet · $correct richtig · $wrong falsch",
                style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Zurück zum Start") }
        }
    }
}
