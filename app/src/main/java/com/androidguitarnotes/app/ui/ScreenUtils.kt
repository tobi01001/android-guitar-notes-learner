package com.androidguitarnotes.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

/**
 * Keeps the screen on while this composable is active in the composition.
 * When the composable leaves the composition, the screen is allowed to turn off normally.
 *
 * @param enabled Whether to keep the screen on. When false, the screen can turn off normally.
 */
@Composable
fun KeepScreenOn(enabled: Boolean = true) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose {
            view.keepScreenOn = false
        }
    }
}
