package com.example.vokabeltrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vokabeltrainer.ui.dict.DictionaryScreen
import com.example.vokabeltrainer.ui.dict.WordDetailScreen
import com.example.vokabeltrainer.ui.home.HomeScreen
import com.example.vokabeltrainer.ui.quiz.QuizScreen
import com.example.vokabeltrainer.ui.settings.SettingsScreen
import com.example.vokabeltrainer.ui.theme.VokabelTrainerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VokabelTrainerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartQuiz = { nav.navigate("quiz") },
                onOpenDict = { nav.navigate("dict") },
                onOpenSettings = { nav.navigate("settings") }
            )
        }
        composable("quiz") {
            QuizScreen(onFinished = { nav.popBackStack() })
        }
        composable("dict") {
            DictionaryScreen(
                onBack = { nav.popBackStack() },
                onOpenWord = { id -> nav.navigate("word/$id") }
            )
        }
        composable("word/{id}") { backStack ->
            val id = backStack.arguments?.getString("id") ?: return@composable
            WordDetailScreen(wordId = id, onBack = { nav.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { nav.popBackStack() })
        }
    }
}
