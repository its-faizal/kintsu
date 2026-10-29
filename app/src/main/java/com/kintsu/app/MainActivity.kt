package com.kintsu.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.kintsu.app.ui.KintsuApp

class MainActivity : ComponentActivity() {

    private val shortcutAction = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        shortcutAction.value = intent?.action
        setContent {
            KintsuApp(
                shortcutAction = shortcutAction.value,
                onShortcutHandled = { shortcutAction.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        shortcutAction.value = intent.action
    }
}
