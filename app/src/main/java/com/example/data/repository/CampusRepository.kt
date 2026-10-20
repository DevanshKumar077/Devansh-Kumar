package com.example.data.repository

import com.example.data.local.CampusPilotDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CampusRepository(private val dao: CampusPilotDao) {

    val profileFlow: Flow<InstituteProfile?> = dao.getProfileFlow()
    val coursesFlow: Flow<List<Course>> = dao.getCoursesFlow()
    val faqsFlow: Flow<List<Faq>> = dao.getFaqsFlow()
    val leadsFlow: Flow<List<Lead>> = dao.getLeadsFlow()
    val tasksFlow: Flow<List<FollowUpTask>> = dao.getTasksFlow()
    val appointmentsFlow: Flow<List<Appointment>> = dao.getAppointmentsFlow()
    val notificationsFlow: Flow<List<AppNotification>> = dao.getNotificationsFlow()
    val automationRulesFlow: Flow<List<AutomationRule>> = dao.getAutomationRulesFlow()
    val allMessagesFlow: Flow<List<ConversationMessage>> = dao.getAllMessagesFlow()

    fun getMessagesForLead(leadId: Long): Flow<List<ConversationMessage>> =
        dao.getMessagesForLeadFlow(leadId)

    suspend fun getProfileSync(): InstituteProfile {
        return dao.getProfileSync() ?: InstituteProfile()
    }

    suspend fun saveProfile(profile: InstituteProfile) {
        dao.saveProfile(profile)
    }

    suspend fun addLead(lead: Lead): Long {
        val id = dao.insertLead(lead)
        // Auto-create initial enquiry message
        dao.insertMessage(
            ConversationMessage(
                leadId = id,
                leadName = lead.studentName,
                channel = lead.source,
                senderType = "STUDENT",
                text = "Hello, I am interested in ${lead.courseInterest}. Could you please share fee details and batch schedules?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 15
            )
        )
        // Auto notification
        dao.insertNotification(
            AppNotification(
                title = "New Lead: ${lead.studentName}",
                message = "Interested in ${lead.courseInterest} via ${lead.source}. Score: ${lead.score}",
                type = "LEAD",
                relatedLeadId = id
            )
        )
        return id
    }

    suspend fun updateLead(lead: Lead) {
        dao.updateLead(lead)
    }

    suspend fun deleteLead(lead: Lead) {
        dao.deleteLead(lead)
    }

    suspend fun addMessage(message: ConversationMessage): Long {
        return dao.insertMessage(message)
    }

    suspend fun addCourse(course: Course): Long = dao.insertCourse(course)
    suspend fun updateCourse(course: Course) = dao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = dao.deleteCourse(course)

    suspend fun addFaq(faq: Faq): Long = dao.insertFaq(faq)
    suspend fun updateFaq(faq: Faq) = dao.updateFaq(faq)
    suspend fun deleteFaq(faq: Faq) = dao.deleteFaq(faq)

    suspend fun addTask(task: FollowUpTask): Long = dao.insertTask(task)
    suspend fun updateTask(task: FollowUpTask) = dao.updateTask(task)
    suspend fun deleteTask(task: FollowUpTask) = dao.deleteTask(task)

    suspend fun addAppointment(appointment: Appointment): Long {
        val id = dao.insertAppointment(appointment)
        dao.insertNotification(
            AppNotification(
                title = "Counselling Booked: ${appointment.studentName}",
                message = "${appointment.date} at ${appointment.timeSlot} with ${appointment.counsellorName}",
                type = "APPOINTMENT",
                relatedLeadId = appointment.leadId
            )
        )
        return id
    }
    suspend fun updateAppointment(appointment: Appointment) = dao.updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = dao.deleteAppointment(appointment)

    suspend fun markNotificationRead(id: Long) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()

    suspend fun updateAutomationRule(rule: AutomationRule) = dao.updateAutomationRule(rule)

    suspend fun ensureInitialized() {
        val existing = dao.getProfileSync()
        if (existing == null) {
            seedDemoData()
        }
    }

    suspend fun resetDemoData() {
        dao.clearLeads()
        dao.clearMessages()
        dao.clearTasks()
        dao.clearAppointments()
        dao.clearNotifications()
        dao.clearCourses()
        dao.clearFaqs()
        dao.clearAutomationRules()
        seedDemoData()
    }

    private suspend fun seedDemoData() {
        // 1. Institute Profile
        val profile = InstituteProfile(
            id = 1L,
            name = "Apex IIT-JEE & Medical Academy",
            instituteType = "Premier Engineering & Medical Coaching",
            city = "Kota",
            state = "Rajasthan",
            phone = "+91 98765 43210",
            email = "admissions@apexiit.edu.in",
            website = "https://apexiit.edu.in",
            aiEmployeeName = "Aarohi",
            aiTone = "Professional, Empathetic & Knowledgeable",
            supportedLanguages = "English, Hindi, Hinglish",
            workingHours = "08:30 AM - 08:30 PM IST",
            outOfHoursBehavior = "Collect enquiry, qualify learning goal, schedule next morning callback",
            escalationKeywords = "discount, refund, complaint, talk to counsellor, principal, scholarship exception, fee reduction",
            isAiActive = true,
            isOnboardingCompleted = true,
            isDemoMode = true
        )
        dao.saveProfile(profile)

        // 2. Courses
        val courses = listOf(
            Course(
                name = "JEE Advanced 2-Year Premier Program",
                targetExam = "JEE Main & Advanced 2027",
                duration = "24 Months (Class 11 + 12)",
                fees = "₹1,45,000 / year (Inclusive of GST)",
                batches = "Morning Batch (08:00 AM) & Evening Batch (04:30 PM)",
                eligibility = "Minimum 75% in Class 10 Science & Maths or Apex Scholarship Test",
                description = "Comprehensive classroom coaching covering Physics, Chemistry, and Mathematics with daily practice problems, mock tests, and top faculty mentorship."
            ),
            Course(
                name = "NEET Target / Dropper Batch 2026",
                targetExam = "NEET UG 2026",
                duration = "12 Months Intensive",
                fees = "₹1,25,000 (Full Year)",
                batches = "Full-day Intensive (08:30 AM - 02:30 PM)",
                eligibility = "Passed Class 12 with PCB subjects",
                description = "Dedicated dropper program with NCERT line-by-line mastery, high-yield biology modules, daily doubt clearance, and 45+ full-length mock examinations."
            ),
            Course(
                name = "Foundation & Olympiad Masterclass (Class 9-10)",
                targetExam = "NTSE, IOQM, NSEJS & School Boards",
                duration = "10 Months",
                fees = "₹65,000 / year",
                batches = "Weekend Batch (Sat-Sun) or After-School (05:00 PM)",
                eligibility = "School students moving into Class 9 or 10",
                description = "Builds strong scientific intuition and problem-solving aptitude for national-level talent searches and competitive exams early."
            ),
            Course(
                name = "MHT-CET & State Board Fast-Track Crash Course",
                targetExam = "MHT-CET 2026",
                duration = "4 Months",
                fees = "₹35,000",
                batches = "Evening Online + Offline Hybrid",
                eligibility = "Class 12 appearing or passed",
                description = "Rapid high-yield formula revision, chapter-wise previous 10-year question solving, and speed optimization tests."
            )
        )
        courses.forEach { dao.insertCourse(it) }

        // 3. FAQs
        val faqs = listOf(
            Faq(
                category = "Scholarships",
                question = "Are there any scholarships or admission fee concessions?",
                answer = "Yes! Students can appear for the Apex Talent Hunt Exam (ATHE) conducted every Sunday. Depending on their score, up to 90% tuition fee waiver is awarded. Direct 25% scholarship is available for state board top 1% scorers."
            ),
            Faq(
                category = "Hostel & Facilities",
                question = "Does the academy provide hostel and transport facilities in Kota?",
                answer = "We have affiliated secure AC and non-AC student residencies with hygienic mess facilities, biometric security, and dedicated evening study halls within 500m of the main academic campus."
            ),
            Faq(
                category = "Fee Payment",
                question = "Can course fees be paid in monthly or quarterly installments?",
                answer = "Yes, we provide flexible 3-installment payment schedules without any interest charges. Zero-cost education EMI options through partner banks are also available upon request."
            ),
            Faq(
                category = "Faculty & Mentorship",
                question = "Who are the teachers and faculty members?",
                answer = "Our faculty team comprises ex-IITians, seasoned NIT graduates, and MBBS doctors with 8+ years of dedicated coaching experience and top 100 AIR track records."
            ),
            Faq(
                category = "Refund Policy",
                question = "What is the fee refund policy if a student discontinues?",
                answer = "Refund requests submitted within 14 days of batch commencement are processed with only nominal administrative deductions as per standard government coaching guidelines. Please consult the admissions office for detailed terms."
            ),
            Faq(
                category = "Demo Classes",
                question = "Can prospective students attend demo classes before admission?",
                answer = "Yes, we offer 2 complimentary trial lectures and a comprehensive academic counselling session for both the student and parents before formal registration."
            )
        )
        faqs.forEach { dao.insertFaq(it) }

        // 4. Leads
        val leads = listOf(
            Lead(
                studentName = "Aarav Sharma",
                phone = "+91 98230 11223",
                email = "aarav.sharma@gmail.com",
                courseInterest = "JEE Advanced 2-Year Premier Program",
                status = "QUALIFIED",
                priority = "HIGH",
                score = 88,
                scoreReasons = "Class 11 aspirant (+25) • High budget confirmed (+20) • Requested syllabus brochure (+20) • Phone verified (+23)",
                source = "WhatsApp",
                assignedCounsellor = "Counsellor Neha",
                nextFollowUpDate = "Today, 03:30 PM",
                notes = "Parent wants Kota hostel accommodation details. Good 10th ICSE score (94%).",
                aiHandled = true,
                humanEscalated = false
            ),
            Lead(
                studentName = "Priya Verma",
                phone = "+91 97120 44556",
                email = "priya.verma2007@gmail.com",
                courseInterest = "NEET Target / Dropper Batch 2026",
                status = "COUNSELLING_SCHEDULED",
                priority = "HIGH",
                score = 94,
                scoreReasons = "Repeater dropper (+30) • Immediate joining (+25) • Counselling slot booked (+25) • High intent (+14)",
                source = "Website Widget",
                assignedCounsellor = "Counsellor Rajesh",
                nextFollowUpDate = "Tomorrow, 11:00 AM",
                notes = "Scored 545 in NEET last attempt. Focused on improving Physics speed.",
                aiHandled = true,
                humanEscalated = false
            ),
            Lead(
                studentName = "Rohan Gupta",
                phone = "+91 98450 77889",
                email = "gupta.rohan@rediffmail.com",
                courseInterest = "JEE Advanced 2-Year Premier Program",
                status = "CONTACTED",
                priority = "MEDIUM",
                score = 65,
                scoreReasons = "JEE Main target (+20) • Waiting for school finals (+15) • Responsive (+30)",
                source = "WhatsApp",
                assignedCounsellor = "Counsellor Neha",
                nextFollowUpDate = "22 Oct, 10:00 AM",
                notes = "Sent fee breakdown PDF. Follow-up after father reviews.",
                aiHandled = true,
                humanEscalated = false
            ),
            Lead(
                studentName = "Ananya Deshmukh",
                phone = "+91 91580 33221",
                email = "ananya.d@gmail.com",
                courseInterest = "Foundation & Olympiad Masterclass (Class 9-10)",
                status = "NEW",
                priority = "MEDIUM",
                score = 52,
                scoreReasons = "Enquiry received via Instagram ad (+30) • Looking for weekend batch (+22)",
                source = "Instagram",
                assignedCounsellor = "Unassigned",
                nextFollowUpDate = "Today, 05:00 PM",
                notes = "Mother enquired regarding IOQM mathematics olympiad batch.",
                aiHandled = true,
                humanEscalated = false
            ),
            Lead(
                studentName = "Devendra Yadav",
                phone = "+91 99340 88112",
                email = "dev.yadav99@gmail.com",
                courseInterest = "NEET Target / Dropper Batch 2026",
                status = "ENROLLED",
                priority = "HIGH",
                score = 100,
                scoreReasons = "Admission completed (+50) • 1st Installment received (+30) • Registered (+20)",
                source = "Walk-in",
                assignedCounsellor = "Counsellor Rajesh",
                nextFollowUpDate = "Completed",
                notes = "Enrolled in Batch Alpha. ID Card and study kit issued.",
                aiHandled = false,
                humanEscalated = false
            ),
            Lead(
                studentName = "Simran Kaur",
                phone = "+91 98720 99443",
                email = "simran.k@yahoo.com",
                courseInterest = "JEE Advanced 2-Year Premier Program",
                status = "NEW",
                priority = "HIGH",
                score = 78,
                scoreReasons = "Asked about 50% fee scholarship discount (+35) • Flagged for human review (+43)",
                source = "WhatsApp",
                assignedCounsellor = "Counsellor Neha",
                nextFollowUpDate = "Immediate",
                notes = "Student specifically requested fee concession. Aarohi triggered human escalation.",
                aiHandled = true,
                humanEscalated = true
            ),
            Lead(
                studentName = "Vikramaditya Rathore",
                phone = "+91 94140 22334",
                email = "vikram.rathore@outlook.com",
                courseInterest = "MHT-CET & State Board Fast-Track Crash Course",
                status = "QUALIFIED",
                priority = "NORMAL",
                score = 58,
                scoreReasons = "State board student (+25) • Evening batch preference (+20) • Price conscious (+13)",
                source = "Referral",
                assignedCounsellor = "Counsellor Rajesh",
                nextFollowUpDate = "23 Oct, 04:00 PM",
                notes = "Interested in online test series plus revision lectures.",
                aiHandled = true,
                humanEscalated = false
            ),
            Lead(
                studentName = "Kavita Meena",
                phone = "+91 94600 55667",
                email = "",
                courseInterest = "NEET Target / Dropper Batch 2026",
                status = "CONTACTED",
                priority = "MEDIUM",
                score = 62,
                scoreReasons = "NEET aspirant (+30) • Inquired about hostel mess (+20) • Verified (+12)",
                source = "Walk-in",
                assignedCounsellor = "Counsellor Neha",
                nextFollowUpDate = "Tomorrow, 02:00 PM",
                notes = "Visited campus with elder brother. Requested hostel tour on Friday.",
                aiHandled = true,
                humanEscalated = false
            )
        )

        val insertedLeadIds = mutableListOf<Long>()
        leads.forEach {
            val id = dao.insertLead(it)
            insertedLeadIds.add(id)
        }

        // 5. Conversation Messages
        val leadAaravId = insertedLeadIds.getOrNull(0) ?: 1L
        val leadPriyaId = insertedLeadIds.getOrNull(1) ?: 2L
        val leadSimranId = insertedLeadIds.getOrNull(5) ?: 6L

        dao.insertMessage(
            ConversationMessage(
                leadId = leadAaravId,
                leadName = "Aarav Sharma",
                senderType = "STUDENT",
                text = "Hello, what are the fees and batch timings for 11th JEE 2-year classroom course in Kota?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120
            )
        )
        dao.insertMessage(
            ConversationMessage(
                leadId = leadAaravId,
                leadName = "Aarav Sharma",
                senderType = "AI",
                text = "Namaste Aarav! For our JEE Advanced 2-Year Premier Program (Class 11 + 12), the fee is ₹1,45,000 / year. Batches are available in Morning (08:00 AM) and Evening (04:30 PM). We also have scholarship tests (ATHE) every Sunday for up to 90% fee waiver. Would you like to schedule a free demo session?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 118,
                isAiGenerated = true,
                groundingSource = "Course Catalog & FAQ [Scholarships]"
            )
        )
        dao.insertMessage(
            ConversationMessage(
                leadId = leadAaravId,
                leadName = "Aarav Sharma",
                senderType = "STUDENT",
                text = "Yes, can I book a demo class for this Saturday?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60
            )
        )
        dao.insertMessage(
            ConversationMessage(
                leadId = leadAaravId,
                leadName = "Aarav Sharma",
                senderType = "AI",
                text = "Certainly! I have reserved a provisional demo class slot for Saturday at 10:00 AM at our Kota main campus. Our counsellor Neha will confirm the classroom details with your father shortly.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 59,
                isAiGenerated = true,
                groundingSource = "Demo Class Booking Engine"
            )
        )

        // Priya thread
        dao.insertMessage(
            ConversationMessage(
                leadId = leadPriyaId,
                leadName = "Priya Verma",
                senderType = "STUDENT",
                text = "Hi Aarohi, I took NEET 2025 and got 545. Do you have a dedicated dropper batch?",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 90
            )
        )
        dao.insertMessage(
            ConversationMessage(
                leadId = leadPriyaId,
                leadName = "Priya Verma",
                senderType = "AI",
                text = "Namaste Priya! Excellent effort in your first attempt. Yes, our NEET Target / Dropper Batch 2026 is tailored specifically for repeaters aiming for 680+ marks. Full-day intensive schedule runs 08:30 AM to 02:30 PM with daily doubt clinics.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 88,
                isAiGenerated = true,
                groundingSource = "Course Catalog: NEET Dropper Batch"
            )
        )

        // Simran Escalated Thread
        dao.insertMessage(
            ConversationMessage(
                leadId = leadSimranId,
                leadName = "Simran Kaur",
                senderType = "STUDENT",
                text = "Can I get a direct 40% discount on the JEE batch? My father is an ex-serviceman.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 20
            )
        )
        dao.insertMessage(
            ConversationMessage(
                leadId = leadSimranId,
                leadName = "Simran Kaur",
                senderType = "AI",
                text = "Namaste Simran. We deeply honour our Armed Forces personnel. For special defence and hardship concessions, this requires approval from our Admissions Director. I have escalated this conversation to Senior Counsellor Neha who will call you shortly.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 19,
                isAiGenerated = true,
                groundingSource = "Human Escalation: Concession Request"
            )
        )

        // 6. Follow-up Tasks
        val tasks = listOf(
            FollowUpTask(
                leadId = leadSimranId,
                leadName = "Simran Kaur",
                taskType = "Special Concession Callback",
                dueText = "Overdue (30 min ago)",
                isOverdue = true,
                status = "PENDING",
                assignedTo = "Neha (Admissions)",
                notes = "Review defence certificate for 25-30% fee concession approval."
            ),
            FollowUpTask(
                leadId = leadAaravId,
                leadName = "Aarav Sharma",
                taskType = "Hostel & Transport Brochure",
                dueText = "Today, 03:30 PM",
                isOverdue = false,
                status = "PENDING",
                assignedTo = "Neha (Admissions)",
                notes = "Share residency video tour and fee breakdown on WhatsApp."
            ),
            FollowUpTask(
                leadId = leadPriyaId,
                leadName = "Priya Verma",
                taskType = "Counselling Preparation Sheet",
                dueText = "Tomorrow, 10:30 AM",
                isOverdue = false,
                status = "PENDING",
                assignedTo = "Rajesh (NEET Head)",
                notes = "Prepare gap analysis of 2025 physics score."
            ),
            FollowUpTask(
                leadId = insertedLeadIds.getOrNull(2) ?: 3L,
                leadName = "Rohan Gupta",
                taskType = "Fee Schedule Follow-up",
                dueText = "22 Oct, 11:00 AM",
                isOverdue = false,
                status = "PENDING",
                assignedTo = "Neha (Admissions)",
                notes = "Check if father has decided on 3-installment vs one-time payment."
            )
        )
        tasks.forEach { dao.insertTask(it) }

        // 7. Appointments
        val appointments = listOf(
            Appointment(
                leadId = leadPriyaId,
                studentName = "Priya Verma",
                counsellorName = "Dr. Rajeshwar Singh (NEET Head)",
                appointmentType = "1-on-1 Academic Counselling",
                date = "21 Oct 2026",
                timeSlot = "11:00 AM - 11:45 AM",
                status = "CONFIRMED",
                notes = "Room 204, Academic Block. Parent accompanying."
            ),
            Appointment(
                leadId = leadAaravId,
                studentName = "Aarav Sharma",
                counsellorName = "Er. Neha Tiwari",
                appointmentType = "Campus & Hostel Tour",
                date = "24 Oct 2026",
                timeSlot = "10:00 AM - 11:00 AM",
                status = "CONFIRMED",
                notes = "Demo lecture in Batch 11-A followed by hostel inspection."
            )
        )
        appointments.forEach { dao.insertAppointment(it) }

        // 8. Notifications
        val notifications = listOf(
            AppNotification(
                title = "Immediate Escalation: Simran Kaur",
                message = "Student requested defence fee concession. Human intervention required.",
                type = "ESCALATION",
                isRead = false,
                relatedLeadId = leadSimranId
            ),
            AppNotification(
                title = "High Score Lead Qualified (94%)",
                message = "Priya Verma completed automated qualifying questions for NEET Dropper.",
                type = "LEAD",
                isRead = false,
                relatedLeadId = leadPriyaId
            ),
            AppNotification(
                title = "Counselling Session Confirmed",
                message = "Priya Verma booked for tomorrow at 11:00 AM with Dr. Rajeshwar.",
                type = "APPOINTMENT",
                isRead = true,
                relatedLeadId = leadPriyaId
            )
        )
        notifications.forEach { dao.insertNotification(it) }

        // 9. Automation Rules
        val rules = listOf(
            AutomationRule(
                name = "Instant WhatsApp Welcome & Intent Qualification",
                triggerEvent = "WHEN new enquiry arrives via WhatsApp or Web Widget",
                conditionText = "IF AI Employee is Active AND Enquiry within or outside hours",
                actionText = "THEN Aarohi sends grounded welcome message & identifies target exam",
                delayText = "Immediate (under 10 seconds)",
                isActive = true,
                executionsCount = 142
            ),
            AutomationRule(
                name = "Lead Scoring & Priority Assignment",
                triggerEvent = "WHEN student indicates target batch and joining timeline",
                conditionText = "IF joining timeline <= 30 days OR marks >= 80%",
                actionText = "THEN set Lead Priority to HIGH and score >= 75",
                delayText = "Instant",
                isActive = true,
                executionsCount = 98
            ),
            AutomationRule(
                name = "24-Hour Inactive Enquiry Follow-up",
                triggerEvent = "WHEN student does not reply to fee information",
                conditionText = "IF Lead Status is NEW or CONTACTED AND no callback booked",
                actionText = "THEN send gentle check-in message with syllabus breakdown PDF link",
                delayText = "After 24 hours",
                isActive = true,
                executionsCount = 57
            ),
            AutomationRule(
                name = "Pre-Counselling SMS / WhatsApp Reminder",
                triggerEvent = "WHEN counselling appointment is confirmed",
                conditionText = "IF appointment status is CONFIRMED",
                actionText = "THEN send location map pin and counsellor contact card",
                delayText = "2 hours prior to slot",
                isActive = true,
                executionsCount = 34
            )
        )
        rules.forEach { dao.insertAutomationRule(it) }
    }
}
