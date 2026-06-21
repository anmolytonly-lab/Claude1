package com.auraos.launcher.ui.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.auraos.launcher.AssistantState
import com.auraos.launcher.AuraViewModel
import com.auraos.launcher.ChatMessage
import com.auraos.launcher.voice.VoiceState

@Composable
fun AuraPanelOverlay(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val assistantState by viewModel.assistantState.collectAsState()
    val isVisible = assistantState != AssistantState.HIDDEN

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                slideInVertically(spring(stiffness = Spring.StiffnessMedium)) { it / 3 },
        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) +
                slideOutVertically(spring(stiffness = Spring.StiffnessHigh)) { it / 3 },
        modifier = modifier
    ) {
        AuraPanel(viewModel = viewModel)
    }
}

@Composable
private fun AuraPanel(viewModel: AuraViewModel) {
    val messages by viewModel.messages.collectAsState()
    val assistantState by viewModel.assistantState.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val voiceState by viewModel.voiceManager.voiceState.collectAsState()
    val rmsLevel by viewModel.voiceManager.rmsLevel.collectAsState()
    val partialResult by viewModel.voiceManager.partialResult.collectAsState()
    val wakeWordEnabled by viewModel.wakeWordEnabled.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AuraHeader(
                wakeWordEnabled = wakeWordEnabled,
                onClose = { viewModel.hideAssistant() },
                onClear = { viewModel.clearConversation() },
                onToggleWakeWord = { viewModel.toggleWakeWord(context) }
            )

            // Messages or voice visualizer
            Box(modifier = Modifier.weight(1f)) {
                if (assistantState == AssistantState.LISTENING) {
                    VoiceVisualizer(
                        rmsLevel = rmsLevel,
                        partialText = partialResult,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = messages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                onConfirm = { message.pendingAction?.let { viewModel.confirmAction(it) } },
                                onCancel = { message.pendingAction?.let { viewModel.cancelAction(it) } }
                            )
                        }
                    }
                }
            }

            // Input area
            InputArea(
                text = inputText,
                isListening = assistantState == AssistantState.LISTENING,
                isThinking = assistantState == AssistantState.THINKING,
                onTextChange = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendMessage() },
                onVoiceStart = { viewModel.startVoiceInput() },
                onVoiceStop = { viewModel.stopVoiceInput() }
            )
        }
    }
}

@Composable
private fun AuraHeader(
    wakeWordEnabled: Boolean,
    onClose: () -> Unit,
    onClear: () -> Unit,
    onToggleWakeWord: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Aura",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            // Wake word toggle
            IconButton(
                onClick = onToggleWakeWord,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (wakeWordEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    if (wakeWordEnabled) Icons.Rounded.RecordVoiceOver else Icons.Rounded.MicOff,
                    contentDescription = if (wakeWordEnabled) "Wake word ON" else "Wake word OFF"
                )
            }

            IconButton(onClick = onClear) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Clear chat",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val isUser = message.isUser

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (message.isTyping) {
            // Thinking indicator
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
                modifier = Modifier.widthIn(max = 100.dp)
            ) {
                ThinkingAnimation(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            }
            return
        }

        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary
            else if (message.isError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = if (isUser)
                RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
            else
                RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = message.displayedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else if (message.isError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurface
                )

                // Action confirmation buttons
                if (message.isActionCard && message.pendingAction != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(onClick = onConfirm) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm")
                        }
                        OutlinedButton(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputArea(
    text: String,
    isListening: Boolean,
    isThinking: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Voice button
            IconButton(
                onClick = { if (isListening) onVoiceStop() else onVoiceStart() },
                enabled = !isThinking,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isListening) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Voice input"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text input
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        if (isListening) "Listening..."
                        else if (isThinking) "Thinking..."
                        else "Ask Aura anything...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                enabled = !isListening && !isThinking,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isThinking,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}
