package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Comment
import com.example.data.model.Report
import com.example.data.model.User
import com.example.data.model.Video
import com.example.ui.MgViewModel
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgGreen
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextSecondary

@Composable
fun AdminDashboardScreen(
    viewModel: MgViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allVideos by viewModel.allVideosAdmin.collectAsState()
    val reports by viewModel.reportsList.collectAsState()
    val allComments by viewModel.allCommentsAdmin.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }

    val adminTabs = listOf(
        "Overview",
        "Users (${allUsers.size})",
        "Videos (${allVideos.size})",
        "Reports (${reports.size})",
        "Comments (${allComments.size})",
        "Categories",
        "Broadcast"
    )

    // Security Gate check
    if (currentUser?.role != "ADMIN") {
        Box(
            modifier = Modifier.fillMaxSize().background(MgPitchBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MgHeartRed, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Access Denied", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("This administrative portal is strictly restricted to MG Operations personnel.", color = MgTextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = MgCyan)) {
                    Text("Return", color = Color.Black)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MgPurple, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("MG Trust & Safety Portal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MgDarkSurface,
            contentColor = MgCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MgPurple,
                    height = 2.dp
                )
            }
        ) {
            adminTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTab == index) Color.White else MgTextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Overview
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Platform Operational Metrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Total Users", "${allUsers.size}", "Registered", Icons.Default.VerifiedUser, MgCyan, Modifier.weight(1f))
                        MetricCard("Total Videos", "${allVideos.size}", "Published", Icons.Default.PlayArrow, MgPurple, Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard("Reports Queue", "${reports.size}", "Active items", Icons.Default.Flag, MgHeartRed, Modifier.weight(1f))
                        MetricCard("Suspended Users", "${allUsers.count { it.isBanned }}", "Banned", Icons.Default.Lock, Color(0xFFFFB800), Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("System Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SystemStatusRow("Video Streaming Pipeline", "Operational", MgGreen)
                            SystemStatusRow("Room Local Persistence & Sync", "Active", MgGreen)
                            SystemStatusRow("Anti-Spam View Detection", "Active (Session Protected)", MgCyan)
                            SystemStatusRow("Automated Moderation Gate", "Ready", MgPurple)
                        }
                    }
                }
            }

            1 -> {
                // Users Management
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allUsers, key = { it.id }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MgDarkSurface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (user.role == "ADMIN") {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MgPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("ADMIN", color = MgPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text("@${user.username} • ${user.email}", color = MgTextSecondary, fontSize = 12.sp)
                                if (user.isBanned) {
                                    Text("STATUS: SUSPENDED", color = MgHeartRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (user.role != "ADMIN") {
                                Button(
                                    onClick = {
                                        viewModel.setBannedStatus(user.id, !user.isBanned)
                                        Toast.makeText(context, if (user.isBanned) "User restored" else "User suspended", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (user.isBanned) MgGreen else MgHeartRed
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (user.isBanned) "Restore" else "Suspend", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Videos Management
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allVideos, key = { it.videoId }) { vid ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vid.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("@${vid.creatorUsername} • ${vid.category} • ${vid.moderationStatus}", color = MgTextSecondary, fontSize = 11.sp)
                                }
                                IconButton(onClick = { viewModel.togglePinVideo(vid.videoId, !vid.isPinned) }) {
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Pin",
                                        tint = if (vid.isPinned) MgCyan else MgTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        val newStatus = if (vid.moderationStatus == "REMOVED") "APPROVED" else "REMOVED"
                                        viewModel.setVideoModeration(vid.videoId, newStatus)
                                        Toast.makeText(context, "Status set to $newStatus", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vid.moderationStatus == "REMOVED") MgGreen else MgHeartRed
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(if (vid.moderationStatus == "REMOVED") "Restore" else "Remove", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Reports & Moderation
                if (reports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MgGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Moderation queue is all clear!", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("No flagged videos currently pending review.", color = MgTextSecondary, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reports, key = { it.id }) { report ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(report.videoTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MgHeartRed.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(report.reason, color = MgHeartRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Reported by: @${report.reportedByUsername}", color = MgTextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.resolveReport(report.id, "DISMISSED")
                                                Toast.makeText(context, "Report dismissed", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Dismiss", color = MgTextSecondary, fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.setVideoModeration(report.videoId, "REMOVED")
                                                viewModel.resolveReport(report.id, "RESOLVED")
                                                Toast.makeText(context, "Video removed from feed", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MgHeartRed),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Remove Video", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Comments Moderation
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(allComments, key = { it.commentId }) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MgDarkSurface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("@${comment.username}", color = MgCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(comment.text, color = Color.White, fontSize = 13.sp)
                            }
                            IconButton(onClick = {
                                viewModel.deleteCommentAdmin(comment.commentId)
                                Toast.makeText(context, "Comment removed", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete comment", tint = MgHeartRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            5 -> {
                // Categories & Hashtags
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Featured Categories", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val catList = listOf("Technology", "Music", "Art", "Sports", "Gaming", "Comedy", "Education", "Travel", "Fashion", "Food", "Fitness")
                    catList.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MgDarkSurface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, tint = MgCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(cat, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Text("Active", color = MgGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            6 -> {
                // Broadcast
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Broadcast System Announcement", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Send notifications to all MG users worldwide.", color = MgTextSecondary, fontSize = 12.sp)

                    OutlinedTextField(
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        label = { Text("Announcement Headline") },
                        placeholder = { Text("e.g. MG Creator Festival 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = announcementMessage,
                        onValueChange = { announcementMessage = it },
                        label = { Text("Message Body") },
                        placeholder = { Text("Detailed notification message...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Button(
                        onClick = {
                            if (announcementTitle.isNotBlank() && announcementMessage.isNotBlank()) {
                                viewModel.broadcastAnnouncement(announcementTitle, announcementMessage)
                                Toast.makeText(context, "Announcement broadcast sent!", Toast.LENGTH_SHORT).show()
                                announcementTitle = ""
                                announcementMessage = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MgPurple),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Announcement", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatusRow(label: String, status: String, statusColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
        Text(status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
