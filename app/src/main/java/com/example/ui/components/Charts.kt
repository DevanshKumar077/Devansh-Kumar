package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun EnquiryTrendsChart(
    modifier: Modifier = Modifier,
    dataPoints: List<Float> = listOf(14f, 22f, 18f, 35f, 28f, 42f, 48f),
    labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
) {
    Column(
        modifier = modifier
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Enquiry Velocity",
                    color = TextPrimaryDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "207 total enquiries this week (+24% vs last week)",
                    color = SuccessGreen,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .background(PrimaryAccent.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Last 7 Days", color = AiHighlight, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxVal = (dataPoints.maxOrNull() ?: 50f).coerceAtLeast(10f)
                val widthPerPoint = size.width / (dataPoints.size - 1).coerceAtLeast(1)

                val points = dataPoints.mapIndexed { index, value ->
                    val x = index * widthPerPoint
                    val y = size.height - (value / maxVal) * (size.height - 24.dp.toPx()) - 12.dp.toPx()
                    Offset(x, y)
                }

                // Fill Path
                val fillPath = Path().apply {
                    moveTo(0f, size.height)
                    points.forEachIndexed { i, pt ->
                        if (i == 0) lineTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                    lineTo(size.width, size.height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            PrimaryAccent.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                // Stroke Path
                val strokePath = Path().apply {
                    points.forEachIndexed { i, pt ->
                        if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = PrimaryAccent,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw Dots
                points.forEach { pt ->
                    drawCircle(
                        color = DarkBgPrimary,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = AiHighlight,
                        radius = 3.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(text = label, color = TextMutedDark, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun LeadFunnelCard(
    modifier: Modifier = Modifier,
    totalEnquiries: Int = 142,
    qualified: Int = 98,
    counselling: Int = 45,
    enrolled: Int = 24
) {
    Column(
        modifier = modifier
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Admissions Conversion Funnel",
            color = TextPrimaryDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "From raw enquiry to enrolled student",
            color = TextSecondaryDark,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        FunnelBarItem("1. Total Enquiries", totalEnquiries, 1.0f, PrimaryAccent)
        Spacer(modifier = Modifier.height(8.dp))
        FunnelBarItem("2. AI Qualified Leads", qualified, qualified.toFloat() / totalEnquiries.coerceAtLeast(1), SecondaryAccent)
        Spacer(modifier = Modifier.height(8.dp))
        FunnelBarItem("3. Counselling Sessions", counselling, counselling.toFloat() / totalEnquiries.coerceAtLeast(1), WarningAmber)
        Spacer(modifier = Modifier.height(8.dp))
        FunnelBarItem("4. Final Enrolments", enrolled, enrolled.toFloat() / totalEnquiries.coerceAtLeast(1), SuccessGreen)
    }
}

@Composable
private fun FunnelBarItem(
    label: String,
    count: Int,
    progress: Float,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondaryDark, fontSize = 12.sp)
            Text(
                text = "$count (${(progress * 100).toInt()}%)",
                color = TextPrimaryDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DarkCardSurface, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                    .fillMaxHeight()
                    .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}
