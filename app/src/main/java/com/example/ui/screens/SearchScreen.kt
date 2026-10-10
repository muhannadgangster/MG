package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.Video
import com.example.ui.MgViewModel
import com.example.ui.components.formatCount
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextSecondary

@Composable
fun SearchScreen(
    viewModel: MgViewModel,
    onBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onVideoClick: (Video) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0 = Top, 1 = Videos, 2 = Users, 3 = Hashtags

    val matchedUsers = allUsers.filter {
        searchQuery.isNotBlank() && (it.username.contains(searchQuery, ignoreCase = true) || it.displayName.contains(searchQuery, ignoreCase = true))
    }

    val trendingSearches = listOf(
        "Quantum computing mobile",
        "Rainy Shibuya 4K",
        "Future of video MG",
        "Modular synthesizers beats",
        "FPV drone Alps dive",
        "AI design workflows"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
    ) {
        // Search Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Search MG...", color = MgTextSecondary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MgCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MgTextSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_input"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = MgDarkSurface,
                    unfocusedContainerColor = MgDarkSurface,
                    focusedBorderColor = MgCyan,
                    unfocusedBorderColor = Color(0xFF262E3E)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(4.dp))

            TextButton(onClick = { viewModel.submitSearch(searchQuery) }) {
                Text("Search", color = MgCyan, fontWeight = FontWeight.Bold)
            }
        }

        if (searchQuery.isBlank()) {
            // Search History & Trending Searches
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search History
                if (searchHistory.isNotEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, contentDescription = null, tint = MgTextSecondary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recent Searches", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                    Text("Clear All", color = MgTextSecondary, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(searchHistory) { item ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MgDarkSurfaceVariant)
                                            .clickable { viewModel.submitSearch(item.query) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(item.query, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Trending Searches
                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MgCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trending Searches", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            trendingSearches.forEachIndexed { index, query ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.submitSearch(query) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = if (index < 3) MgCyan else MgTextSecondary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        modifier = Modifier.width(26.dp)
                                    )
                                    Text(text = query, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Results Tabs
            val tabs = listOf("Top", "Videos", "Users", "Hashtags")
            TabRow(
                selectedTabIndex = selectedFilterTab,
                containerColor = MgDarkSurface,
                contentColor = MgCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedFilterTab]),
                        color = MgCyan,
                        height = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedFilterTab == index,
                        onClick = { selectedFilterTab = index },
                        text = {
                            Text(
                                title,
                                color = if (selectedFilterTab == index) Color.White else MgTextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Results List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Show Users if filter is Top or Users
                if ((selectedFilterTab == 0 || selectedFilterTab == 2) && matchedUsers.isNotEmpty()) {
                    item {
                        Text("Creators", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    items(matchedUsers) { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MgDarkSurface)
                                .clickable { onNavigateToProfile(user.id) }
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(MgCyan, MgPurple))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (user.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MgCyan, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text("@${user.username} • ${formatCount(user.followersCount.toLong())} followers", color = MgTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Show Videos if filter is Top or Videos
                if (selectedFilterTab == 0 || selectedFilterTab == 1) {
                    if (searchResults.isNotEmpty()) {
                        item {
                            Text("Videos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        items(searchResults) { vid ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MgDarkSurface)
                                    .clickable { onVideoClick(vid) }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 65.dp, height = 90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Brush.verticalGradient(listOf(Color(vid.gradientStart), Color(vid.gradientEnd)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(vid.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("@${vid.creatorUsername}", color = MgCyan, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${formatCount(vid.views)} views • ${formatCount(vid.likes)} likes", color = MgTextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    } else if (selectedFilterTab == 1) {
                        item {
                            Text("No videos found matching \"$searchQuery\"", color = MgTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
