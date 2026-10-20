package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.InstituteProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun SettingsAndIntegrationsScreen(
    viewModel: CampusPilotViewModel,
    profile: InstituteProfile
) {
    var instituteName by remember(profile.name) { mutableStateOf(profile.name) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var city by remember(profile.city) { mutableStateOf(profile.city) }
    var aiName by remember(profile.aiEmployeeName) { mutableStateOf(profile.aiEmployeeName) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Integrations & Institute Settings",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure messaging channels, web widget, and institute identity",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }
        }

        // WhatsApp Business API Integration Card
        item {
            IntegrationCard(
                title = "WhatsApp Business Cloud Platform",
                status = "CONNECTED",
                statusColor = SuccessGreen,
                description = "Official Meta WhatsApp Business Cloud API. Automated enquiries, lead routing, and counsellor notifications.",
                details = listOf(
                    "Connected Phone: ${profile.phone}",
                    "Webhook: https://api.campuspilot.ai/v1/webhook/whatsapp",
                    "Tier: Tier 1 (1,000 conversations/day)"
                ),
                actionLabel = "Configure Templates",
                onAction = { viewModel.showToast("WhatsApp Cloud templates verified") }
            )
        }

        // Website AI Chat Widget Card
        item {
            IntegrationCard(
                title = "Website Embeddable Chat Widget",
                status = "ACTIVE",
                statusColor = PrimaryAccent,
                description = "Embed Aarohi on your institute's landing page or fee portal. Collects student leads 24/7.",
                details = listOf(
                    "Script: <script src=\"https://cdn.campuspilot.ai/widget.js\" data-institute=\"apex\"></script>",
                    "Allowed Domain: ${profile.website}"
                ),
                actionLabel = "Copy Widget Code",
                onAction = { viewModel.showToast("Widget snippet copied to clipboard!") }
            )
        }

        // Institute Profile Edit Card
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
                    Text("Institute Identity", color = TextPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = {
                            val updated = profile.copy(
                                name = instituteName,
                                phone = phone,
                                city = city,
                                aiEmployeeName = aiName
                            )
                            viewModel.updateProfile(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Save Changes", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SettingsInputField("Institute Name", instituteName) { instituteName = it }
                SettingsInputField("Admissions Contact Phone", phone) { phone = it }
                SettingsInputField("Campus City & State", city) { city = it }
                SettingsInputField("AI Admissions Assistant Name", aiName) { aiName = it }
            }
        }

        // Public Showcase & Onboarding Links
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardElevated, RoundedCornerShape(14.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text("Product Navigation", color = TextPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.LANDING) },
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text("Public Landing Page", fontSize = 11.sp, color = TextPrimaryDark)
                    }
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.ONBOARDING) },
                        modifier = Modifier.weight(1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text("Onboarding Wizard", fontSize = 11.sp, color = TextPrimaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.resetDemoData() },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_demo_data_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Demo Dataset to Default", color = WarningAmber, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun IntegrationCard(
    title: String,
    status: String,
    statusColor: Color,
    description: String,
    details: List<String>,
    actionLabel: String,
    onAction: () -> Unit
) {
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
            Text(title, color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(description, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)

        Spacer(modifier = Modifier.height(8.dp))

        details.forEach { d ->
            Text("• $d", color = TextMutedDark, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(actionLabel, color = TextPrimaryDark, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingsInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = TextSecondaryDark, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkCardSurface,
                unfocusedContainerColor = DarkCardSurface,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark
            )
        )
    }
}
