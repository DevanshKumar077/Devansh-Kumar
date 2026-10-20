package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppNotification
import com.example.data.model.Course
import com.example.data.model.Faq
import com.example.data.model.Lead
import com.example.ui.theme.*

@Composable
fun AddLeadDialog(
    courses: List<Course>,
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, email: String, course: String, priority: String, source: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+91 ") }
    var email by remember { mutableStateOf("") }
    var selectedCourse by remember { mutableStateOf(courses.firstOrNull()?.name ?: "JEE Advanced 2-Year") }
    var priority by remember { mutableStateOf("HIGH") }
    var source by remember { mutableStateOf("WhatsApp") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add New Student Enquiry",
                    color = TextPrimaryDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                if (errorMsg != null) {
                    Text(text = errorMsg!!, color = ErrorRed, fontSize = 11.sp)
                }

                DialogInputField("Student Name", name) { name = it }
                DialogInputField("Mobile Phone", phone) { phone = it }
                DialogInputField("Target Course", selectedCourse) { selectedCourse = it }

                // Priority Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("HIGH", "MEDIUM", "NORMAL").forEach { p ->
                        val isSelected = priority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) PrimaryAccent else DarkCardSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, if (isSelected) PrimaryAccent else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { priority = p }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                color = if (isSelected) Color.White else TextSecondaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                DialogInputField("Channel / Source (e.g. WhatsApp, Walk-in)", source) { source = it }
                DialogInputField("Counsellor Notes", notes) { notes = it }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || phone.length < 8) {
                                errorMsg = "Please provide valid student name and phone number."
                            } else {
                                onSubmit(name.trim(), phone.trim(), email.trim(), selectedCourse.trim(), priority, source.trim(), notes.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_add_lead_button")
                    ) {
                        Text("Add Lead", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAppointmentDialog(
    initialLead: Lead? = null,
    onDismiss: () -> Unit,
    onSubmit: (leadId: Long, name: String, counsellor: String, type: String, date: String, slot: String, notes: String) -> Unit
) {
    var studentName by remember { mutableStateOf(initialLead?.studentName ?: "") }
    var counsellor by remember { mutableStateOf("Er. Neha Tiwari") }
    var type by remember { mutableStateOf("1-on-1 Academic Counselling") }
    var date by remember { mutableStateOf("Tomorrow, 21 Oct") }
    var slot by remember { mutableStateOf("11:00 AM - 11:45 AM") }
    var notes by remember { mutableStateOf("Review target exam syllabus and scholarship concession") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Book Counselling Session", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                DialogInputField("Student Name", studentName) { studentName = it }
                DialogInputField("Assigned Counsellor", counsellor) { counsellor = it }
                DialogInputField("Session Type", type) { type = it }
                DialogInputField("Date", date) { date = it }
                DialogInputField("Time Slot", slot) { slot = it }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (studentName.isNotBlank()) {
                                onSubmit(initialLead?.id ?: 0L, studentName, counsellor, type, date, slot, notes)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Slot", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    initialLead: Lead? = null,
    onDismiss: () -> Unit,
    onSubmit: (leadId: Long, name: String, type: String, due: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialLead?.studentName ?: "Student") }
    var type by remember { mutableStateOf("Manual Callback") }
    var due by remember { mutableStateOf("Today, 04:30 PM") }
    var notes by remember { mutableStateOf("Follow-up on fee breakdown and parent consultation") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Schedule Follow-Up Task", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                DialogInputField("Student Name", name) { name = it }
                DialogInputField("Task Type", type) { type = it }
                DialogInputField("Due Time", due) { due = it }
                DialogInputField("Notes", notes) { notes = it }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmit(initialLead?.id ?: 0L, name, type, due, notes)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Create Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCourseDialog(
    onDismiss: () -> Unit,
    onSubmit: (Course) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var exam by remember { mutableStateOf("JEE / NEET") }
    var duration by remember { mutableStateOf("12 Months") }
    var fees by remember { mutableStateOf("₹95,000 / year") }
    var batches by remember { mutableStateOf("Morning (8:30 AM) & Evening (4:30 PM)") }
    var eligibility by remember { mutableStateOf("Class 11 / 12 students") }
    var desc by remember { mutableStateOf("Complete classroom guidance with test series") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add Approved Course", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                DialogInputField("Course Name", name) { name = it }
                DialogInputField("Target Exam", exam) { exam = it }
                DialogInputField("Official Fees", fees) { fees = it }
                DialogInputField("Duration", duration) { duration = it }
                DialogInputField("Batches", batches) { batches = it }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSubmit(
                                    Course(
                                        name = name,
                                        targetExam = exam,
                                        duration = duration,
                                        fees = fees,
                                        batches = batches,
                                        eligibility = eligibility,
                                        description = desc
                                    )
                                )
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Course", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddFaqDialog(
    onDismiss: () -> Unit,
    onSubmit: (Faq) -> Unit
) {
    var category by remember { mutableStateOf("Admissions") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add Approved FAQ", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                DialogInputField("Category (e.g. Fees, Hostel, Scholarships)", category) { category = it }
                DialogInputField("Student Question", question) { question = it }
                DialogInputField("Approved Answer", answer) { answer = it }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (question.isNotBlank() && answer.isNotBlank()) {
                                onSubmit(Faq(category = category, question = question, answer = answer))
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save FAQ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (Long?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Admissions Alerts", color = TextPrimaryDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all read", color = PrimaryAccent, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (notifications.isEmpty()) {
                    Text("No notifications.", color = TextMutedDark, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { notif ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (notif.isRead) DarkCardSurface else Color(0xFF1E1035), RoundedCornerShape(10.dp))
                                    .border(1.dp, if (notif.isRead) DarkBorder else PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        onDismiss()
                                        onNotificationClick(notif.relatedLeadId)
                                    }
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!notif.isRead) {
                                            Box(modifier = Modifier.size(6.dp).background(ErrorRed, CircleShape))
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(notif.title, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(notif.message, color = TextSecondaryDark, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreNavigationSheet(
    onDismiss: () -> Unit,
    onNavigate: (com.example.ui.viewmodel.AppScreen) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkBgSecondary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMutedDark) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "CampusPilot Modules",
                color = TextPrimaryDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            MoreSheetItem(Icons.Default.Event, "Counselling Calendar & Appointments", "Manage student booking slots") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.APPOINTMENTS)
            }

            MoreSheetItem(Icons.Default.Task, "Follow-Up Tasks & Automation Engine", "Scheduled callbacks & lead rules") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.FOLLOW_UPS)
            }

            MoreSheetItem(Icons.AutoMirrored.Filled.LibraryBooks, "Knowledge Base & Grounding", "Approved courses, fees & FAQs") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.KNOWLEDGE_BASE)
            }

            MoreSheetItem(Icons.Default.Analytics, "Admissions Analytics & CSV Export", "Funnel, sources & counsellor stats") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.ANALYTICS)
            }

            MoreSheetItem(Icons.Default.Settings, "Integrations & Institute Settings", "WhatsApp API, Web Widget & Profile") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.SETTINGS)
            }

            HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))

            MoreSheetItem(Icons.Default.Public, "Public Landing Page Showcase", "SaaS marketing view with pricing tiers") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.LANDING)
            }

            MoreSheetItem(Icons.Default.AutoFixHigh, "First-Time Onboarding Wizard", "6-step guided institute setup") {
                onDismiss()
                onNavigate(com.example.ui.viewmodel.AppScreen.ONBOARDING)
            }
        }
    }
}

@Composable
private fun MoreSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryAccent.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AiHighlight, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondaryDark, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DialogInputField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Text(label, color = TextSecondaryDark, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
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
