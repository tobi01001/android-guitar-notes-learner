package com.androidguitarnotes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.androidguitarnotes.app.practice.PracticeConfig
import com.androidguitarnotes.app.practice.PracticeConfigScreen
import com.androidguitarnotes.app.practice.PracticeSessionScreen
import com.androidguitarnotes.app.settings.SettingsScreen
import com.androidguitarnotes.app.tuner.TunerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GuitarNotesApp()
        }
    }
}

@Composable
fun GuitarNotesApp() {
    MaterialTheme {
        val navController = rememberNavController()
        var practiceConfig by remember { mutableStateOf<PracticeConfig?>(null) }

        NavHost(navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onStartPractice = { navController.navigate("practice") },
                    onOpenTuner = { navController.navigate("tuner") },
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable("practice") {
                PracticeConfigScreen(
                    onBack = { navController.popBackStack() },
                    onStartPractice = { config ->
                        practiceConfig = config
                        navController.navigate("practiceSession")
                    },
                )
            }
            composable("practiceSession") {
                val config = practiceConfig
                if (config != null) {
                    PracticeSessionScreen(
                        config = config,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable("tuner") {
                TunerScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onStartPractice: () -> Unit,
    onOpenTuner: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Guitar Notes Learner") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Welcome — select Practice to start a session")
            Button(onClick = onStartPractice) { Text("Practice") }
            Button(onClick = onOpenTuner) { Text("Tuner") }
            Button(onClick = onOpenSettings) { Text("Settings") }
        }
    }
}
