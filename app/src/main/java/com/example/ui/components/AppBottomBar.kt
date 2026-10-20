package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onOpenMoreMenu: () -> Unit
) {
    NavigationBar(
        containerColor = DarkBgSecondary,
        contentColor = TextPrimaryDark,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = DarkBorder)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = { onNavigate(AppScreen.DASHBOARD) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = "Overview",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Overview", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TextPrimaryDark,
                selectedTextColor = PrimaryAccent,
                indicatorColor = PrimaryAccent.copy(alpha = 0.25f),
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            ),
            modifier = Modifier.testTag("nav_overview")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.LEADS || currentScreen == AppScreen.LEAD_DETAIL,
            onClick = { onNavigate(AppScreen.LEADS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Leads",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Leads", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TextPrimaryDark,
                selectedTextColor = PrimaryAccent,
                indicatorColor = PrimaryAccent.copy(alpha = 0.25f),
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            ),
            modifier = Modifier.testTag("nav_leads")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.AI_EMPLOYEE,
            onClick = { onNavigate(AppScreen.AI_EMPLOYEE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI Employee",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("AI Aarohi", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TextPrimaryDark,
                selectedTextColor = PrimaryAccent,
                indicatorColor = PrimaryAccent.copy(alpha = 0.25f),
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            ),
            modifier = Modifier.testTag("nav_ai_employee")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.INBOX,
            onClick = { onNavigate(AppScreen.INBOX) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Inbox",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("Inbox", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TextPrimaryDark,
                selectedTextColor = PrimaryAccent,
                indicatorColor = PrimaryAccent.copy(alpha = 0.25f),
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            ),
            modifier = Modifier.testTag("nav_inbox")
        )

        val isMoreSelected = currentScreen in listOf(
            AppScreen.APPOINTMENTS,
            AppScreen.FOLLOW_UPS,
            AppScreen.KNOWLEDGE_BASE,
            AppScreen.ANALYTICS,
            AppScreen.SETTINGS,
            AppScreen.LANDING,
            AppScreen.ONBOARDING
        )

        NavigationBarItem(
            selected = isMoreSelected,
            onClick = { onOpenMoreMenu() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "More",
                    modifier = Modifier.size(20.dp)
                )
            },
            label = { Text("More", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TextPrimaryDark,
                selectedTextColor = PrimaryAccent,
                indicatorColor = PrimaryAccent.copy(alpha = 0.25f),
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark
            ),
            modifier = Modifier.testTag("nav_more")
        )
    }
}
