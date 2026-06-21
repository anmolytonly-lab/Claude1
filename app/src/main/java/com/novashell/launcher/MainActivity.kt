package com.novashell.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.novashell.launcher.ui.HomeScreen
import com.novashell.launcher.ui.theme.NovaShellTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            NovaShellTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen()
                }
            }
        }
    }

    // Pressing Back on the home screen should do nothing — standard launcher behavior.
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intentionally empty: the home screen IS the final destination.
    }
}
