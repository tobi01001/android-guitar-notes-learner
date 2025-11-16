package com.androidguitarnotes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.androidguitarnotes.app.notesplayed.NotesPlayedScreen
import com.androidguitarnotes.app.practice.PracticeConfig
import com.androidguitarnotes.app.practice.PracticeConfigScreen
import com.androidguitarnotes.app.practice.PracticeSessionScreen
import com.androidguitarnotes.app.settings.SettingsScreen
import com.androidguitarnotes.app.tuner.TunerScreen
import com.androidguitarnotes.app.ui.NoteColors

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
                    onOpenNotesPlayed = { navController.navigate("notesPlayed") },
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
            composable("notesPlayed") {
                NotesPlayedScreen(
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
    onOpenNotesPlayed: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Welcome message
                Text(
                    text = stringResource(R.string.home_welcome_message),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // 2x2 Grid of navigation buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // First row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HomeNavigationButton(
                            label = stringResource(R.string.home_practice),
                            icon = Icons.Filled.MusicNote,
                            backgroundColor = NoteColors.getColorForNote("A"),
                            onClick = onStartPractice,
                            modifier = Modifier.weight(1f),
                        )
                        HomeNavigationButton(
                            label = stringResource(R.string.home_tuner),
                            icon = Icons.Filled.Tune,
                            backgroundColor = NoteColors.getColorForNote("D"),
                            onClick = onOpenTuner,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Second row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        HomeNavigationButton(
                            label = stringResource(R.string.home_notes_played),
                            icon = Icons.Filled.History,
                            backgroundColor = NoteColors.getColorForNote("G"),
                            onClick = onOpenNotesPlayed,
                            modifier = Modifier.weight(1f),
                        )
                        HomeNavigationButton(
                            label = stringResource(R.string.home_settings),
                            icon = Icons.Filled.Settings,
                            backgroundColor = NoteColors.getColorForNote("C"),
                            onClick = onOpenSettings,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeNavigationButton(
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .aspectRatio(1f)
                .height(140.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = Color.White,
            ),
        shape = RoundedCornerShape(16.dp),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
