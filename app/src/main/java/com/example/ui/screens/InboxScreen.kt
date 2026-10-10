package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationItem
import com.example.data.model.User
import com.example.ui.MgViewModel
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextSecondary

@Composable
fun InboxScreen(
    viewModel: MgViewModel,
    onOpenChat: (userId: String, username: String) -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Notifications, 1 = Messages

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Activity & Inbox", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

            if (selectedTab == 0 && notifications.isNotEmpty()) {
                IconButton(onClick = { viewModel.markNotificationsAsRead() }) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Mark all read", tint = MgCyan)
                }
            }
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MgDarkSurface,
            contentColor = MgCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MgCyan,
                    height = 2.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications", color = if (selectedTab == 0) Color.White else MgTextSecondary, fontWeight = FontWeight.SemiBold)
                        val unreadCount = notifications.count { !it.isRead }
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(MgHeartRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$unreadCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text("Messages", color = if (selectedTab == 1) Color.White else MgTextSecondary, fontWeight = FontWeight.SemiBold)
                }
            )
        }

        if (selectedTab == 0) {
            // Notifications List
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MgTextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No notifications yet", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Likes, comments, and followers will appear here.", color = MgTextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationRow(notif = notif)
                    }
                }
            }
        } else {
            // Messages List (Direct messaging contacts)
            val contacts = allUsers.filter { it.id != currentUser?.id }
            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No chats yet", color = MgTextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(contacts) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MgDarkSurface)
                                .clickable { onOpenChat(user.id, user.username) }
                                .padding(12.dp)
                                .testTag("chat_contact_${user.username}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(MgCyan, MgPurple))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Tap to chat with @${user.username}", color = MgTextSecondary, fontSize = 12.sp, maxLines = 1)
                            }
                            Icon(Icons.Default.Chat, contentDescription = null, tint = MgCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationRow(notif: NotificationItem) {
    val icon = when (notif.type) {
        "LIKE" -> Icons.Default.Favorite
        "COMMENT" -> Icons.Default.Comment
        "FOLLOW" -> Icons.Default.PersonAdd
        else -> Icons.Default.Campaign
    }
    val iconTint = when (notif.type) {
        "LIKE" -> MgHeartRed
        "COMMENT" -> MgCyan
        "FOLLOW" -> MgPurple
        else -> Color(0xFFFFB800)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (notif.isRead) MgDarkSurface else MgDarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = notif.type, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(notif.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(notif.message, color = Color(0xFFCBD5E1), fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }

        if (!notif.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MgCyan)
            )
        }
    }
}
