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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.Video
import com.example.ui.AppDestination
import com.example.ui.MgViewModel
import com.example.ui.components.formatCount
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgGold
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextSecondary

@Composable
fun ProfileScreen(
    userId: String?,
    viewModel: MgViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToCreatorStudio: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val context = LocalContext.current
    val loggedInUser by viewModel.currentUser.collectAsState()

    // Determine target profile: loggedInUser if userId is null or matches, else fetch user
    val isMyProfile = userId == null || userId == loggedInUser?.id
    val targetUserFlow = remember(userId, loggedInUser) {
        if (isMyProfile) viewModel.currentUser else viewModel.getUserById(userId ?: "")
    }
    val profileUser by targetUserFlow.collectAsState(initial = loggedInUser)

    val myVideosFlow = remember(profileUser?.id) {
        profileUser?.id?.let { viewModel.getVideosByCreator(it) } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val userVideos by myVideosFlow.collectAsState(initial = emptyList())

    val likedVideos by viewModel.likedVideos.collectAsState()
    val savedVideos by viewModel.savedVideos.collectAsState()
    val drafts by viewModel.userDrafts.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Videos, 1 = Liked, 2 = Saved, 3 = Drafts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${profileUser?.username ?: "mguser"}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (profileUser?.isVerified == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MgCyan, modifier = Modifier.size(16.dp))
                }
            }

            Row {
                if (profileUser?.role == "ADMIN" && isMyProfile) {
                    IconButton(onClick = onNavigateToAdmin, modifier = Modifier.testTag("admin_portal_btn")) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin Portal", tint = MgPurple)
                    }
                }
                if (isMyProfile) {
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("profile_settings_btn")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            }
        }

        // Profile Header Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(MgCyan, MgPurple)))
                    .border(2.dp, MgCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profileUser?.username?.take(1)?.uppercase() ?: "M",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = profileUser?.displayName ?: "MG Creator",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (!profileUser?.bio.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = profileUser?.bio ?: "",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Counter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MgDarkSurface)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Followers", formatCount(profileUser?.followersCount?.toLong() ?: 0))
                StatItem("Following", formatCount(profileUser?.followingCount?.toLong() ?: 0))
                StatItem("Likes", formatCount(profileUser?.likesCount?.toLong() ?: 0))
                StatItem("Videos", "${userVideos.size}")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            if (isMyProfile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToCreatorStudio,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("creator_studio_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MgDarkSurfaceVariant)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = MgCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Creator Studio", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_profile_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Button(
                    onClick = {
                        profileUser?.id?.let {
                            viewModel.toggleFollow(it, false)
                            Toast.makeText(context, "Following @${profileUser?.username}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MgCyan)
                ) {
                    Text("Follow", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Tabs
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
                icon = { Icon(Icons.Default.VideoLibrary, contentDescription = "Videos", modifier = Modifier.size(20.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Liked", modifier = Modifier.size(20.dp)) }
            )
            if (isMyProfile) {
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved", modifier = Modifier.size(20.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Drafts, contentDescription = "Drafts", modifier = Modifier.size(20.dp)) }
                )
            }
        }

        // Tab Content Grid
        val activeList = when (selectedTab) {
            0 -> userVideos
            1 -> likedVideos
            2 -> savedVideos
            else -> emptyList()
        }

        if (selectedTab == 3) {
            // Drafts list
            if (drafts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No drafts saved", color = MgTextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(drafts) { draft ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.75f)
                                .background(MgDarkSurfaceVariant)
                                .clickable {
                                    Toast.makeText(context, "Resuming draft \"${draft.title}\"", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateTo(AppDestination.CreateStudio)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                                Icon(Icons.Default.Drafts, contentDescription = null, tint = MgCyan, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(draft.title, color = Color.White, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Draft", color = MgTextSecondary, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Videos Grid
            if (activeList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (selectedTab) {
                                1 -> "No liked videos"
                                2 -> "No saved videos"
                                else -> "No videos uploaded yet"
                            },
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (selectedTab) {
                                1 -> "Videos you like will appear here"
                                2 -> "Save videos privately to watch later"
                                else -> "Tap (+) to publish your first video!"
                            },
                            color = MgTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(activeList, key = { it.videoId }) { vid ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(0.72f)
                                .background(Brush.verticalGradient(listOf(Color(vid.gradientStart), Color(vid.gradientEnd))))
                                .clickable { onVideoClick(vid) }
                        ) {
                            if (vid.isPinned) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MgHeartRed)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("PINNED", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(formatCount(vid.views), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = MgTextSecondary, fontSize = 11.sp)
    }
}
