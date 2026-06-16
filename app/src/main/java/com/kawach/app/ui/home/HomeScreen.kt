package com.kawach.app.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.*
import com.kawach.app.KawachApp
import com.kawach.app.ui.fakecall.FakeCallActivity
import com.kawach.app.ui.theme.SosRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as KawachApp

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            app.contactRepository,
            app.incidentRepository,
            app.sosManager,
            app.settingsDataStore.shakeToSosEnabled
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contacts by viewModel.personalContacts.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // ── Shake-to-SOS via accelerometer ────────────────────────────────────
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

    DisposableEffect(uiState.shakeEnabled) {
        if (!uiState.shakeEnabled) return@DisposableEffect onDispose {}
        val detector = viewModel.buildShakeDetector {
            viewModel.activateSos(context)
        }
        sensorManager.registerListener(detector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(detector) }
    }

    // ── Walking timer dialog state ─────────────────────────────────────────
    var showTimerDialog by remember { mutableStateOf(false) }
    var selectedMinutes by remember { mutableIntStateOf(15) }

    // ── Auto-share location link via Android share sheet ──────────────────
    LaunchedEffect(uiState.sharedLocationLink) {
        val link = uiState.sharedLocationLink ?: return@LaunchedEffect
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "मेरा Location: $link")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Location Share करें"))
        viewModel.clearSharedLocation()
    }

    // ── Permission states ──────────────────────────────────────────────────
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val smsPermission = rememberPermissionState(Manifest.permission.SEND_SMS)
    val callPermission = rememberPermissionState(Manifest.permission.CALL_PHONE)

    // ── SOS full-screen overlay ────────────────────────────────────────────
    if (uiState.sosActive) {
        SosActiveOverlay(onCancel = { viewModel.deactivateSos() })
        return
    }

    // ── Normal home screen ─────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "कवच",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "आपकी सुरक्षा, आपके हाथ में",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(8.dp))

        // No-contacts warning
        if (uiState.personalContactCount == 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "कोई Emergency Contact नहीं है! Contacts में जाकर जोड़ें।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Walking timer status strip
        if (uiState.walkingTimerRunning) {
            WalkingTimerStrip(
                remaining = uiState.walkingTimerRemaining,
                onCancel = { viewModel.cancelWalkingTimer(context) }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.weight(1f))

        // ── BIG SOS BUTTON ────────────────────────────────────────────────
        SosPanicButton(
            onHoldComplete = {
                if (!smsPermission.status.isGranted) {
                    smsPermission.launchPermissionRequest()
                } else if (!locationPermission.status.isGranted) {
                    locationPermission.launchPermissionRequest()
                } else {
                    viewModel.activateSos(context)
                }
            }
        )

        Text(
            text = "2 सेकंड दबाए रखें — SOS भेजें",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        // ── Quick action row ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Filled.Share,
                label = "Location\nShare",
                loading = uiState.locationShareLoading,
                onClick = {
                    if (!locationPermission.status.isGranted) {
                        locationPermission.launchPermissionRequest()
                    } else {
                        viewModel.shareLocation(context)
                    }
                }
            )
            QuickActionButton(
                icon = Icons.Filled.PhoneInTalk,
                label = "Fake\nCall",
                onClick = {
                    context.startActivity(
                        Intent(context, FakeCallActivity::class.java)
                    )
                }
            )
            QuickActionButton(
                icon = Icons.Filled.Timer,
                label = "Walking\nTimer",
                onClick = { showTimerDialog = true }
            )
        }
    }

    // ── Error snackbar ─────────────────────────────────────────────────────
    if (uiState.errorMessage != null) {
        LaunchedEffect(uiState.errorMessage) {
            delay(3000)
            viewModel.clearError()
        }
    }

    // ── Walking timer duration picker ──────────────────────────────────────
    if (showTimerDialog) {
        WalkingTimerDialog(
            selectedMinutes = selectedMinutes,
            onMinutesChange = { selectedMinutes = it },
            onStart = {
                viewModel.startWalkingTimer(context, selectedMinutes)
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }
}

// ─── SOS Panic Button ────────────────────────────────────────────────────────

@Composable
fun SosPanicButton(onHoldComplete: () -> Unit) {
    val scope = rememberCoroutineScope()
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isHolding) 0.92f else 1f,
        animationSpec = tween(150),
        label = "sos_scale"
    )

    val progressAnim by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(100),
        label = "sos_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Progress ring
        CircularProgressIndicator(
            progress = { progressAnim },
            modifier = Modifier.size(200.dp),
            color = SosRed,
            strokeWidth = 6.dp,
            trackColor = SosRed.copy(alpha = 0.2f)
        )

        // Main SOS button
        Button(
            onClick = {},
            modifier = Modifier
                .size(172.dp)
                .scale(scale)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            var elapsed = 0
                            val totalMs = 2000
                            val tickMs = 50
                            var triggered = false
                            scope.launch {
                                while (elapsed < totalMs) {
                                    delay(tickMs.toLong())
                                    elapsed += tickMs
                                    holdProgress = elapsed.toFloat() / totalMs
                                }
                                triggered = true
                                onHoldComplete()
                            }
                            tryAwaitRelease()
                            isHolding = false
                            if (!triggered) holdProgress = 0f
                        }
                    )
                },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = SosRed),
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = "SOS",
                    modifier = Modifier.size(52.dp),
                    tint = Color.White
                )
                Text(
                    text = "SOS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

// ─── SOS Active full-screen overlay ──────────────────────────────────────────

@Composable
fun SosActiveOverlay(onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_flash")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "sos_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SosRed.copy(alpha = alpha)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = Color.White
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "SOS सक्रिय!",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "मदद भेजी जा रही है…\nसायरन और torch चालू है",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    text = "रद्द करें (Cancel)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SosRed
                )
            }
        }
    }
}

// ─── Walking Timer strip ──────────────────────────────────────────────────────

@Composable
fun WalkingTimerStrip(remaining: Long, onCancel: () -> Unit) {
    val minutes = remaining / 60
    val seconds = remaining % 60
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Timer, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "%02d:%02d — Walking Timer".format(minutes, seconds),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onCancel) {
                Text("मैं Safe हूँ", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ─── Quick Action Button ──────────────────────────────────────────────────────

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            } else {
                Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

// ─── Walking Timer dialog ─────────────────────────────────────────────────────

@Composable
fun WalkingTimerDialog(
    selectedMinutes: Int,
    onMinutesChange: (Int) -> Unit,
    onStart: () -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(5, 10, 15, 20, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Walking Home Timer") },
        text = {
            Column {
                Text("कितनी देर बाद SOS होना चाहिए?",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                options.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { min ->
                            FilterChip(
                                selected = selectedMinutes == min,
                                onClick = { onMinutesChange(min) },
                                label = { Text("${min}m") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = onStart) { Text("Timer Start करें") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
