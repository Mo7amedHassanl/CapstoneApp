package com.m7md7sn.capstoneApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.m7md7sn.capstoneApp.ui.app.TannitheaApp
import com.m7md7sn.capstoneApp.ui.theme.TannitheaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TannitheaTheme {
                TannitheaApp()
            }
        }
    }
}
