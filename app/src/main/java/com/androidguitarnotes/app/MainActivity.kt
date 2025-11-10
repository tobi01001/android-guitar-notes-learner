package com.androidguitarnotes.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*

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
        NavHost(navController, startDestination = "home") {
            composable("home") { HomeScreen(onStartPractice = { navController.navigate("practice") }) }
            composable("practice") { PracticeScreen(onBack = { navController.popBackStack() }) }
            composable("settings") { SettingsScreen() }
        }
    }
}

@Composable
fun HomeScreen(onStartPractice: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Guitar Notes Learner") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Welcome — select Practice to start a session")
            Button(onClick = onStartPractice) { Text("Practice") }
            Button(onClick = { /* navigate to settings */ }) { Text("Settings") }
        }
    }
}

@Composable
fun PracticeScreen(onBack: () -> Unit) {
    val recordPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> }
    LaunchedEffect(Unit) {
        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Practice") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Practice configuration (UI controls to select strings, fret range, mode, duration)")
            Button(onClick = { /* start practice */ }) { Text("Start Session (not implemented)") }
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
    }
}

@Composable
fun SettingsScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Tuning: Standard (E A D G B E) — tuner will be added later")
        }
    }
}
