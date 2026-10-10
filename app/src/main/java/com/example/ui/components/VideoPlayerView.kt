package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Video
import com.example.ui.components.GlassTier
import com.example.ui.components.liquidGlass
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgGold
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun VideoPlayerView(
    video: Video,
    isCurrentActive: Boolean,
    isLiked: Boolean,
    isSaved: Boolean,
    isFollowing: Boolean,
    onLikeToggle: () -> Unit,
    onSaveToggle: () -> Unit,
    onFollowToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onCreatorClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var showPauseOverlay by remember { mutableStateOf(false) }
    var doubleTapHeartOffset by remember { mutableStateOf<Offset?>(null) }
    var showHeartBurst by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Automatically pause if not the active video in pager
    LaunchedEffect(isCurrentActive) {
        if (!isCurrentActive) {
            isPlaying = false
        } else {
            isPlaying = true
        }
    }

    // Video progress simulation
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying, isCurrentActive) {
        if (isPlaying && isCurrentActive) {
            val step = 0.01f
            while (isPlaying) {
                delay(150)
                progress += step
                if (progress >= 1f) progress = 0f
            }
        }
    }

    // Audio vinyl rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    // Dynamic wave animation for video audio spectrum visualizer
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spectrum_wave"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        isPlaying = !isPlaying
                        showPauseOverlay = true
                        coroutineScope.launch {
                            delay(900)
                            showPauseOverlay = false
                        }
                    },
                    onDoubleTap = { offset ->
                        doubleTapHeartOffset = offset
                        showHeartBurst = true
                        if (!isLiked) {
                            onLikeToggle()
                        }
                        coroutineScope.launch {
                            delay(900)
                            showHeartBurst = false
                            doubleTapHeartOffset = null
                        }
                    },
                    onLongPress = {
                        onLongPress()
                    }
                )
            }
    ) {
        // --- High-End Cinematic Dynamic Video Canvas ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background dynamic color gradient
            val startColor = Color(video.gradientStart)
            val endColor = Color(video.gradientEnd)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor, Color(0xFF07080B)),
                    startY = 0f,
                    endY = height
                )
            )

            // Dynamic ambient light circles and pulsing wave
            val cx = width * 0.5f + (sin(waveOffset.toDouble()) * 80f).toFloat()
            val cy = height * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        startColor.copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = width * 0.7f
                ),
                center = Offset(cx, cy),
                radius = width * 0.7f
            )

            // Animated waveform bars in center
            if (isPlaying) {
                val barCount = 32
                val barWidth = width / (barCount * 1.8f)
                val spacing = barWidth * 0.8f
                val startX = (width - (barCount * (barWidth + spacing))) / 2f
                val baseY = height * 0.48f

                for (i in 0 until barCount) {
                    val angle = waveOffset + (i * 0.35f)
                    val barHeight = ((sin(angle.toDouble()) + 1.2) * 55f * (0.6f + (i % 3) * 0.3f)).toFloat()
                    val x = startX + i * (barWidth + spacing)

                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(MgCyan.copy(alpha = 0.85f), MgPurple.copy(alpha = 0.7f)),
                            startY = baseY - barHeight / 2,
                            endY = baseY + barHeight / 2
                        ),
                        start = Offset(x, baseY - barHeight / 2),
                        end = Offset(x, baseY + barHeight / 2),
                        strokeWidth = barWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        // Top subtle gradient scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
                .align(Alignment.TopCenter)
        )

        // Bottom heavy gradient scrim for readable text and action bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                    )
                )
                .align(Alignment.BottomCenter)
        )

        // Center pause / play icon animation overlay
        AnimatedVisibility(
            visible = showPauseOverlay,
            enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.5.dp, MgCyan.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!isPlaying) Icons.Default.PlayArrow else Icons.Default.GraphicEq,
                    contentDescription = "Playback state",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        // Double-tap heart burst effect
        AnimatedVisibility(
            visible = showHeartBurst,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = MgHeartRed,
                modifier = Modifier.size(96.dp)
            )
        }

        // Mute / Unmute quick toggle button (top right) in Liquid Glass
        IconButton(
            onClick = { isMuted = !isMuted },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .size(38.dp)
                .liquidGlass(shape = CircleShape, tier = GlassTier.ULTRA_THIN, borderWidth = 1.dp)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Toggle Mute",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // --- Right-Hand Interaction Action Column ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Creator Avatar with (+) Follow badge
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MgCyan, CircleShape)
                        .clickable { onCreatorClick() }
                        .testTag("creator_avatar_click")
                ) {
                    if (video.creatorAvatar.isNotBlank()) {
                        AsyncImage(
                            model = video.creatorAvatar,
                            contentDescription = video.creatorUsername,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(MgCyan, MgPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = video.creatorUsername.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                if (!isFollowing) {
                    Box(
                        modifier = Modifier
                            .offset(y = 8.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MgCyan)
                            .clickable { onFollowToggle() }
                            .testTag("follow_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Follow",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Like Button with heart spring animation
            val heartScale by animateFloatAsState(
                targetValue = if (isLiked) 1.25f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "heart_scale"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onLikeToggle() }
                    .testTag("like_button")
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) MgHeartRed else Color.White,
                    modifier = Modifier
                        .size(34.dp)
                        .scale(heartScale)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCount(video.likes + if (isLiked) 1 else 0),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Comment Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCommentClick() }
                    .testTag("comment_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comments",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCount(video.comments),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Save / Bookmark Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onSaveToggle() }
                    .testTag("save_button")
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) MgGold else Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCount(video.saves + if (isSaved) 1 else 0),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Share Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onShareClick() }
                    .testTag("share_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCount(video.shares),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Spinning Vinyl Disc
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2433))
                    .border(2.dp, Color(0xFF333B4F), CircleShape)
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MgCyan, MgPurple)))
                )
            }
        }

        // --- Bottom Left Content Info Overlay ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 26.dp)
        ) {
            // Creator handle & verified check
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onCreatorClick() }
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "@${video.creatorUsername}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified",
                    tint = MgCyan,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Title & Description
            var isExpanded by remember { mutableStateOf(false) }
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )

            if (video.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Hashtags
            if (video.hashtags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.hashtags,
                    color = MgCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio track ticker in Liquid Glass
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .liquidGlass(shape = RoundedCornerShape(12.dp), tier = GlassTier.ULTRA_THIN, borderWidth = 0.8.dp)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Audio track",
                    tint = MgCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${video.audioTitle} • ${video.audioArtist}",
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // --- Bottom Scrubber Progress Bar ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(listOf(MgCyan, MgPurple))
                    )
            )
        }
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size((scale * 32).dp)
)

fun formatCount(count: Long): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
