package com.tribely.app.feature.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tribely.app.feature.battles.BattleDetailScreen
import com.tribely.app.feature.battles.BattlesScreen
import com.tribely.app.feature.battles.CreateBattleScreen
import com.tribely.app.feature.chat.ChatListScreen
import com.tribely.app.feature.chat.ChatScreen
import com.tribely.app.feature.daily.DailyRollScreen
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
    var showCreateBattle by rememberSaveable { mutableStateOf(false) }
    var openBattleId by rememberSaveable { mutableStateOf<String?>(null) }
    var openChatGroupId by rememberSaveable { mutableStateOf<String?>(null) }

    val tabs = remember {
        listOf(
            TabItem(TribelyTabs.DAILY, Icons.Filled.PhotoLibrary, "Лента"),
            TabItem(TribelyTabs.CHALLENGES, Icons.Filled.Add, "Челленджи"),
            TabItem(TribelyTabs.CHAT, Icons.AutoMirrored.Filled.Chat, "Чат"),
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
            TribelyTabs.QUESTS -> PlaceholderTabScreen(
                title = "Квесты",
                subtitle = "Скоро здесь появятся задания для группы"
            )
            TribelyTabs.CHALLENGES -> {
                when {
                    showCreateBattle -> {
                        CreateBattleScreen(
                            onBack = { showCreateBattle = false },
                            onCreated = { battleId ->
                                showCreateBattle = false
                                openBattleId = battleId
                            }
                        )
                    }
                    openBattleId != null -> {
                        BattleDetailScreen(
                            battleId = openBattleId.orEmpty(),
                            onBack = { openBattleId = null }
                        )
                    }
                    else -> {
                        BattlesScreen(
                            onCreateClick = { showCreateBattle = true },
                            onBattleClick = { battleId -> openBattleId = battleId }
                        )
                    }
                }
            }
            TribelyTabs.FRIENDS -> PlaceholderTabScreen(
                title = "Друзья",
                subtitle = "Скоро здесь появится список друзей"
            )
            TribelyTabs.CHAT -> {
                if (openChatGroupId != null) {
                    ChatScreen(
                        groupId = openChatGroupId.orEmpty(),
                        onBack = { openChatGroupId = null }
                    )
                } else {
                    ChatListScreen(
                        onChatClick = { groupId -> openChatGroupId = groupId }
                    )
                }
            }
            TribelyTabs.PROFILE -> ProfileScreen(onLoggedOut = onLoggedOut)
        }

        if (
            !hideBottomBar &&
            !(selectedTab == TribelyTabs.CHALLENGES && (showCreateBattle || openBattleId != null)) &&
            !(selectedTab == TribelyTabs.CHAT && openChatGroupId != null)
        ) {
            FloatingBottomBar(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun FloatingBottomBar(
    tabs: List<TabItem>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .height(74.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xE6141418),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shadowElevation = 18.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                BottomTabItem(
                    tab = tab,
                    isSelected = selectedTab == tab.route,
                    onClick = { onTabSelected(tab.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    tab: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color.White
    val inactiveColor = Color.White.copy(alpha = 0.55f)
    val contentColor = if (isSelected) activeColor else inactiveColor

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Color.White.copy(alpha = 0.12f)
                    } else {
                        Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )
        }
        Text(
            text = tab.label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun PlaceholderTabScreen(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = subtitle,
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}
