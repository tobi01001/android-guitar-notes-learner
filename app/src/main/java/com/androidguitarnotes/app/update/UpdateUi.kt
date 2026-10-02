package com.androidguitarnotes.app.update

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidguitarnotes.app.R
import kotlinx.coroutines.launch

@Composable
private fun UpdateAvailableDialog(
    release: ReleaseInfo,
    currentVersion: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.update_available_title)) },
        text = { Text(stringResource(R.string.update_available_message, release.version, currentVersion)) },
        confirmButton = {
            TextButton(onClick = {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.downloadUrl)))
                } catch (e: Exception) {
                    // No handler available; nothing more to do.
                }
                onDismiss()
            }) { Text(stringResource(R.string.update_download)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.update_later)) }
        },
    )
}

/** Runs a check on app start if the last check is older than the configured interval. */
@Composable
fun AutoUpdateCheck() {
    val context = LocalContext.current
    val prefs = remember { UpdatePreferences(context.applicationContext) }
    var release by remember { mutableStateOf<ReleaseInfo?>(null) }
    val current = remember { UpdateChecker.currentVersion(context) }

    LaunchedEffect(Unit) {
        if (!prefs.isCheckDue()) return@LaunchedEffect
        try {
            val latest = UpdateChecker.fetchLatestRelease()
            prefs.markChecked()
            if (UpdateChecker.isNewer(latest.version, current)) release = latest
        } catch (e: Exception) {
            // Silent failure for automatic checks; retried on next start.
        }
    }

    release?.let { UpdateAvailableDialog(it, current, onDismiss = { release = null }) }
}

/** Settings rows: manual check, auto-check toggle and interval. */
@Composable
fun UpdateSettings() {
    val context = LocalContext.current
    val prefs = remember { UpdatePreferences(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val autoCheck by prefs.autoCheckEnabled.collectAsStateWithLifecycle(initialValue = true)
    val days by prefs.intervalDays.collectAsStateWithLifecycle(initialValue = UpdatePreferences.DEFAULT_INTERVAL_DAYS)
    val current = remember { UpdateChecker.currentVersion(context) }
    var status by remember { mutableStateOf<String?>(null) }
    var release by remember { mutableStateOf<ReleaseInfo?>(null) }
    var checking by remember { mutableStateOf(false) }

    val upToDate = stringResource(R.string.update_up_to_date)
    val failed = stringResource(R.string.update_check_failed)
    val checkingText = stringResource(R.string.update_checking)

    Column(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(enabled = !checking) {
                    checking = true
                    status = checkingText
                    scope.launch {
                        try {
                            val latest = UpdateChecker.fetchLatestRelease()
                            prefs.markChecked()
                            if (UpdateChecker.isNewer(latest.version, current)) {
                                release = latest
                                status = null
                            } else {
                                status = upToDate
                            }
                        } catch (e: Exception) {
                            status = failed
                        }
                        checking = false
                    }
                }.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(stringResource(R.string.check_for_updates), style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text(
                status ?: stringResource(R.string.check_for_updates_description),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.auto_update_check), style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text(
                    stringResource(R.string.auto_update_check_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
            Switch(checked = autoCheck, onCheckedChange = { scope.launch { prefs.setAutoCheckEnabled(it) } })
        }
        if (autoCheck) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    stringResource(R.string.update_interval_days, days),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Slider(
                    value = days.toFloat(),
                    onValueChange = { scope.launch { prefs.setIntervalDays(it.toInt()) } },
                    valueRange = 1f..30f,
                    steps = 28,
                )
            }
        }
    }

    release?.let { UpdateAvailableDialog(it, current, onDismiss = { release = null }) }
}
