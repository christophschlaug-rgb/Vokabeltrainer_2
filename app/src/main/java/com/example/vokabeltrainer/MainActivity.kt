package com.example.vokabeltrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vokabeltrainer.ui.dict.DictionaryScreen
import com.example.vokabeltrainer.ui.dict.WordDetailScreen
import com.example.vokabeltrainer.ui.home.HomeScreen
import com.example.vokabeltrainer.ui.quiz.QuizScreen
import com.example.vokabeltrainer.ui.quiz.QuizViewModel
import com.example.vokabeltrainer.ui.settings.SettingsScreen
import com.example.vokabeltrainer.ui.theme.VokabelTrainerTheme
import com.example.vokabeltrainer.ui.trash.TrashScreen
import com.example.vokabeltrainer.ui.units.UnitDetailScreen
import com.example.vokabeltrainer.ui.units.UnitsScreen

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
                onOpenSettings = { nav.navigate("settings") },
                onOpenUnits = { nav.navigate("units") },
                onOpenTrash = { nav.navigate("trash") }
            )
        }
        composable("quiz") {
            // Alles-Lernen-Modus (kein Unit-Filter)
            QuizScreen(onFinished = { nav.popBackStack() })
        }
        composable(
            route = "quiz/unit/{unitId}",
            arguments = listOf(navArgument("unitId") { type = NavType.StringType })
        ) { backStack ->
            val unitId = backStack.arguments?.getString("unitId") ?: return@composable
            val vm: QuizViewModel = viewModel()
            androidx.compose.runtime.LaunchedEffect(unitId) {
                vm.restrictToUnit(unitId)
            }
            QuizScreen(onFinished = { nav.popBackStack() }, vm = vm)
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
        composable("units") {
            UnitsScreen(
                onBack = { nav.popBackStack() },
                onOpenUnit = { unitId -> nav.navigate("unit/$unitId") }
            )
        }
        composable(
            route = "unit/{unitId}",
            arguments = listOf(navArgument("unitId") { type = NavType.StringType })
        ) { backStack ->
            val unitId = backStack.arguments?.getString("unitId") ?: return@composable
            UnitDetailScreen(
                unitId = unitId,
                onBack = { nav.popBackStack() },
                onStartUnitQuiz = { id -> nav.navigate("quiz/unit/$id") }
            )
        }
        composable("trash") {
            TrashScreen(onBack = { nav.popBackStack() })
        }
    }
}
