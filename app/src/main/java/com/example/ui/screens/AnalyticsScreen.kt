package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lead
import com.example.ui.components.EnquiryTrendsChart
import com.example.ui.components.LeadFunnelCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.CampusPilotViewModel

@Composable
fun AnalyticsScreen(
    viewModel: CampusPilotViewModel,
    leads: List<Lead>
) {
    val totalCount = leads.size
    val qualifiedCount = leads.count { it.status == "QUALIFIED" }
    val counsellingCount = leads.count { it.status == "COUNSELLING_SCHEDULED" }
    val enrolledCount = leads.count { it.status == "ENROLLED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgPrimary)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Admissions Analytics",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Data-driven enquiry intelligence",
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { viewModel.showToast("Exported ${leads.size} lead records to admissions_report.csv") },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryAccent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("export_csv_button")
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                EnquiryTrendsChart()
            }

            item {
                LeadFunnelCard(
                    totalEnquiries = totalCount.coerceAtLeast(1),
                    qualified = qualifiedCount,
                    counselling = counsellingCount,
                    enrolled = enrolledCount
                )
            }

            // Lead Source Distribution
            item {
                SourceDistributionCard(leads = leads)
            }

            // Counsellor Performance
            item {
                CounsellorPerformanceCard()
            }
        }
    }
}

@Composable
private fun SourceDistributionCard(leads: List<Lead>) {
    val whatsappCount = leads.count { it.source.equals("WhatsApp", true) }
    val widgetCount = leads.count { it.source.contains("Widget", true) }
    val walkinCount = leads.count { it.source.equals("Walk-in", true) }
    val otherCount = leads.size - (whatsappCount + widgetCount + walkinCount).coerceAtMost(leads.size)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text("Lead Channels & Attribution", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        SourceProgressRow("WhatsApp Business", whatsappCount, leads.size, SuccessGreen)
        Spacer(modifier = Modifier.height(8.dp))
        SourceProgressRow("Website AI Chat Widget", widgetCount, leads.size, PrimaryAccent)
        Spacer(modifier = Modifier.height(8.dp))
        SourceProgressRow("Campus Walk-in / Center", walkinCount, leads.size, WarningAmber)
        Spacer(modifier = Modifier.height(8.dp))
        SourceProgressRow("Instagram & Referrals", otherCount.coerceAtLeast(0), leads.size, SecondaryAccent)
    }
}

@Composable
private fun SourceProgressRow(name: String, count: Int, total: Int, color: Color) {
    val pct = if (total > 0) count.toFloat() / total else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, color = TextSecondaryDark, fontSize = 12.sp)
            Text("$count (${(pct * 100).toInt()}%)", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(DarkCardSurface, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = pct.coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
private fun CounsellorPerformanceCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCardElevated, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text("Admissions Counsellor Velocity", color = TextPrimaryDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Counsellor Neha", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Avg response: 4 mins • 88% qualified", color = SuccessGreen, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Counsellor Rajesh", color = TextPrimaryDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("Avg response: 6 mins • 92% qualified", color = SuccessGreen, fontSize = 11.sp)
        }
    }
}
