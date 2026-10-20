package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "institute_profile")
data class InstituteProfile(
    @PrimaryKey
    val id: Long = 1L,
    val name: String = "Apex IIT-JEE & Medical Academy",
    val instituteType: String = "JEE & NEET Academy",
    val city: String = "Kota",
    val state: String = "Rajasthan",
    val phone: String = "+91 98765 43210",
    val email: String = "admissions@apexiit.edu.in",
    val website: String = "https://apexiit.edu.in",
    val aiEmployeeName: String = "Aarohi",
    val aiTone: String = "Professional, Empathetic & Knowledgeable",
    val supportedLanguages: String = "English, Hindi, Hinglish",
    val workingHours: String = "08:30 AM - 08:00 PM IST",
    val outOfHoursBehavior: String = "Acknowledge enquiry, qualify student goal, schedule next morning counsellor callback",
    val escalationKeywords: String = "fee discount, refund, complaint, principal, scholarship concession, human counsellor, urgent",
    val isAiActive: Boolean = true,
    val isOnboardingCompleted: Boolean = true,
    val isDemoMode: Boolean = true
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetExam: String,
    val duration: String,
    val fees: String,
    val batches: String,
    val eligibility: String,
    val description: String,
    val isActive: Boolean = true
)

@Entity(tableName = "faqs")
data class Faq(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val question: String,
    val answer: String
)

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentName: String,
    val phone: String,
    val email: String = "",
    val courseInterest: String,
    val status: String = "NEW", // NEW, CONTACTED, QUALIFIED, COUNSELLING_SCHEDULED, ENROLLED, NOT_INTERESTED, LOST
    val priority: String = "MEDIUM", // HIGH, MEDIUM, NORMAL
    val score: Int = 50,
    val scoreReasons: String = "Standard enquiry",
    val source: String = "WhatsApp", // WhatsApp, Website Widget, Walk-in, Referral, Instagram
    val assignedCounsellor: String = "Counsellor Neha",
    val nextFollowUpDate: String = "Tomorrow, 11:00 AM",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val aiHandled: Boolean = true,
    val humanEscalated: Boolean = false
)

@Entity(tableName = "conversation_messages")
data class ConversationMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val leadId: Long,
    val leadName: String,
    val channel: String = "WhatsApp",
    val senderType: String, // STUDENT, AI, COUNSELLOR
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = false,
    val groundingSource: String = ""
)

@Entity(tableName = "follow_up_tasks")
data class FollowUpTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val leadId: Long,
    val leadName: String,
    val taskType: String, // Manual Callback, Counselling Reminder, Fee Confirmation, Batch Selection
    val dueText: String,
    val isOverdue: Boolean = false,
    val status: String = "PENDING", // PENDING, COMPLETED, CANCELLED
    val assignedTo: String = "Neha (Admissions)",
    val notes: String = ""
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val leadId: Long,
    val studentName: String,
    val counsellorName: String,
    val appointmentType: String = "1-on-1 In-Person Counselling",
    val date: String,
    val timeSlot: String,
    val status: String = "CONFIRMED", // CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED
    val notes: String = ""
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // LEAD, ESCALATION, APPOINTMENT, AUTOMATION
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedLeadId: Long? = null
)

@Entity(tableName = "automation_rules")
data class AutomationRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val triggerEvent: String,
    val conditionText: String,
    val actionText: String,
    val delayText: String,
    val isActive: Boolean = true,
    val executionsCount: Int = 0
)
