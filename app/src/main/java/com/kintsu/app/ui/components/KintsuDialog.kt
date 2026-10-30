package com.kintsu.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kintsu.app.ui.theme.KintsuTheme

@Composable
fun KintsuDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val colors = KintsuTheme.colors
    val typography = KintsuTheme.typography

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = typography.itemTitle,
                color = colors.textPrimary
            )
        },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        containerColor = colors.surfaceElevated,
        shape = KintsuTheme.shapes.large
    )
}
