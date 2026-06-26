package com.shingar.salon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.shingar.salon.navigation.NavGraph
import com.shingar.salon.ui.theme.ScreenBackground
import com.shingar.salon.ui.theme.ShingSalonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShingSalonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ScreenBackground
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
