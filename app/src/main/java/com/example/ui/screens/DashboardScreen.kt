package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun DashboardScreen(
    viewModel: CampusPilotViewModel,
    profile: InstituteProfile,
    leads: List<Lead>,
    tasks: List<FollowUpTask>,
    appointments: List<Appointment>,
    onQuickAddLead: () -> Unit,
    onStartLiveVoice: () -> Unit = {}
) {
    val totalLeadsCount = leads.size
    val newLeadsCount = leads.count { it.status == "NEW" }
    val qualifiedCount = leads.count { it.status == "QUALIFIED" }
    val counsellingCount = appointments.count { it.status == "CONFIRMED" }
    val overdueTasksCount = tasks.count { it.isOverdue && it.status == "PENDING" }
    val enrolledCount = leads.count { it.status == "ENROLLED" }
    val conversionRate = if (totalLeadsCount > 0) ((enrolledCount.toFloat() / totalLeadsCount) * 100).toInt() else 0

    val needsAttentionLeads = leads.filter { it.humanEscalated || it.priority == "HIGH" || it.status == "NEW" }.take(4)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Welcome Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good morning, Director",
                            color = TextPrimaryDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Here's what's happening with admissions today",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }

                    // AI Pulse Badge
                    Box(
                        modifier = Modifier
                            .background(if (profile.isAiActive) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, if (profile.isAiActive) SuccessGreen.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { viewModel.toggleAiStatus() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("ai_status_toggle_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (profile.isAiActive) SuccessGreen else WarningAmber, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (profile.isAiActive) "${profile.aiEmployeeName}: Active" else "AI Paused",
                                color = if (profile.isAiActive) SuccessGreen else WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Quick Action Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ActionChip(
                        icon = Icons.Default.GraphicEq,
                        label = "Live Voice Call",
                        onClick = onStartLiveVoice,
                        color = AiHighlight
                    )
                }
                item {
                    ActionChip(
                        icon = Icons.Default.Add,
                        label = "Add Lead",
                        onClick = onQuickAddLead,
                        color = PrimaryAccent
                    )
                }
                item {
                    ActionChip(
                        icon = Icons.Default.SmartToy,
                        label = "Test Aarohi",
                        onClick = { viewModel.navigateTo(AppScreen.AI_EMPLOYEE) },
                        color = AiHighlight
                    )
                }
                item {
                    ActionChip(
                        icon = Icons.Default.Event,
                        label = "Appointments (${appointments.size})",
                        onClick = { viewModel.navigateTo(AppScreen.APPOINTMENTS) },
                        color = SecondaryAccent
                    )
                }
                item {
                    ActionChip(
                        icon = Icons.Default.Task,
                        label = "Tasks (${tasks.size})",
                        onClick = { viewModel.navigateTo(AppScreen.FOLLOW_UPS) },
                        color = WarningAmber
                    )
                }
            }
        }

        // Live Voice Call Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xFF2E1065), Color(0xFF1E1B4B))
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .border(1.dp, PrimaryAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = AiHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aarohi Live Voice Admissions",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Real-time speech with gemini-3.8-live. Students & parents can ask questions and hear instantaneous voice answers.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onStartLiveVoice,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("dashboard_start_live_voice_button")
                    ) {
                        Text("Start Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Key Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Enquiries",
                        value = totalLeadsCount.toString(),
                        subtitle = "+24% this week",
                        icon = Icons.Default.People,
                        iconTint = PrimaryAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.LEADS) }
                    )
                    MetricCard(
                        title = "New Enquiries",
                        value = newLeadsCount.toString(),
                        subtitle = "Needs review",
                        icon = Icons.Default.ChatBubble,
                        iconTint = SecondaryAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.LEADS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Qualified Leads",
                        value = qualifiedCount.toString(),
                        subtitle = "High Intent",
                        icon = Icons.Default.Verified,
                        iconTint = SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.LEADS) }
                    )
                    MetricCard(
                        title = "Counselling Slots",
                        value = counsellingCount.toString(),
                        subtitle = "Active bookings",
                        icon = Icons.Default.CalendarToday,
                        iconTint = AiHighlight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.APPOINTMENTS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Overdue Tasks",
                        value = overdueTasksCount.toString(),
                        subtitle = if (overdueTasksCount > 0) "Immediate action" else "All caught up",
                        icon = Icons.Default.Warning,
                        iconTint = if (overdueTasksCount > 0) ErrorRed else SuccessGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.FOLLOW_UPS) }
                    )
                    MetricCard(
                        title = "Enrolment Rate",
                        value = "$conversionRate%",
                        subtitle = "$enrolledCount enrolled",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint = WarningAmber,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) }
                    )
                }
            }
        }

        // Priority Queue / "Needs Attention" Panel
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardElevated, RoundedCornerShape(14.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ErrorRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Priority Queue (Needs Attention)",
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "View All",
                        color = PrimaryAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.LEADS) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (needsAttentionLeads.isEmpty()) {
                    Text(
                        text = "No pending escalations or urgent follow-ups.",
                        color = TextMutedDark,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    needsAttentionLeads.forEach { lead ->
                        PriorityLeadRow(
                            lead = lead,
                            onClick = { viewModel.openLeadDetail(lead.id) },
                            onOpenChat = { viewModel.openInboxForLead(lead.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Weekly Enquiry Velocity Chart
        item {
            EnquiryTrendsChart()
        }

        // Admissions Conversion Funnel
        item {
            LeadFunnelCard(
                totalEnquiries = totalLeadsCount.coerceAtLeast(1),
                qualified = qualifiedCount,
                counselling = counsellingCount,
                enrolled = enrolledCount
            )
        }

        // Footer spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Surface(
        onClick = onClick,
        color = DarkCardElevated,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.height(38.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = TextPrimaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PriorityLeadRow(
    lead: Lead,
    onClick: () -> Unit,
    onOpenChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardSurface, RoundedCornerShape(10.dp))
            .border(1.dp, if (lead.humanEscalated) ErrorRed.copy(alpha = 0.5f) else DarkBorderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = lead.studentName,
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (lead.humanEscalated) {
                    Box(
                        modifier = Modifier
                            .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Human Escalation", color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                text = "${lead.courseInterest} • ${lead.source}",
                color = TextSecondaryDark,
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreBadge(score = lead.score)
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = onOpenChat,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Chat",
                    tint = PrimaryAccent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
