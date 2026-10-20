package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.CampusRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    LANDING,
    ONBOARDING,
    DASHBOARD,
    LEADS,
    LEAD_DETAIL,
    AI_EMPLOYEE,
    INBOX,
    FOLLOW_UPS,
    APPOINTMENTS,
    KNOWLEDGE_BASE,
    ANALYTICS,
    SETTINGS
}

data class TestChatMessage(
    val sender: String, // "YOU", "AAROHI"
    val text: String,
    val isAi: Boolean = false,
    val groundingSource: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class CampusPilotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampusRepository
    private val dao = AppDatabase.getDatabase(application).dao()

    init {
        repository = CampusRepository(dao)
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedLeadId = MutableStateFlow<Long?>(null)
    val selectedLeadId: StateFlow<Long?> = _selectedLeadId.asStateFlow()

    private val _activeInboxLeadId = MutableStateFlow<Long?>(null)
    val activeInboxLeadId: StateFlow<Long?> = _activeInboxLeadId.asStateFlow()

    // Data Flows from Repository
    val profile: StateFlow<InstituteProfile> = repository.profileFlow
        .map { it ?: InstituteProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InstituteProfile())

    val courses: StateFlow<List<Course>> = repository.coursesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faqs: StateFlow<List<Faq>> = repository.faqsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leads: StateFlow<List<Lead>> = repository.leadsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<FollowUpTask>> = repository.tasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.appointmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationRules: StateFlow<List<AutomationRule>> = repository.automationRulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<ConversationMessage>> = repository.allMessagesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filters & Search
    val leadSearchQuery = MutableStateFlow("")
    val leadStatusFilter = MutableStateFlow<String?>("ALL")
    val leadPriorityFilter = MutableStateFlow<String?>("ALL")

    // UI Feedback
    val toastMessage = MutableStateFlow<String?>(null)
    val isAiThinking = MutableStateFlow(false)

    // AI Employee Test Simulator Messages
    private val _testChatMessages = MutableStateFlow<List<TestChatMessage>>(
        listOf(
            TestChatMessage(
                sender = "AAROHI",
                text = "Namaste! I am Aarohi, your AI Admissions Assistant for Apex Academy. You can test asking me about course fees, batches, eligibility, scholarship tests, or request to talk to a human counsellor.",
                isAi = true,
                groundingSource = "System Welcome"
            )
        )
    )
    val testChatMessages: StateFlow<List<TestChatMessage>> = _testChatMessages.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openLeadDetail(leadId: Long) {
        _selectedLeadId.value = leadId
        _currentScreen.value = AppScreen.LEAD_DETAIL
    }

    fun openInboxForLead(leadId: Long) {
        _activeInboxLeadId.value = leadId
        _currentScreen.value = AppScreen.INBOX
    }

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // Lead Actions
    fun createLead(
        name: String,
        phone: String,
        email: String,
        course: String,
        priority: String,
        source: String,
        notes: String
    ) {
        viewModelScope.launch {
            val (score, reasons) = GeminiAiService.calculateLeadScore(
                courseInterest = course,
                priority = priority,
                status = "NEW",
                hasPhone = phone.isNotBlank(),
                hasEmail = email.isNotBlank(),
                hasAppointments = false
            )

            val lead = Lead(
                studentName = name,
                phone = phone,
                email = email,
                courseInterest = course,
                status = "NEW",
                priority = priority,
                score = score,
                scoreReasons = reasons,
                source = source,
                assignedCounsellor = "Counsellor Neha",
                nextFollowUpDate = "Today, 04:00 PM",
                notes = notes,
                aiHandled = true
            )
            val newId = repository.addLead(lead)
            showToast("Lead '$name' added successfully (Score: $score)")
            openLeadDetail(newId)
        }
    }

    fun updateLeadStatus(lead: Lead, newStatus: String) {
        viewModelScope.launch {
            val updated = lead.copy(status = newStatus, updatedAt = System.currentTimeMillis())
            repository.updateLead(updated)
            showToast("Updated status to $newStatus")
        }
    }

    fun updateLeadPriority(lead: Lead, newPriority: String) {
        viewModelScope.launch {
            val updated = lead.copy(priority = newPriority, updatedAt = System.currentTimeMillis())
            repository.updateLead(updated)
            showToast("Updated priority to $newPriority")
        }
    }

    fun updateLeadNotes(lead: Lead, newNotes: String) {
        viewModelScope.launch {
            val updated = lead.copy(notes = newNotes, updatedAt = System.currentTimeMillis())
            repository.updateLead(updated)
            showToast("Notes saved")
        }
    }

    fun toggleAiStatus() {
        viewModelScope.launch {
            val current = profile.value
            val updated = current.copy(isAiActive = !current.isAiActive)
            repository.saveProfile(updated)
            val stateStr = if (updated.isAiActive) "Active" else "Paused"
            showToast("AI Employee is now $stateStr")
        }
    }

    fun updateProfile(updated: InstituteProfile) {
        viewModelScope.launch {
            repository.saveProfile(updated)
            showToast("Institute profile & AI identity saved")
        }
    }

    // Test Chat Simulator
    fun sendTestMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = TestChatMessage(sender = "YOU", text = text, isAi = false)
        _testChatMessages.value = _testChatMessages.value + userMsg
        isAiThinking.value = true

        viewModelScope.launch {
            val currentProfile = profile.value
            val currentCourses = courses.value
            val currentFaqs = faqs.value

            val result = GeminiAiService.answerEnquiry(
                studentQuery = text,
                profile = currentProfile,
                courses = currentCourses,
                faqs = currentFaqs,
                studentName = "Test Parent"
            )

            isAiThinking.value = false
            val aiMsg = TestChatMessage(
                sender = currentProfile.aiEmployeeName.uppercase(),
                text = result.replyText,
                isAi = true,
                groundingSource = result.groundingSource
            )
            _testChatMessages.value = _testChatMessages.value + aiMsg
        }
    }

    fun resetTestChat() {
        val name = profile.value.aiEmployeeName
        _testChatMessages.value = listOf(
            TestChatMessage(
                sender = name.uppercase(),
                text = "Namaste! Conversation reset. How can I assist you with admissions today?",
                isAi = true,
                groundingSource = "Session Reset"
            )
        )
    }

    // Send Real Message in Inbox
    fun sendInboxMessage(lead: Lead, messageText: String, isCounsellor: Boolean) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            val sender = if (isCounsellor) "COUNSELLOR" else "STUDENT"
            repository.addMessage(
                ConversationMessage(
                    leadId = lead.id,
                    leadName = lead.studentName,
                    channel = lead.source,
                    senderType = sender,
                    text = messageText,
                    timestamp = System.currentTimeMillis(),
                    isAiGenerated = false
                )
            )

            // If it came from student and AI is active and not humanEscalated, trigger AI reply
            if (!isCounsellor && profile.value.isAiActive && !lead.humanEscalated) {
                isAiThinking.value = true
                val result = GeminiAiService.answerEnquiry(
                    studentQuery = messageText,
                    profile = profile.value,
                    courses = courses.value,
                    faqs = faqs.value,
                    studentName = lead.studentName
                )
                isAiThinking.value = false

                repository.addMessage(
                    ConversationMessage(
                        leadId = lead.id,
                        leadName = lead.studentName,
                        channel = lead.source,
                        senderType = "AI",
                        text = result.replyText,
                        timestamp = System.currentTimeMillis() + 500,
                        isAiGenerated = true,
                        groundingSource = result.groundingSource
                    )
                )

                if (result.isEscalated) {
                    val updatedLead = lead.copy(humanEscalated = true, priority = "HIGH")
                    repository.updateLead(updatedLead)
                    showToast("Conversation escalated to Human Counsellor")
                }
            }
        }
    }

    fun toggleLeadEscalation(lead: Lead) {
        viewModelScope.launch {
            val updated = lead.copy(humanEscalated = !lead.humanEscalated)
            repository.updateLead(updated)
            val status = if (updated.humanEscalated) "Escalated to Human" else "Returned to AI"
            showToast(status)
        }
    }

    // Courses & FAQs
    fun createCourse(course: Course) {
        viewModelScope.launch {
            repository.addCourse(course)
            showToast("Course added: ${course.name}")
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            showToast("Course deleted")
        }
    }

    fun createFaq(faq: Faq) {
        viewModelScope.launch {
            repository.addFaq(faq)
            showToast("FAQ added: ${faq.question.take(25)}...")
        }
    }

    fun deleteFaq(faq: Faq) {
        viewModelScope.launch {
            repository.deleteFaq(faq)
            showToast("FAQ deleted")
        }
    }

    // Follow-ups & Appointments
    fun completeTask(task: FollowUpTask) {
        viewModelScope.launch {
            val updated = task.copy(status = "COMPLETED")
            repository.updateTask(updated)
            showToast("Task completed")
        }
    }

    fun createTask(leadId: Long, leadName: String, taskType: String, dueText: String, notes: String) {
        viewModelScope.launch {
            repository.addTask(
                FollowUpTask(
                    leadId = leadId,
                    leadName = leadName,
                    taskType = taskType,
                    dueText = dueText,
                    notes = notes
                )
            )
            showToast("Follow-up task scheduled")
        }
    }

    fun createAppointment(
        leadId: Long,
        studentName: String,
        counsellor: String,
        type: String,
        date: String,
        timeSlot: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addAppointment(
                Appointment(
                    leadId = leadId,
                    studentName = studentName,
                    counsellorName = counsellor,
                    appointmentType = type,
                    date = date,
                    timeSlot = timeSlot,
                    notes = notes
                )
            )
            showToast("Counselling session booked for $studentName")
        }
    }

    fun toggleAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            val updated = rule.copy(isActive = !rule.isActive)
            repository.updateAutomationRule(updated)
            showToast("Rule '${rule.name}' updated")
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
            showToast("All notifications marked as read")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            showToast("Demo dataset refreshed with 8 leads, 4 courses, and live chat")
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }
}
