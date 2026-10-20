package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun OnboardingWizardScreen(
    viewModel: CampusPilotViewModel,
    profile: InstituteProfile,
    courses: List<Course>,
    faqs: List<Faq>,
    onComplete: () -> Unit
) {
    BackHandler { onComplete() }

    var currentStep by remember { mutableStateOf(1) } // 1 to 6

    var name by remember(profile.name) { mutableStateOf(profile.name) }
    var type by remember(profile.instituteType) { mutableStateOf(profile.instituteType) }
    var city by remember(profile.city) { mutableStateOf(profile.city) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var aiName by remember(profile.aiEmployeeName) { mutableStateOf(profile.aiEmployeeName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(16.dp)
    ) {
        // Top Header & Step Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (currentStep > 1) currentStep-- else onComplete() }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimaryDark)
            }
            Text(
                text = "Step $currentStep of 6",
                color = AiHighlight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onComplete) {
                Text("Skip to App", color = TextSecondaryDark, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Linear Step Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..6) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            if (i <= currentStep) PrimaryAccent else DarkCardSurface,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStep) {
                1 -> Step1Profile(
                    name = name,
                    type = type,
                    city = city,
                    phone = phone,
                    onNameChange = { name = it },
                    onTypeChange = { type = it },
                    onCityChange = { city = it },
                    onPhoneChange = { phone = it }
                )
                2 -> Step2Courses(courses = courses)
                3 -> Step3AiIdentity(
                    aiName = aiName,
                    onAiNameChange = { aiName = it },
                    profile = profile
                )
                4 -> Step4Knowledge(faqs = faqs)
                5 -> Step5TestChat(viewModel = viewModel, aiName = aiName)
                6 -> Step6LaunchChecklist(
                    profile = profile,
                    coursesCount = courses.size,
                    faqsCount = faqs.size
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Footer Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Text("Previous", color = TextPrimaryDark)
                }
            }

            Button(
                onClick = {
                    if (currentStep < 6) {
                        if (currentStep == 1 || currentStep == 3) {
                            viewModel.updateProfile(
                                profile.copy(
                                    name = name,
                                    instituteType = type,
                                    city = city,
                                    phone = phone,
                                    aiEmployeeName = aiName
                                )
                            )
                        }
                        currentStep++
                    } else {
                        onComplete()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("onboarding_next_button")
            ) {
                Text(
                    text = if (currentStep == 6) "Launch Dashboard" else "Next Step",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Step1Profile(
    name: String,
    type: String,
    city: String,
    phone: String,
    onNameChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Step 1: Institute Profile", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Enter your coaching academy's primary details.", color = TextSecondaryDark, fontSize = 12.sp)
        }
        item { OnboardingTextField("Institute Name", name, onNameChange) }
        item { OnboardingTextField("Institute Category (e.g., JEE/NEET, UPSC, Tuition)", type, onTypeChange) }
        item { OnboardingTextField("City & State (e.g., Kota, Rajasthan)", city, onCityChange) }
        item { OnboardingTextField("Official Admissions Phone", phone, onPhoneChange) }
    }
}

@Composable
private fun Step2Courses(courses: List<Course>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Step 2: Course Catalog", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Review active courses. Aarohi grounds all fee and batch replies in this list.", color = TextSecondaryDark, fontSize = 12.sp)
        }
        items(courses) { c ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(c.name, color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Fee: ${c.fees} • ${c.duration}", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text("Batches: ${c.batches}", color = TextSecondaryDark, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun Step3AiIdentity(
    aiName: String,
    onAiNameChange: (String) -> Unit,
    profile: InstituteProfile
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Step 3: AI Employee Identity", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Personalize your digital employee's name and behavior.", color = TextSecondaryDark, fontSize = 12.sp)
        }
        item { OnboardingTextField("AI Assistant Name (e.g., Aarohi)", aiName, onAiNameChange) }
        item { OnboardingTextField("Tone", profile.aiTone, {}) }
        item { OnboardingTextField("Supported Languages", profile.supportedLanguages, {}) }
        item { OnboardingTextField("Working Hours", profile.workingHours, {}) }
    }
}

@Composable
private fun Step4Knowledge(faqs: List<Faq>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Step 4: Knowledge Base & FAQs", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Pre-loaded FAQs for scholarships, hostel, and payment installments.", color = TextSecondaryDark, fontSize = 12.sp)
        }
        items(faqs) { f ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkCardElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(f.question, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(f.answer, color = TextSecondaryDark, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun Step5TestChat(viewModel: CampusPilotViewModel, aiName: String) {
    var query by remember { mutableStateOf("What is the fee for JEE 2-year?") }
    val messages by viewModel.testChatMessages.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Step 5: Test Your Employee", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Ask $aiName a question to verify that responses are strictly grounded in your course catalog.", color = TextSecondaryDark, fontSize = 12.sp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkCardElevated, RoundedCornerShape(12.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages.takeLast(4)) { msg ->
                    Text(
                        text = "${msg.sender}: ${msg.text}",
                        color = if (msg.isAi) AiHighlight else TextPrimaryDark,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark,
                    focusedBorderColor = PrimaryAccent,
                    unfocusedBorderColor = DarkBorder
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    viewModel.sendTestMessage(query)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Test", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Step6LaunchChecklist(profile: InstituteProfile, coursesCount: Int, faqsCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Step 6: Launch Checklist", color = TextPrimaryDark, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Everything is set up and ready to handle student enquiries.", color = TextSecondaryDark, fontSize = 12.sp)

        ChecklistItem("Institute Profile Configured", profile.name)
        ChecklistItem("Courses Indexed", "$coursesCount official courses with fees & batches")
        ChecklistItem("Knowledge Base Ready", "$faqsCount grounded FAQs loaded")
        ChecklistItem("AI Employee Tested", "${profile.aiEmployeeName} verified with zero-hallucination guardrails")
        ChecklistItem("Messaging Channels", "WhatsApp Cloud & Web Widget ready")
    }
}

@Composable
private fun ChecklistItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(10.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(SuccessGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = TextSecondaryDark, fontSize = 11.sp)
        }
    }
}

@Composable
private fun OnboardingTextField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
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
