package com.tribely.app.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tribely.app.feature.daily.DailyRollScreen
import com.tribely.app.feature.feed.FeedScreen
import com.tribely.app.feature.profile.ProfileScreen
import com.tribely.app.navigation.TribelyTabs

private data class TabItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun MainTabsScreen(
    onLoggedOut: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(TribelyTabs.DAILY) }
    var hideBottomBar by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            TabItem(TribelyTabs.DAILY, Icons.Filled.Casino, "Бросок"),
            TabItem(TribelyTabs.FEED, Icons.Filled.PhotoLibrary, "Лента"),
            TabItem(TribelyTabs.PROFILE, Icons.Filled.Person, "Профиль")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (selectedTab) {
            TribelyTabs.DAILY -> DailyRollScreen(
                onLoggedOut = onLoggedOut,
                onFullscreenChanged = { isOpen -> hideBottomBar = isOpen }
            )
            TribelyTabs.FEED -> FeedScreen()
            TribelyTabs.PROFILE -> ProfileScreen(onLoggedOut = onLoggedOut)
        }

        if (!hideBottomBar) {
            NavigationBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                containerColor = Color(0xFF0A0A0A),
                contentColor = MaterialTheme.colorScheme.onBackground,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.route },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
