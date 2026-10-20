package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.AutomationRule
import com.example.data.model.FollowUpTask
import com.example.ui.theme.*
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun FollowUpsScreen(
    viewModel: CampusPilotViewModel,
    tasks: List<FollowUpTask>,
    rules: List<AutomationRule>,
    onAddTaskClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tasks Queue, 1: Automation Rules Engine

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
                    text = "Follow-Up Automation",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scheduled tasks & automated lead journeys",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }

            if (selectedTab == 0) {
                Button(
                    onClick = onAddTaskClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_followup_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs (Tasks Queue vs Automation Rules)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkCardSurface,
            contentColor = PrimaryAccent,
            modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Task Queue (${tasks.count { it.status == "PENDING" }})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Automation Rules (${rules.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Tasks Queue
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("All follow-up tasks completed!", color = TextSecondaryDark, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onComplete = { viewModel.completeTask(task) }
                        )
                    }
                }
            }
        } else {
            // Automation Rules Engine List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    AutomationRuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleAutomationRule(rule) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskCardItem(
    task: FollowUpTask,
    onComplete: () -> Unit
) {
    val isPending = task.status == "PENDING"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (task.isOverdue && isPending) ErrorRed.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.leadName,
                        color = TextPrimaryDark,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (task.isOverdue && isPending) {
                        Box(
                            modifier = Modifier
                                .background(ErrorRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Overdue", color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = task.taskType,
                    color = AiHighlight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${task.dueText} • Assigned: ${task.assignedTo}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.notes,
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                }
            }

            if (isPending) {
                IconButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(SuccessGreen.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, SuccessGreen, CircleShape)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Complete", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(DarkCardSurface, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Done", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AutomationRuleCard(
    rule: AutomationRule,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rule.name,
                    color = TextPrimaryDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = rule.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryAccent,
                        uncheckedThumbColor = TextMutedDark,
                        uncheckedTrackColor = DarkBgSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            RuleStepRow("TRIGGER", rule.triggerEvent, PrimaryAccent)
            Spacer(modifier = Modifier.height(6.dp))
            RuleStepRow("CONDITION", rule.conditionText, WarningAmber)
            Spacer(modifier = Modifier.height(6.dp))
            RuleStepRow("ACTION", rule.actionText, SuccessGreen)
            Spacer(modifier = Modifier.height(6.dp))
            RuleStepRow("TIMING", rule.delayText, AiHighlight)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Fired ${rule.executionsCount} times",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun RuleStepRow(label: String, text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)
    }
}
