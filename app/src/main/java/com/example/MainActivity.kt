package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Lead
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.CampusPilotTheme
import com.example.ui.theme.DarkBgPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CampusPilotViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusPilotTheme(darkTheme = true) {
                CampusPilotApp()
            }
        }
    }
}

@Composable
fun CampusPilotApp(viewModel: CampusPilotViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val faqs by viewModel.faqs.collectAsStateWithLifecycle()
    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val allMessages by viewModel.allMessages.collectAsStateWithLifecycle()
    val testChatMessages by viewModel.testChatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val selectedLeadId by viewModel.selectedLeadId.collectAsStateWithLifecycle()
    val activeInboxLeadId by viewModel.activeInboxLeadId.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    // Dialog States
    var showAddLeadDialog by remember { mutableStateOf(false) }
    var showLiveVoiceDialog by remember { mutableStateOf(false) }
    var showAddAppointmentDialog by remember { mutableStateOf(false) }
    var appointmentTargetLead by remember { mutableStateOf<Lead?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskTargetLead by remember { mutableStateOf<Lead?>(null) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var showAddFaqDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    val unreadNotifications = notifications.count { !it.isRead }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
    ) {
        when (currentScreen) {
            AppScreen.LANDING -> {
                LandingScreen(
                    viewModel = viewModel,
                    onEnterApp = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            AppScreen.ONBOARDING -> {
                OnboardingWizardScreen(
                    viewModel = viewModel,
                    profile = profile,
                    courses = courses,
                    faqs = faqs,
                    onComplete = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                )
            }
            else -> {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        AppTopBar(
                            viewModel = viewModel,
                            profile = profile,
                            unreadNotificationsCount = unreadNotifications,
                            onOpenNotifications = { showNotificationsDialog = true },
                            onQuickAddLead = { showAddLeadDialog = true }
                        )
                    },
                    bottomBar = {
                        AppBottomBar(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenMoreMenu = { showMoreSheet = true }
                        )
                    },
                    containerColor = DarkBgPrimary
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.DASHBOARD -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    profile = profile,
                                    leads = leads,
                                    tasks = tasks,
                                    appointments = appointments,
                                    onQuickAddLead = { showAddLeadDialog = true }
                                )
                            }
                            AppScreen.LEADS -> {
                                LeadsScreen(
                                    viewModel = viewModel,
                                    leads = leads,
                                    onAddLeadClick = { showAddLeadDialog = true }
                                )
                            }
                            AppScreen.LEAD_DETAIL -> {
                                val selectedLead = leads.firstOrNull { it.id == selectedLeadId }
                                LeadDetailScreen(
                                    viewModel = viewModel,
                                    lead = selectedLead,
                                    onBack = { viewModel.navigateTo(AppScreen.LEADS) },
                                    onOpenInbox = { leadId -> viewModel.openInboxForLead(leadId) },
                                    onScheduleFollowUp = { lead ->
                                        taskTargetLead = lead
                                        showAddTaskDialog = true
                                    },
                                    onBookCounselling = { lead ->
                                        appointmentTargetLead = lead
                                        showAddAppointmentDialog = true
                                    }
                                )
                            }
                            AppScreen.AI_EMPLOYEE -> {
                                AiEmployeeScreen(
                                    viewModel = viewModel,
                                    profile = profile,
                                    courses = courses,
                                    faqs = faqs,
                                    testMessages = testChatMessages,
                                    isThinking = isAiThinking
                                )
                            }
                            AppScreen.INBOX -> {
                                InboxScreen(
                                    viewModel = viewModel,
                                    leads = leads,
                                    allMessages = allMessages,
                                    activeLeadId = activeInboxLeadId
                                )
                            }
                            AppScreen.APPOINTMENTS -> {
                                AppointmentsScreen(
                                    viewModel = viewModel,
                                    appointments = appointments,
                                    leads = leads,
                                    onAddAppointmentClick = {
                                        appointmentTargetLead = null
                                        showAddAppointmentDialog = true
                                    }
                                )
                            }
                            AppScreen.FOLLOW_UPS -> {
                                val rules by viewModel.automationRules.collectAsStateWithLifecycle()
                                FollowUpsScreen(
                                    viewModel = viewModel,
                                    tasks = tasks,
                                    rules = rules,
                                    onAddTaskClick = {
                                        taskTargetLead = null
                                        showAddTaskDialog = true
                                    }
                                )
                            }
                            AppScreen.KNOWLEDGE_BASE -> {
                                KnowledgeBaseScreen(
                                    viewModel = viewModel,
                                    courses = courses,
                                    faqs = faqs,
                                    onAddCourseClick = { showAddCourseDialog = true },
                                    onAddFaqClick = { showAddFaqDialog = true }
                                )
                            }
                            AppScreen.ANALYTICS -> {
                                AnalyticsScreen(
                                    viewModel = viewModel,
                                    leads = leads
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsAndIntegrationsScreen(
                                    viewModel = viewModel,
                                    profile = profile
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showAddLeadDialog) {
            AddLeadDialog(
                courses = courses,
                onDismiss = { showAddLeadDialog = false },
                onSubmit = { name, phone, email, course, priority, source, notes ->
                    viewModel.createLead(name, phone, email, course, priority, source, notes)
                }
            )
        }

        if (showAddAppointmentDialog) {
            AddAppointmentDialog(
                initialLead = appointmentTargetLead,
                onDismiss = { showAddAppointmentDialog = false },
                onSubmit = { leadId, name, counsellor, type, date, slot, notes ->
                    viewModel.createAppointment(leadId, name, counsellor, type, date, slot, notes)
                }
            )
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                initialLead = taskTargetLead,
                onDismiss = { showAddTaskDialog = false },
                onSubmit = { leadId, name, type, due, notes ->
                    viewModel.createTask(leadId, name, type, due, notes)
                }
            )
        }

        if (showAddCourseDialog) {
            AddCourseDialog(
                onDismiss = { showAddCourseDialog = false },
                onSubmit = { course ->
                    viewModel.createCourse(course)
                }
            )
        }

        if (showAddFaqDialog) {
            AddFaqDialog(
                onDismiss = { showAddFaqDialog = false },
                onSubmit = { faq ->
                    viewModel.createFaq(faq)
                }
            )
        }

        if (showNotificationsDialog) {
            NotificationsDialog(
                notifications = notifications,
                onDismiss = { showNotificationsDialog = false },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onNotificationClick = { leadId ->
                    if (leadId != null && leadId > 0) {
                        viewModel.openLeadDetail(leadId)
                    }
                }
            )
        }

        if (showMoreSheet) {
            MoreNavigationSheet(
                onDismiss = { showMoreSheet = false },
                onNavigate = { screen ->
                    viewModel.navigateTo(screen)
                }
            )
        }
    }
}
