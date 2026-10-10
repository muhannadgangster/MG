package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.User
import com.example.data.model.Video
import com.example.ui.AppDestination
import com.example.ui.MainTab
import com.example.ui.MgViewModel
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.GlassTier
import com.example.ui.components.LgMonogramLogo
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.ReportVideoDialog
import com.example.ui.components.ShareBottomSheet
import com.example.ui.components.VideoLongPressDialog
import com.example.ui.components.VideoPlayerView
import com.example.ui.components.liquidGlass
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MgViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val homeFeedType by viewModel.homeFeedType.collectAsState()
    val publicFeed by viewModel.publicFeed.collectAsState()
    val followingFeed by viewModel.followingFeed.collectAsState()

    val currentFeed = if (homeFeedType == "FOLLOWING") followingFeed else publicFeed

    val likedVideos by viewModel.likedVideos.collectAsState()
    val savedVideos by viewModel.savedVideos.collectAsState()

    // Modals state
    var selectedVideoForComments by remember { mutableStateOf<Video?>(null) }
    var selectedVideoForShare by remember { mutableStateOf<Video?>(null) }
    var selectedVideoForLongPress by remember { mutableStateOf<Video?>(null) }
    var selectedVideoForReport by remember { mutableStateOf<Video?>(null) }

    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shareSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { currentFeed.size }
    )

    // Register view for current video
    LaunchedEffect(pagerState.currentPage, currentFeed) {
        if (currentFeed.isNotEmpty() && pagerState.currentPage in currentFeed.indices) {
            val activeVideo = currentFeed[pagerState.currentPage]
            viewModel.registerVideoView(activeVideo.videoId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
    ) {
        if (currentFeed.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LgMonogramLogo(size = 56.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (homeFeedType == "FOLLOWING") "No videos from followed creators yet" else "No videos yet",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (homeFeedType == "FOLLOWING") "Follow creators from 'For You' or Explore!" else "Be the first to publish on LG!",
                        color = MgTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Fullscreen Vertical Pager
            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_vertical_pager")
            ) { page ->
                val video = currentFeed[page]
                val isLiked = likedVideos.any { it.videoId == video.videoId }
                val isSaved = savedVideos.any { it.videoId == video.videoId }
                val isFollowing = allUsers.find { it.id == video.creatorId }?.let { creator ->
                    currentUser?.followingCount ?: 0 > 0
                } ?: false

                VideoPlayerView(
                    video = video,
                    isCurrentActive = pagerState.currentPage == page,
                    isLiked = isLiked,
                    isSaved = isSaved,
                    isFollowing = isFollowing,
                    onLikeToggle = { viewModel.toggleLike(video.videoId, isLiked) },
                    onSaveToggle = { viewModel.toggleSave(video.videoId, isSaved) },
                    onFollowToggle = { viewModel.toggleFollow(video.creatorId, isFollowing) },
                    onCommentClick = { selectedVideoForComments = video },
                    onShareClick = {
                        viewModel.shareVideo(video.videoId)
                        selectedVideoForShare = video
                    },
                    onCreatorClick = { onNavigateToProfile(video.creatorId) },
                    onLongPress = { selectedVideoForLongPress = video }
                )
            }
        }

        // Top Navigation Header Overlay (Following | For You + Live & Search)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Live badge in Liquid Glass Pill
            LiquidGlassPill(
                isSelected = false,
                onClick = {
                    Toast.makeText(context, "LG Live Streams coming in next creator update!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MgHeartRed)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Feed selector: "Following" & "For You"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Following",
                    color = if (homeFeedType == "FOLLOWING") Color.White else MgTextSecondary,
                    fontSize = if (homeFeedType == "FOLLOWING") 17.sp else 15.sp,
                    fontWeight = if (homeFeedType == "FOLLOWING") FontWeight.Black else FontWeight.Normal,
                    modifier = Modifier.clickable { viewModel.setHomeFeedType("FOLLOWING") }
                )
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(MgTextSecondary.copy(alpha = 0.5f))
                )
                Text(
                    text = "For You",
                    color = if (homeFeedType == "FOR_YOU") Color.White else MgTextSecondary,
                    fontSize = if (homeFeedType == "FOR_YOU") 17.sp else 15.sp,
                    fontWeight = if (homeFeedType == "FOR_YOU") FontWeight.Black else FontWeight.Normal,
                    modifier = Modifier.clickable { viewModel.setHomeFeedType("FOR_YOU") }
                )
            }

            // Search Icon with Liquid Glass
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .liquidGlass(shape = CircleShape, tier = GlassTier.ULTRA_THIN, borderWidth = 1.dp)
                    .clickable { onNavigateToSearch() }
                    .testTag("home_search_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // --- Comment Bottom Sheet ---
    selectedVideoForComments?.let { vid ->
        val commentsFlow = remember(vid.videoId) { viewModel.getCommentsForVideo(vid.videoId) }
        val comments by commentsFlow.collectAsState(initial = emptyList())

        CommentBottomSheet(
            comments = comments,
            currentUser = currentUser,
            sheetState = commentSheetState,
            onDismiss = { selectedVideoForComments = null },
            onAddComment = { text, parentId ->
                viewModel.addComment(vid.videoId, text, parentId)
            },
            onLikeComment = { commentId ->
                viewModel.likeComment(commentId)
            },
            onDeleteComment = { commentId ->
                viewModel.deleteComment(commentId, vid.videoId)
            }
        )
    }

    // --- Share Bottom Sheet ---
    selectedVideoForShare?.let { vid ->
        ShareBottomSheet(
            video = vid,
            contacts = allUsers.filter { it.id != currentUser?.id },
            sheetState = shareSheetState,
            onDismiss = { selectedVideoForShare = null },
            onShareToUser = { recipient ->
                viewModel.sendChatMessage(
                    receiverId = recipient.id,
                    text = "Check out this video on MG: \"${vid.title}\"",
                    sharedVideoId = vid.videoId
                )
            },
            onReportClick = {
                selectedVideoForReport = vid
            }
        )
    }

    // --- Video Long Press Dialog ---
    selectedVideoForLongPress?.let { vid ->
        val isSaved = savedVideos.any { it.videoId == vid.videoId }
        VideoLongPressDialog(
            onDismiss = { selectedVideoForLongPress = null },
            onNotInterested = {
                Toast.makeText(context, "We'll show fewer videos like this", Toast.LENGTH_SHORT).show()
                selectedVideoForLongPress = null
            },
            onCopyLink = {
                val clip = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clip.setPrimaryClip(android.content.ClipData.newPlainText("MG Link", "https://mg.app/video/${vid.videoId}"))
                Toast.makeText(context, "Link copied!", Toast.LENGTH_SHORT).show()
                selectedVideoForLongPress = null
            },
            onSaveVideo = {
                viewModel.toggleSave(vid.videoId, isSaved)
                Toast.makeText(context, if (isSaved) "Removed from Saved" else "Saved to Collection", Toast.LENGTH_SHORT).show()
                selectedVideoForLongPress = null
            },
            onOpenReport = {
                selectedVideoForReport = vid
                selectedVideoForLongPress = null
            }
        )
    }

    // --- Report Dialog ---
    selectedVideoForReport?.let { vid ->
        ReportVideoDialog(
            videoTitle = vid.title,
            onDismiss = { selectedVideoForReport = null },
            onSubmitReport = { reason ->
                viewModel.reportVideo(vid.videoId, vid.title, reason)
                Toast.makeText(context, "Report received. MG Moderation will review within 24h.", Toast.LENGTH_LONG).show()
                selectedVideoForReport = null
            }
        )
    }
}
