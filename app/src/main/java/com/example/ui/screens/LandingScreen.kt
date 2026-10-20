package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun LandingScreen(
    viewModel: CampusPilotViewModel,
    onEnterApp: () -> Unit
) {
    BackHandler { onEnterApp() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Brush.linearGradient(listOf(PrimaryAccent, SecondaryAccent)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CampusPilot AI",
                        color = TextPrimaryDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onEnterApp,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("landing_enter_app_button")
                ) {
                    Text("Dashboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Hero Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E1035), DarkCardElevated)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, PrimaryAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryAccent.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Built for Indian Coaching Institutes",
                            color = AiHighlight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Your AI Admissions Employee.\nAlways Ready.",
                        color = TextPrimaryDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Respond to student enquiries, qualify prospective students, automate follow-ups, and book counselling sessions—all from one intelligent workspace.",
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onEnterApp,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("landing_start_free_button")
                        ) {
                            Text("Start Free", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.resetDemoData()
                                onEnterApp()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("landing_explore_demo_button")
                        ) {
                            Text("Explore Demo", color = TextPrimaryDark, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Core Features Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Engineered for Coaching Academies",
                    color = TextPrimaryDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                FeatureItem(
                    icon = Icons.Default.Schedule,
                    title = "24/7 Enquiry Handling",
                    description = "Aarohi answers fee, batch, and eligibility questions instantly on WhatsApp and your website.",
                    color = PrimaryAccent
                )

                FeatureItem(
                    icon = Icons.Default.FilterList,
                    title = "Intelligent Lead Qualification",
                    description = "Discovers student target exam, grade, and timeline. Automatically calculates 0-100 lead score.",
                    color = SecondaryAccent
                )

                FeatureItem(
                    icon = Icons.Default.EventAvailable,
                    title = "Automated Follow-ups & Calendar",
                    description = "Schedules counsellor callbacks and reminders according to your institute's working hours.",
                    color = SuccessGreen
                )

                FeatureItem(
                    icon = Icons.Default.Security,
                    title = "Zero Hallucination Guardrails",
                    description = "Strictly grounded in your verified course catalog and FAQs. Escalates refund or discount requests to staff.",
                    color = WarningAmber
                )
            }
        }

        // How it works 3-step section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardElevated, RoundedCornerShape(14.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text("How CampusPilot AI Works", color = TextPrimaryDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                StepRow("1", "Connect Channels", "Link WhatsApp Business or drop our 1-line widget into your website.")
                Spacer(modifier = Modifier.height(10.dp))
                StepRow("2", "Upload Course & Fee FAQs", "Provide approved syllabus, batch times, and fee installment rules.")
                Spacer(modifier = Modifier.height(10.dp))
                StepRow("3", "Review Qualified Enrolments", "Let Aarohi pre-qualify students and book counselling slots automatically.")
            }
        }

        // Pricing Tiers
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Transparent Pricing for Institutes", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                PricingTierCard(
                    tier = "Starter",
                    price = "₹999 / mo",
                    features = listOf("1 Coaching Institute", "1 AI Employee (Aarohi)", "Up to 500 conversations/mo", "Basic lead CRM & reminders")
                )

                PricingTierCard(
                    tier = "Growth (Popular)",
                    price = "₹2,499 / mo",
                    features = listOf("Everything in Starter", "Unlimited AI conversations", "Full WhatsApp Business API", "Automations & calendar booking", "Detailed admissions analytics"),
                    isPopular = true
                )

                PricingTierCard(
                    tier = "Scale",
                    price = "Custom Enterprise",
                    features = listOf("Multi-branch Kota/Delhi franchises", "Dedicated account manager", "Custom ERP / CRM integrations")
                )
            }
        }

        // Footer
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CampusPilot AI • Admissions Operating System", color = TextMutedDark, fontSize = 11.sp)
                Text("Privacy Policy • Terms of Service • Support in India", color = TextSecondaryDark, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, title: String, description: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, color = TextSecondaryDark, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun StepRow(step: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(PrimaryAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(step, color = TextPrimaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = TextSecondaryDark, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PricingTierCard(tier: String, price: String, features: List<String>, isPopular: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (isPopular) PrimaryAccent else DarkBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tier, color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (isPopular) {
                    Box(
                        modifier = Modifier
                            .background(PrimaryAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("MOST POPULAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(price, color = AiHighlight, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)

            Spacer(modifier = Modifier.height(10.dp))

            features.forEach { feat ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(feat, color = TextSecondaryDark, fontSize = 11.sp)
                }
            }
        }
    }
}
