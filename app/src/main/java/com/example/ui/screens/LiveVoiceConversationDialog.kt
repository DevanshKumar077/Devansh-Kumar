package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ai.GeminiLiveVoiceService
import com.example.ai.LiveConnectionState
import com.example.ai.LiveVoiceListener
import com.example.data.model.Course
import com.example.data.model.Faq
import com.example.data.model.InstituteProfile
import com.example.ui.theme.*

data class LiveTranscriptLine(
    val speaker: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun LiveVoiceConversationDialog(
    profile: InstituteProfile,
    courses: List<Course>,
    faqs: List<Faq>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    var connectionState by remember { mutableStateOf(LiveConnectionState.DISCONNECTED) }
    var userAmplitude by remember { mutableFloatStateOf(0.1f) }
    var aiAmplitude by remember { mutableFloatStateOf(0.1f) }
    var isAiSpeaking by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    val transcriptList = remember { mutableStateListOf<LiveTranscriptLine>() }
    var textInput by remember { mutableStateOf("") }

    val liveService = remember { GeminiLiveVoiceService(context) }

    DisposableEffect(Unit) {
        liveService.setListener(object : LiveVoiceListener {
            override fun onConnectionStateChanged(state: LiveConnectionState) {
                connectionState = state
            }

            override fun onUserSpeechDetected(amplitude: Float) {
                userAmplitude = amplitude
            }

            override fun onAiSpeaking(speaking: Boolean, amplitude: Float) {
                isAiSpeaking = speaking
                aiAmplitude = if (speaking) amplitude else 0.1f
            }

            override fun onTranscriptReceived(sender: String, text: String, isFinal: Boolean) {
                if (transcriptList.isNotEmpty() && transcriptList.last().speaker == sender && !isFinal) {
                    val last = transcriptList.removeAt(transcriptList.size - 1)
                    transcriptList.add(last.copy(text = last.text + " " + text))
                } else {
                    transcriptList.add(LiveTranscriptLine(sender, text))
                }
            }

            override fun onError(message: String) {
                transcriptList.add(LiveTranscriptLine("SYSTEM", "Error: $message"))
            }
        })

        if (hasMicPermission) {
            liveService.startSession(profile, courses, faqs)
        }

        onDispose {
            liveService.endSession()
        }
    }

    // Animation Pulse for the AI Orb
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isAiSpeaking) 1.25f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isAiSpeaking) 400 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val listState = rememberLazyListState()
    LaunchedEffect(transcriptList.size) {
        if (transcriptList.isNotEmpty()) {
            listState.animateScrollToItem(transcriptList.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBgPrimary),
            color = DarkBgPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .windowInsetsPadding(WindowInsets.statusBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(DarkCardSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimaryDark, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${profile.aiEmployeeName} Live Voice",
                                color = TextPrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-Time Admissions Voice Call",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Model Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2E1065), RoundedCornerShape(20.dp))
                            .border(1.dp, PrimaryAccent, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "gemini-3.8-live",
                            color = AiHighlight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection State Pill
                val statusColor = when (connectionState) {
                    LiveConnectionState.CONNECTED, LiveConnectionState.LISTENING -> SuccessGreen
                    LiveConnectionState.AI_TALKING -> AiHighlight
                    LiveConnectionState.CONNECTING -> WarningAmber
                    else -> ErrorRed
                }

                val statusText = when (connectionState) {
                    LiveConnectionState.CONNECTED -> "Live API Connected • Listening"
                    LiveConnectionState.LISTENING -> if (isMuted) "Microphone Muted" else "Listening to your voice..."
                    LiveConnectionState.AI_TALKING -> "${profile.aiEmployeeName} is speaking..."
                    LiveConnectionState.CONNECTING -> "Connecting to Gemini 3.8 Live API..."
                    LiveConnectionState.ERROR -> "Connection Failed"
                    LiveConnectionState.DISCONNECTED -> "Session Ended"
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(statusText, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animated AI Voice Orb & Soundwave Canvas
                Box(
                    modifier = Modifier
                        .size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glowing ripple rings
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(pulseScale)
                    ) {
                        val activeColor = if (isAiSpeaking) PrimaryAccent else SecondaryAccent
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    activeColor.copy(alpha = 0.45f),
                                    activeColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 1.5f
                        )
                    }

                    // Inner glowing orb core
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryAccent, SecondaryAccent, AiHighlight)
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAiSpeaking) Icons.Default.VolumeUp else Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasMicPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Microphone Access to Speak")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Live Rolling Transcript Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(DarkCardElevated, RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    if (transcriptList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Speak into your microphone or tap a prompt below.\nAarohi will respond in real-time audio with verified course knowledge.",
                                color = TextMutedDark,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(transcriptList) { line ->
                                val isUser = line.speaker == "YOU"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .background(
                                                if (isUser) PrimaryAccent else DarkCardSurface,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isUser) PrimaryAccent else DarkBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isUser) "You" else profile.aiEmployeeName,
                                                color = if (isUser) Color.White.copy(alpha = 0.8f) else AiHighlight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = line.text,
                                                color = TextPrimaryDark,
                                                fontSize = 13.sp,
                                                lineHeight = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Spoken Prompts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickVoiceChip("Tell me about NEET dropper batch") {
                        liveService.sendTextMessage("Tell me about your NEET dropper batch")
                    }
                    QuickVoiceChip("What are the fees for JEE 2-year?") {
                        liveService.sendTextMessage("What are the fees for JEE 2-year premier course?")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Call Controls (Mic Mute, End Session, Send Text)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Toggle
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            if (isMuted) {
                                liveService.stopMicStreaming()
                            } else {
                                liveService.startMicStreaming()
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isMuted) ErrorRed.copy(alpha = 0.2f) else DarkCardSurface, CircleShape)
                            .border(1.dp, if (isMuted) ErrorRed else DarkBorder, CircleShape)
                            .testTag("live_mic_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (isMuted) ErrorRed else TextPrimaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Main End Call Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .padding(horizontal = 8.dp)
                            .testTag("live_end_call_button")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("End Voice Call", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.QuickVoiceChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(DarkCardSurface, RoundedCornerShape(8.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = TextSecondaryDark,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
