package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusPilotDao {

    // Institute Profile
    @Query("SELECT * FROM institute_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<InstituteProfile?>

    @Query("SELECT * FROM institute_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): InstituteProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: InstituteProfile)

    // Courses
    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun getCoursesFlow(): Flow<List<Course>>

    @Query("SELECT * FROM courses ORDER BY id ASC")
    suspend fun getCoursesSync(): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course): Long

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)

    // FAQs
    @Query("SELECT * FROM faqs ORDER BY id ASC")
    fun getFaqsFlow(): Flow<List<Faq>>

    @Query("SELECT * FROM faqs ORDER BY id ASC")
    suspend fun getFaqsSync(): List<Faq>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaq(faq: Faq): Long

    @Update
    suspend fun updateFaq(faq: Faq)

    @Delete
    suspend fun deleteFaq(faq: Faq)

    // Leads
    @Query("SELECT * FROM leads ORDER BY updatedAt DESC")
    fun getLeadsFlow(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :leadId LIMIT 1")
    fun getLeadByIdFlow(leadId: Long): Flow<Lead?>

    @Query("SELECT * FROM leads WHERE id = :leadId LIMIT 1")
    suspend fun getLeadByIdSync(leadId: Long): Lead?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead): Long

    @Update
    suspend fun updateLead(lead: Lead)

    @Delete
    suspend fun deleteLead(lead: Lead)

    // Conversation Messages
    @Query("SELECT * FROM conversation_messages WHERE leadId = :leadId ORDER BY timestamp ASC")
    fun getMessagesForLeadFlow(leadId: Long): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<ConversationMessage>>

    @Query("SELECT * FROM conversation_messages ORDER BY timestamp DESC")
    suspend fun getAllMessagesSync(): List<ConversationMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessage): Long

    // Follow-Up Tasks
    @Query("SELECT * FROM follow_up_tasks ORDER BY isOverdue DESC, id DESC")
    fun getTasksFlow(): Flow<List<FollowUpTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: FollowUpTask): Long

    @Update
    suspend fun updateTask(task: FollowUpTask)

    @Delete
    suspend fun deleteTask(task: FollowUpTask)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAppointmentsFlow(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotificationsFlow(): Flow<List<AppNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    // Automation Rules
    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    fun getAutomationRulesFlow(): Flow<List<AutomationRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationRule(rule: AutomationRule): Long

    @Update
    suspend fun updateAutomationRule(rule: AutomationRule)

    // Batch Clear For Demo Reset
    @Query("DELETE FROM leads")
    suspend fun clearLeads()

    @Query("DELETE FROM conversation_messages")
    suspend fun clearMessages()

    @Query("DELETE FROM follow_up_tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM appointments")
    suspend fun clearAppointments()

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()

    @Query("DELETE FROM courses")
    suspend fun clearCourses()

    @Query("DELETE FROM faqs")
    suspend fun clearFaqs()

    @Query("DELETE FROM automation_rules")
    suspend fun clearAutomationRules()
}
