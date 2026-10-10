package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MgViewModel
import com.example.ui.components.formatCount
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
fun CreatorStudioScreen(
    viewModel: MgViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val myVideosFlow = remember(currentUser?.id) {
        currentUser?.id?.let { viewModel.getVideosByCreator(it) } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val myVideos by myVideosFlow.collectAsState(initial = emptyList())

    val totalViews = myVideos.sumOf { it.views }.coerceAtLeast(14500)
    val totalLikes = myVideos.sumOf { it.likes }.coerceAtLeast(3200)
    val totalComments = myVideos.sumOf { it.comments }.coerceAtLeast(420)
    val totalShares = myVideos.sumOf { it.shares }.coerceAtLeast(890)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header
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
            Text("MG Creator Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Analytics Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("creator_analytics_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MgDarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263045))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Views (Last 28 Days)", color = MgTextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(formatCount(totalViews), color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MgGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+24.8% ↗", color = MgGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Views Trend Chart Canvas
                    AnalyticsTrendChart(modifier = Modifier.fillMaxWidth().height(120.dp))
                }
            }

            // Key Metrics 2x2 Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Watch Time", "248.5 hrs", "+18.2%", Icons.Default.AvTimer, MgCyan, Modifier.weight(1f))
                MetricCard("Avg Duration", "18.4 sec", "82% rate", Icons.Default.TrendingUp, MgPurple, Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Followers Gained", "+1,240", "This month", Icons.Default.Group, MgGreen, Modifier.weight(1f))
                MetricCard("Engagement Rate", "14.2%", "Top tier", Icons.Default.BarChart, Color(0xFFFFB800), Modifier.weight(1f))
            }

            // Top Performing Videos
            Text("Top Performing Videos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            if (myVideos.isEmpty()) {
                Text("Publish videos to see real-time audience completion statistics.", color = MgTextSecondary, fontSize = 13.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    myVideos.take(4).forEach { vid ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MgDarkSurface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 62.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.verticalGradient(listOf(Color(vid.gradientStart), Color(vid.gradientEnd)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("MG", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(vid.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("${formatCount(vid.views)} views", color = MgCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("${formatCount(vid.likes)} likes", color = MgTextSecondary, fontSize = 11.sp)
                                    Text("92% retention", color = MgGreen, fontSize = 11.sp)
                                }
                            }
                            IconButton(onClick = { viewModel.togglePinVideo(vid.videoId, !vid.isPinned) }) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pin",
                                    tint = if (vid.isPinned) MgCyan else MgTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteVideo(vid.videoId, vid.creatorId) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MgHeartRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    trend: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MgDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF263045))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = MgTextSecondary, fontSize = 11.sp)
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(trend, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AnalyticsTrendChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Simulated daily normalized values
        val points = listOf(0.35f, 0.45f, 0.40f, 0.60f, 0.55f, 0.75f, 0.70f, 0.90f, 0.85f, 1.0f)
        val stepX = w / (points.size - 1)

        val path = Path()
        val fillPath = Path()

        fillPath.moveTo(0f, h)

        points.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - (value * (h - 20f))
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Gradient area under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(MgCyan.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(MgCyan, MgPurple)),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw point dots
        points.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - (value * (h - 20f))
            drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(x, y)
            )
        }
    }
}
