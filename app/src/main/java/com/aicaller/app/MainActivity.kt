package com.aicaller.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aicaller.app.ui.navigation.AppNavHost
import com.aicaller.app.ui.theme.AiCallerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

private val REQUIRED_PERMISSIONS = buildList {
    add(Manifest.permission.CALL_PHONE)
    add(Manifest.permission.READ_PHONE_STATE)
    add(Manifest.permission.READ_CALL_LOG)
    add(Manifest.permission.READ_CONTACTS)
    add(Manifest.permission.RECORD_AUDIO)
    add(Manifest.permission.SEND_SMS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiCallerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AiCallerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AiCallerApp() {
    val permissionsState = rememberMultiplePermissionsState(REQUIRED_PERMISSIONS)

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (permissionsState.allPermissionsGranted) {
        AppNavHost()
    } else {
        PermissionRationale(onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() })
    }
}

@Composable
private fun PermissionRationale(onRequestPermissions: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AI Caller needs a few permissions", style = MaterialTheme.typography.titleLarge)
        Text(
            "To screen spam calls, transcribe and summarize calls, manage contacts, and send " +
                "smart auto-replies, AI Caller needs access to phone, contacts, microphone, call log and SMS.",
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Button(onClick = onRequestPermissions) {
            Text("Grant permissions")
        }
    }
}
