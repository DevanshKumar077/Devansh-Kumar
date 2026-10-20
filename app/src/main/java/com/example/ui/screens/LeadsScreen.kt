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
import com.example.data.model.Lead
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun LeadsScreen(
    viewModel: CampusPilotViewModel,
    leads: List<Lead>,
    onAddLeadClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var selectedPriority by remember { mutableStateOf("ALL") }

    val statusFilters = listOf("ALL", "NEW", "CONTACTED", "QUALIFIED", "COUNSELLING_SCHEDULED", "ENROLLED", "LOST")
    val priorityFilters = listOf("ALL", "HIGH", "MEDIUM", "NORMAL")

    val filteredLeads = leads.filter { lead ->
        val matchesQuery = searchQuery.isBlank() ||
                lead.studentName.contains(searchQuery, ignoreCase = true) ||
                lead.phone.contains(searchQuery, ignoreCase = true) ||
                lead.courseInterest.contains(searchQuery, ignoreCase = true)

        val matchesStatus = selectedStatus == "ALL" || lead.status.equals(selectedStatus, ignoreCase = true)
        val matchesPriority = selectedPriority == "ALL" || lead.priority.equals(selectedPriority, ignoreCase = true)

        matchesQuery && matchesStatus && matchesPriority
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lead Management CRM",
                        color = TextPrimaryDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredLeads.size} leads matching filters",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onAddLeadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_lead_fab_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "New Lead",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Lead", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by student name, phone, course...", color = TextMutedDark, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMutedDark,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkCardElevated,
                    unfocusedContainerColor = DarkCardElevated,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("leads_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(statusFilters) { status ->
                    val isSelected = selectedStatus == status
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) PrimaryAccent else DarkCardSurface,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) PrimaryAccent else DarkBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedStatus = status }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (status == "COUNSELLING_SCHEDULED") "Counselling" else status.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (isSelected) Color.White else TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Leads List
            if (filteredLeads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No leads match current filter",
                            color = TextSecondaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Try clearing filters or add a new enquiry.",
                            color = TextMutedDark,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredLeads, key = { it.id }) { lead ->
                        LeadCardItem(
                            lead = lead,
                            onClick = { viewModel.openLeadDetail(lead.id) },
                            onChatClick = { viewModel.openInboxForLead(lead.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadCardItem(
    lead: Lead,
    onClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (lead.humanEscalated) ErrorRed.copy(alpha = 0.5f) else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(14.dp)
            .testTag("lead_card_${lead.id}")
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
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("Escalated", color = ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                ScoreBadge(score = lead.score)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = lead.courseInterest,
                color = AiHighlight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${lead.phone} • via ${lead.source}",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
                Text(
                    text = "Next: ${lead.nextFollowUpDate}",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LeadStatusBadge(status = lead.status)
                    PriorityBadge(priority = lead.priority)
                }

                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
