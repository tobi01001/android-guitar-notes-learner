package com.androidguitarnotes.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun GuitarNotesTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}
