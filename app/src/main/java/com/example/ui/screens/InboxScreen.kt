package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.model.ConversationMessage
import com.example.data.model.Lead
import com.example.ui.components.LeadStatusBadge
import com.example.ui.components.ScoreBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun InboxScreen(
    viewModel: CampusPilotViewModel,
    leads: List<Lead>,
    allMessages: List<ConversationMessage>,
    activeLeadId: Long?
) {
    var selectedLeadId by remember(activeLeadId) { mutableStateOf(activeLeadId) }

    if (selectedLeadId != null) {
        val lead = leads.firstOrNull { it.id == selectedLeadId }
        val leadMessages = allMessages.filter { it.leadId == selectedLeadId }.sortedBy { it.timestamp }
        ConversationThreadView(
            viewModel = viewModel,
            lead = lead,
            messages = leadMessages,
            onBack = { selectedLeadId = null }
        )
    } else {
        ConversationListView(
            leads = leads,
            allMessages = allMessages,
            onSelectLead = { selectedLeadId = it }
        )
    }
}

@Composable
private fun ConversationListView(
    leads: List<Lead>,
    allMessages: List<ConversationMessage>,
    onSelectLead: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Conversations Inbox",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live enquiries from WhatsApp & Website",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .background(PrimaryAccent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${leads.size} Active Threads",
                    color = AiHighlight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(leads, key = { it.id }) { lead ->
                val lastMsg = allMessages.filter { it.leadId == lead.id }.maxByOrNull { it.timestamp }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCardElevated, RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (lead.humanEscalated) ErrorRed.copy(alpha = 0.5f) else DarkBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectLead(lead.id) }
                        .padding(14.dp)
                        .testTag("inbox_item_${lead.id}")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = lead.studentName,
                                    color = TextPrimaryDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (lead.humanEscalated) {
                                    Box(
                                        modifier = Modifier
                                            .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("Human Required", color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = lead.source,
                                color = if (lead.source == "WhatsApp") SuccessGreen else PrimaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = lead.courseInterest,
                            color = AiHighlight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = lastMsg?.text ?: "No messages yet.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LeadStatusBadge(status = lead.status)
                            ScoreBadge(score = lead.score)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationThreadView(
    viewModel: CampusPilotViewModel,
    lead: Lead?,
    messages: List<ConversationMessage>,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    if (lead == null) {
        onBack()
        return
    }

    var inputText by remember { mutableStateOf("") }
    var sendAsCounsellor by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        // Thread Top Bar
        Surface(
            color = DarkBgSecondary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("inbox_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryDark)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = lead.studentName,
                                color = TextPrimaryDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (lead.humanEscalated) {
                                Box(
                                    modifier = Modifier
                                        .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("Escalated", color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            text = "${lead.courseInterest} • ${lead.source}",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }

                // Escalation Toggle Button
                TextButton(
                    onClick = { viewModel.toggleLeadEscalation(lead) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (lead.humanEscalated) PrimaryAccent else ErrorRed
                    )
                ) {
                    Text(
                        text = if (lead.humanEscalated) "Resume AI" else "Take Over",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageRow(msg = msg)
            }
        }

        // Sender Role Switcher (Counsellor Reply vs Student Enquiry Simulation)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkCardSurface)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (sendAsCounsellor) "Replying as: Human Counsellor" else "Simulating as: Student Enquiry",
                color = if (sendAsCounsellor) SecondaryAccent else WarningAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = { sendAsCounsellor = !sendAsCounsellor }) {
                Text(
                    text = if (sendAsCounsellor) "Switch to Student" else "Switch to Counsellor",
                    fontSize = 11.sp
                )
            }
        }

        // Composer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBgSecondary)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = 60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        if (sendAsCounsellor) "Type counsellor reply..." else "Type student question...",
                        color = TextMutedDark,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("inbox_message_input"),
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
                    val text = inputText.trim()
                    if (text.isNotEmpty()) {
                        viewModel.sendInboxMessage(lead, text, sendAsCounsellor)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(PrimaryAccent, RoundedCornerShape(12.dp))
                    .testTag("inbox_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageRow(msg: ConversationMessage) {
    val isStudent = msg.senderType == "STUDENT"
    val isAi = msg.senderType == "AI"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isStudent) Alignment.Start else Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when {
                    isStudent -> msg.leadName
                    isAi -> "Aarohi (AI)"
                    else -> "Admissions Counsellor"
                },
                color = when {
                    isStudent -> TextSecondaryDark
                    isAi -> AiHighlight
                    else -> SecondaryAccent
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (isAi && msg.groundingSource.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(DarkCardElevated, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = msg.groundingSource,
                        color = SuccessGreen,
                        fontSize = 8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    when {
                        isStudent -> DarkCardSurface
                        isAi -> Color(0xFF1E1B4B)
                        else -> PrimaryAccent
                    },
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    when {
                        isStudent -> DarkBorder
                        isAi -> AiHighlight.copy(alpha = 0.4f)
                        else -> PrimaryAccent
                    },
                    RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
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
