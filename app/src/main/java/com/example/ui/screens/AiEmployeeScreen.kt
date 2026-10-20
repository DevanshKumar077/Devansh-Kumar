package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.Faq
import com.example.data.model.InstituteProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.CampusPilotViewModel
import com.example.ui.viewmodel.TestChatMessage
import kotlinx.coroutines.launch

@Composable
fun AiEmployeeScreen(
    viewModel: CampusPilotViewModel,
    profile: InstituteProfile,
    courses: List<Course>,
    faqs: List<Faq>,
    testMessages: List<TestChatMessage>,
    isThinking: Boolean,
    onStartLiveVoice: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Live Simulator, 1: Identity & Rules
    var inputQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(testMessages.size) {
        if (testMessages.isNotEmpty()) {
            listState.animateScrollToItem(testMessages.size - 1)
        }
    }

    val suggestedQuestions = listOf(
        "What is the fee for JEE 2-year?",
        "Do you have a NEET dropper batch?",
        "Can I get a 40% discount?",
        "Are hostel and mess available in Kota?",
        "Book a free counselling session"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header and Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCardElevated, RoundedCornerShape(14.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(PrimaryAccent.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, PrimaryAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = AiHighlight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.aiEmployeeName,
                                    color = TextPrimaryDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(if (profile.isAiActive) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (profile.isAiActive) "ACTIVE & GROUNDED" else "PAUSED",
                                        color = if (profile.isAiActive) SuccessGreen else WarningAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Admissions Employee • ${profile.name}",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = profile.isAiActive,
                        onCheckedChange = { viewModel.toggleAiStatus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryAccent,
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = DarkBgSecondary
                        ),
                        modifier = Modifier.testTag("ai_employee_active_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChannelBadge("WhatsApp API", true)
                    ChannelBadge("Web Chat Widget", true)
                    ChannelBadge("${courses.size} Courses Indexed", true)
                    ChannelBadge("${faqs.size} FAQs Grounded", true)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Start Live Voice Call with gemini-3.8-live Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFF2E1065), SecondaryAccent.copy(alpha = 0.4f))
                    ),
                    RoundedCornerShape(12.dp)
                )
                .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { onStartLiveVoice() }
                .padding(12.dp)
                .testTag("ai_screen_live_voice_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(PrimaryAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Start Live Voice Call",
                                color = TextPrimaryDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1E1035), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("gemini-3.8-live", color = AiHighlight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Real-time bi-directional spoken audio conversation",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector (Simulator vs Configuration)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkCardSurface,
            contentColor = PrimaryAccent,
            modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Test Simulator", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Identity & Guardrails", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            // Live Test Simulator Tab
            // Suggested Questions Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestedQuestions) { q ->
                    Box(
                        modifier = Modifier
                            .background(DarkCardSurface, RoundedCornerShape(16.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                inputQuery = q
                                viewModel.sendTestMessage(q)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(q, color = TextSecondaryDark, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat Messages Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkBgSecondary, RoundedCornerShape(12.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(testMessages) { msg ->
                        TestMessageBubble(msg = msg, aiName = profile.aiEmployeeName)
                    }

                    if (isThinking) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = PrimaryAccent,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${profile.aiEmployeeName} is consulting verified knowledge base...",
                                    color = TextMutedDark,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Composer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    placeholder = { Text("Ask ${profile.aiEmployeeName} anything about admissions...", color = TextMutedDark, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("ai_test_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkCardElevated,
                        unfocusedContainerColor = DarkCardElevated,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val q = inputQuery.trim()
                        if (q.isNotEmpty()) {
                            viewModel.sendTestMessage(q)
                            inputQuery = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryAccent, RoundedCornerShape(12.dp))
                        .testTag("ai_test_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = { viewModel.resetTestChat() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(DarkCardSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("ai_test_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Chat",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Identity & Guardrails Configuration Tab
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    SettingsSectionCard(title = "AI Employee Persona") {
                        ConfigReadOnlyField("Employee Name", profile.aiEmployeeName)
                        ConfigReadOnlyField("Role", "Digital Admissions Assistant")
                        ConfigReadOnlyField("Communication Tone", profile.aiTone)
                        ConfigReadOnlyField("Languages", profile.supportedLanguages)
                        ConfigReadOnlyField("Operational Hours", profile.workingHours)
                    }
                }

                item {
                    SettingsSectionCard(title = "Grounding & Guardrails Policy") {
                        ConfigReadOnlyField("Source of Truth", "Approved Course Catalog & FAQs Only")
                        ConfigReadOnlyField("Strict Prohibition", "Never invent fees, discounts, or admission guarantees")
                        ConfigReadOnlyField(
                            "Human Escalation Triggers",
                            profile.escalationKeywords
                        )
                        ConfigReadOnlyField("Out-of-Hours Policy", profile.outOfHoursBehavior)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelBadge(label: String, isActive: Boolean) {
    Box(
        modifier = Modifier
            .background(DarkCardSurface, RoundedCornerShape(6.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(if (isActive) SuccessGreen else TextMutedDark, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = TextSecondaryDark, fontSize = 10.sp)
        }
    }
}

@Composable
private fun TestMessageBubble(msg: TestChatMessage, aiName: String) {
    val isUser = msg.sender == "YOU"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isUser) "You" else aiName,
                color = if (isUser) TextSecondaryDark else AiHighlight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isUser && msg.groundingSource.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(DarkCardElevated, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Source: ${msg.groundingSource}",
                        color = SuccessGreen,
                        fontSize = 9.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isUser) PrimaryAccent else DarkCardElevated,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (isUser) PrimaryAccent else DarkBorder,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = msg.text,
                color = TextPrimaryDark,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = TextPrimaryDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ConfigReadOnlyField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = TextMutedDark, fontSize = 11.sp)
        Text(text = value, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
