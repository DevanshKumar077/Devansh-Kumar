package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiAiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CampusPilot", appName)
    }

    @Test
    fun `lead score calculation produces accurate explainable score`() {
        val (score, reasons) = GeminiAiService.calculateLeadScore(
            courseInterest = "JEE Advanced 2-Year Premier Program",
            priority = "HIGH",
            status = "QUALIFIED",
            hasPhone = true,
            hasEmail = true,
            hasAppointments = true
        )
        assertTrue(score >= 80)
        assertTrue(reasons.contains("Target course identified"))
        assertTrue(reasons.contains("Verified contact phone"))
    }
}
