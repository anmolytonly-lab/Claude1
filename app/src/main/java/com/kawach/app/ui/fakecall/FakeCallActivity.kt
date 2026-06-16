package com.kawach.app.ui.fakecall

import android.media.AudioManager
import android.media.RingtoneManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawach.app.KawachApp
import com.kawach.app.ui.theme.KawachTheme
import com.kawach.app.ui.theme.SafeGreen
import com.kawach.app.ui.theme.SosRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Full-screen fake incoming call screen.
 * Looks like a real Android call UI to give the user a believable reason to leave
 * an uncomfortable situation.
 */
class FakeCallActivity : ComponentActivity() {

    private var ringtonePlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on and show over lock screen
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContent {
            KawachTheme {
                var callerName by remember { mutableStateOf("Papa") }
                val scope = rememberCoroutineScope()
                var callAnswered by remember { mutableStateOf(false) }
                var callDuration by remember { mutableIntStateOf(0) }

                // Load caller name from settings
                LaunchedEffect(Unit) {
                    val app = applicationContext as KawachApp
                    callerName = app.settingsDataStore.fakeCallerName.first()
                    startRingtone()
                    // Add 2-second delay before ringing (simulates incoming call arriving)
                    delay(2000)
                }

                // Call duration counter when answered
                LaunchedEffect(callAnswered) {
                    if (callAnswered) {
                        stopRingtone()
                        while (true) {
                            delay(1000)
                            callDuration++
                        }
                    }
                }

                if (callAnswered) {
                    ActiveCallScreen(
                        callerName = callerName,
                        duration = callDuration,
                        onEnd = { finish() }
                    )
                } else {
                    IncomingCallScreen(
                        callerName = callerName,
                        onAnswer = { callAnswered = true },
                        onDecline = { finish() }
                    )
                }
            }
        }
    }

    private fun startRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtonePlayer = MediaPlayer().apply {
                setDataSource(this@FakeCallActivity, uri)
                @Suppress("DEPRECATION")
                setAudioStreamType(AudioManager.STREAM_RING)
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) { /* ringtone unavailable */ }
    }

    private fun stopRingtone() {
        ringtonePlayer?.stop()
        ringtonePlayer?.release()
        ringtonePlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}

@Composable
private fun IncomingCallScreen(
    callerName: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "ring_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E)),  // deep navy — mimics stock Android call UI
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(1f))

            // Caller avatar
            Surface(
                shape = CircleShape,
                color = Color(0xFF3F51B5),
                modifier = Modifier.size((120 * ringScale).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = callerName,
                fontSize = 36.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Incoming call…",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.weight(1f))

            // Decline / Answer row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 64.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decline
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FloatingActionButton(
                        onClick = onDecline,
                        containerColor = SosRed,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.CallEnd, contentDescription = "Decline",
                            modifier = Modifier.size(32.dp), tint = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Decline", color = Color.White.copy(alpha = 0.8f))
                }

                // Answer
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FloatingActionButton(
                        onClick = onAnswer,
                        containerColor = SafeGreen,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = "Answer",
                            modifier = Modifier.size(32.dp), tint = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Answer", color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
private fun ActiveCallScreen(
    callerName: String,
    duration: Int,
    onEnd: () -> Unit
) {
    val minutes = duration / 60
    val seconds = duration % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A237E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(1f))

            Surface(
                shape = CircleShape,
                color = Color(0xFF3F51B5),
                modifier = Modifier.size(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null,
                        modifier = Modifier.size(72.dp), tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(callerName, fontSize = 36.sp, fontWeight = FontWeight.Light, color = Color.White)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.weight(1f))

            // End call button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                FloatingActionButton(
                    onClick = onEnd,
                    containerColor = SosRed,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.CallEnd, contentDescription = "End Call",
                        modifier = Modifier.size(32.dp), tint = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Text("End", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
