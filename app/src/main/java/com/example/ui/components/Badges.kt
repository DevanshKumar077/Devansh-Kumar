package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LeadStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "NEW" -> Triple(Color(0xFF1E293B), Color(0xFF93C5FD), "New")
        "CONTACTED" -> Triple(Color(0xFF1E1B4B), Color(0xFFA5B4FC), "Contacted")
        "QUALIFIED" -> Triple(Color(0xFF064E3B), Color(0xFF6EE7B7), "Qualified")
        "COUNSELLING_SCHEDULED" -> Triple(Color(0xFF2E1065), Color(0xFFD8B4FE), "Counselling")
        "ENROLLED" -> Triple(Color(0xFF064E3B), Color(0xFF34D399), "Enrolled")
        "NOT_INTERESTED" -> Triple(Color(0xFF334155), Color(0xFF94A3B8), "Not Interested")
        "LOST" -> Triple(Color(0xFF450A0A), Color(0xFFFCA5A5), "Lost")
        else -> Triple(Color(0xFF1E293B), Color(0xFFCBD5E1), status)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (dotColor, label) = when (priority.uppercase()) {
        "HIGH" -> Pair(ErrorRed, "High")
        "MEDIUM" -> Pair(WarningAmber, "Medium")
        else -> Pair(SuccessGreen, "Normal")
    }

    Row(
        modifier = modifier
            .background(DarkCardSurface, RoundedCornerShape(6.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = " $label",
            color = TextSecondaryDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ScoreBadge(score: Int, modifier: Modifier = Modifier) {
    val color = when {
        score >= 80 -> SuccessGreen
        score >= 60 -> PrimaryAccent
        score >= 40 -> WarningAmber
        else -> ErrorRed
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Score: $score",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
