package com.onideity.rommcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.onideity.rommcompanion.ui.nav.RommNavHost
import com.onideity.rommcompanion.ui.theme.RommCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RommCompanionApp).container

        setContent {
            RommCompanionTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RommNavHost(container)
                }
            }
        }
    }
}
