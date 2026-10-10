package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Draft
import com.example.ui.MgViewModel
import com.example.ui.components.GlassTier
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPill
import com.example.ui.components.liquidGlass
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgGreen
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class StudioStep {
    SELECT_MEDIA, EDIT_VIDEO, PUBLISH
}

data class VideoFilter(val name: String, val startColor: Long, val endColor: Long)

@Composable
fun CreateStudioScreen(
    viewModel: MgViewModel,
    onBack: () -> Unit,
    onPublished: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    var currentStep by remember { mutableStateOf(StudioStep.SELECT_MEDIA) }

    // Media State
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf("MG Studio Video Clip") }

    // Video Picker Launcher using Android Photo Picker (zero permission)
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedVideoName = "Imported Video (${uri.lastPathSegment?.take(15) ?: "mobile"})"
            currentStep = StudioStep.EDIT_VIDEO
        }
    }

    // Editor State
    val filters = listOf(
        VideoFilter("Cyberpunk", 0xFF00E5FF, 0xFF7C3AED),
        VideoFilter("Cinematic", 0xFF0F2027, 0xFF203A43),
        VideoFilter("Neon Glow", 0xFFFF007F, 0xFF7928CA),
        VideoFilter("Vintage Sunset", 0xFFFF512F, 0xFFDD2476),
        VideoFilter("Matrix Green", 0xFF021B1A, 0xFF059669),
        VideoFilter("Noir B&W", 0xFF1E293B, 0xFF0F172A)
    )
    var selectedFilter by remember { mutableStateOf(filters[0]) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(1.0f) }
    var overlayText by remember { mutableStateOf("") }
    var showTextDialog by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(0.0f) }
    var trimStart by remember { mutableFloatStateOf(0f) }
    var trimEnd by remember { mutableFloatStateOf(1f) }

    // Publishing State
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("#MG #Create #Viral") }
    var category by remember { mutableStateOf("Technology") }
    var audience by remember { mutableStateOf("PUBLIC") } // PUBLIC, FOLLOWERS, PRIVATE
    var allowComments by remember { mutableStateOf(true) }
    var allowDownloads by remember { mutableStateOf(true) }
    var allowRemix by remember { mutableStateOf(true) }
    var audioTrackName by remember { mutableStateOf("Original Audio - MG Studio") }

    // Upload progress state
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadStatusText by remember { mutableStateOf("Initializing media...") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when (currentStep) {
            StudioStep.SELECT_MEDIA -> {
                SelectMediaScreen(
                    onBack = onBack,
                    onPickFromDevice = {
                        pickVideoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onUseStudioTemplate = { templateName, filter ->
                        selectedVideoName = templateName
                        selectedFilter = filter
                        currentStep = StudioStep.EDIT_VIDEO
                    }
                )
            }

            StudioStep.EDIT_VIDEO -> {
                EditVideoScreen(
                    videoTitle = selectedVideoName,
                    filter = selectedFilter,
                    filters = filters,
                    onSelectFilter = { selectedFilter = it },
                    speed = playbackSpeed,
                    onSpeedChange = { playbackSpeed = it },
                    isMuted = isMuted,
                    onToggleMute = { isMuted = !isMuted },
                    volume = volume,
                    onVolumeChange = { volume = it },
                    overlayText = overlayText,
                    onAddTextClick = { showTextDialog = true },
                    trimStart = trimStart,
                    trimEnd = trimEnd,
                    onTrimChange = { s, e -> trimStart = s; trimEnd = e },
                    onSaveDraft = {
                        val draft = Draft(
                            id = "draft_${UUID.randomUUID().toString().take(8)}",
                            creatorId = currentUser?.id ?: "guest",
                            title = selectedVideoName,
                            description = "Unfinished video draft",
                            hashtags = hashtags,
                            videoUri = selectedVideoUri?.toString() ?: "studio_template",
                            filterName = selectedFilter.name,
                            speed = playbackSpeed,
                            volume = volume,
                            overlayText = overlayText
                        )
                        viewModel.saveDraft(draft) {
                            Toast.makeText(context, "Draft saved privately to profile!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    onNext = {
                        title = selectedVideoName
                        currentStep = StudioStep.PUBLISH
                    },
                    onBack = { currentStep = StudioStep.SELECT_MEDIA }
                )
            }

            StudioStep.PUBLISH -> {
                PublishVideoScreen(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    hashtags = hashtags,
                    onHashtagsChange = { hashtags = it },
                    category = category,
                    onCategoryChange = { category = it },
                    audience = audience,
                    onAudienceChange = { audience = it },
                    allowComments = allowComments,
                    onAllowCommentsChange = { allowComments = it },
                    allowDownloads = allowDownloads,
                    onAllowDownloadsChange = { allowDownloads = it },
                    allowRemix = allowRemix,
                    onAllowRemixChange = { allowRemix = it },
                    audioTrack = audioTrackName,
                    onAudioTrackChange = { audioTrackName = it },
                    filter = selectedFilter,
                    isUploading = isUploading,
                    uploadProgress = uploadProgress,
                    uploadStatusText = uploadStatusText,
                    onPublish = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a title for your video.", Toast.LENGTH_SHORT).show()
                            return@PublishVideoScreen
                        }
                        isUploading = true
                        coroutineScope.launch {
                            uploadStatusText = "Uploading video to secure cloud... 25%"
                            uploadProgress = 0.25f
                            delay(600)
                            uploadStatusText = "Processing high-res encoding & thumbnail... 65%"
                            uploadProgress = 0.65f
                            delay(700)
                            uploadStatusText = "Running content safety & moderation check... 90%"
                            uploadProgress = 0.90f
                            delay(500)
                            uploadStatusText = "Publishing to global MG feed... 100%"
                            uploadProgress = 1.0f
                            delay(400)

                            viewModel.publishVideo(
                                title = title,
                                description = description,
                                hashtags = hashtags,
                                audioTitle = audioTrackName,
                                category = category,
                                visibility = audience,
                                commentsEnabled = allowComments,
                                downloadsEnabled = allowDownloads,
                                remixEnabled = allowRemix,
                                gradientStart = selectedFilter.startColor,
                                gradientEnd = selectedFilter.endColor
                            ) {
                                isUploading = false
                                Toast.makeText(context, "Video Published Successfully! 🚀", Toast.LENGTH_LONG).show()
                                onPublished()
                            }
                        }
                    },
                    onBack = { currentStep = StudioStep.EDIT_VIDEO }
                )
            }
        }

        // Overlay Text Input Dialog
        if (showTextDialog) {
            var tempText by remember { mutableStateOf(overlayText) }
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showTextDialog = false },
                containerColor = MgDarkSurface,
                title = { Text("Add Text Overlay", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Add engaging title or call-to-action text on your video frame:", color = MgTextSecondary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = tempText,
                            onValueChange = { tempText = it },
                            placeholder = { Text("e.g. Wait for the drop! 🔥") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            overlayText = tempText
                            showTextDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MgCyan)
                    ) {
                        Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTextDialog = false }) {
                        Text("Cancel", color = MgTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SelectMediaScreen(
    onBack: () -> Unit,
    onPickFromDevice: () -> Unit,
    onUseStudioTemplate: (String, VideoFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("MG Creation Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Device Upload Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPickFromDevice() }
                .testTag("upload_device_video_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MgDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.linearGradient(listOf(MgCyan, MgPurple)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(MgCyan, MgPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Upload Video from Device", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Select MP4, MOV or WEBM videos from your gallery", color = MgTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF262E3E)))
            Text("  OR START FROM MG TEMPLATES  ", color = MgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF262E3E)))
        }

        Spacer(modifier = Modifier.height(20.dp))

        val templates = listOf(
            Pair("Cyberpunk Tokyo 2099", VideoFilter("Cyberpunk", 0xFF00E5FF, 0xFF7C3AED)),
            Pair("Analog Synthesizer Jam", VideoFilter("Cinematic", 0xFF0F2027, 0xFF203A43)),
            Pair("Alpine FPV Drone 4K", VideoFilter("Matrix Green", 0xFF021B1A, 0xFF059669)),
            Pair("Sunset Lo-Fi Chill", VideoFilter("Vintage Sunset", 0xFFFF512F, 0xFFDD2476))
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            templates.forEach { (name, filter) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MgDarkSurfaceVariant)
                        .clickable { onUseStudioTemplate(name, filter) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(Color(filter.startColor), Color(filter.endColor)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${filter.name} Filter Preset", color = MgCyan, fontSize = 11.sp)
                    }
                    Icon(Icons.Default.Upload, contentDescription = null, tint = MgTextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun EditVideoScreen(
    videoTitle: String,
    filter: VideoFilter,
    filters: List<VideoFilter>,
    onSelectFilter: (VideoFilter) -> Unit,
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    overlayText: String,
    onAddTextClick: () -> Unit,
    trimStart: Float,
    trimEnd: Float,
    onTrimChange: (Float, Float) -> Unit,
    onSaveDraft: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Video Editor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Row {
                TextButton(onClick = onSaveDraft) {
                    Text("Draft", color = MgTextSecondary, fontSize = 13.sp)
                }
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = MgCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Next", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Video Canvas Preview
        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Color(filter.startColor), Color(filter.endColor)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(videoTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                Text("Speed: ${speed}x • ${filter.name}", color = MgCyan, fontSize = 11.sp)
            }

            // Overlay Text Preview
            if (overlayText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 30.dp, start = 12.dp, end = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(overlayText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Video Timeline Trim Scrubber
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MgDarkSurface)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Trim Timeline", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("${String.format("%.1f", trimEnd * 15)}s duration", color = MgCyan, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = trimEnd,
                onValueChange = { onTrimChange(trimStart, it.coerceAtLeast(0.2f)) },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(thumbColor = MgCyan, activeTrackColor = MgCyan)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Presets Carousel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MgDarkSurface)
                .padding(14.dp)
        ) {
            Text("Visual Filters", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filters) { f ->
                    val isSelected = f.name == filter.name
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSelectFilter(f) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(f.startColor), Color(f.endColor))))
                                .border(if (isSelected) 2.5.dp else 0.dp, MgCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(f.name, color = if (isSelected) MgCyan else MgTextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Speed & Audio Tools Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Speed control
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MgDarkSurface)
                    .padding(12.dp)
            ) {
                Text("Speed (${speed}x)", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { s ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (speed == s) MgCyan else MgDarkSurfaceVariant)
                                .clickable { onSpeedChange(s) }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${s}x", color = if (speed == s) Color.Black else Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Text Tool Button
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MgDarkSurface)
                    .clickable { onAddTextClick() }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.FormatColorText, contentDescription = null, tint = MgCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (overlayText.isBlank()) "Add Text" else "Edit Text", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Audio Mute Toggle
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MgDarkSurface)
                    .clickable { onToggleMute() }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp, contentDescription = null, tint = if (isMuted) MgHeartRed else MgCyan, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (isMuted) "Muted" else "Sound ON", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PublishVideoScreen(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    hashtags: String,
    onHashtagsChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    audience: String,
    onAudienceChange: (String) -> Unit,
    allowComments: Boolean,
    onAllowCommentsChange: (Boolean) -> Unit,
    allowDownloads: Boolean,
    onAllowDownloadsChange: (Boolean) -> Unit,
    allowRemix: Boolean,
    onAllowRemixChange: (Boolean) -> Unit,
    audioTrack: String,
    onAudioTrackChange: (String) -> Unit,
    filter: VideoFilter,
    isUploading: Boolean,
    uploadProgress: Float,
    uploadStatusText: String,
    onPublish: () -> Unit,
    onBack: () -> Unit
) {
    val categories = listOf(
        "Technology", "Music", "Art", "Sports", "Gaming", "Comedy",
        "Education", "Travel", "Fashion", "Food", "Fitness", "Dance",
        "Science", "Film & Animation", "Beauty"
    )
    val suggestedTags = listOf(
        "#LG", "#LiquidGlass", "#Trending", "#Viral", "#FutureOfVideo",
        "#TechTok", "#ModularSynth", "#Tokyo", "#FPVDrone", "#Creative", "#Dance", "#Music"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, enabled = !isUploading) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publish Video", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Thumbnail & Title Row
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(width = 80.dp, height = 110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(listOf(Color(filter.startColor), Color(filter.endColor)))),
                contentAlignment = Alignment.Center
            ) {
                Text("Thumbnail", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Video Title") },
                    placeholder = { Text("Describe your video...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            placeholder = { Text("Add more context, story, or links...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Hashtags
        OutlinedTextField(
            value = hashtags,
            onValueChange = onHashtagsChange,
            label = { Text("Hashtags") },
            placeholder = { Text("#trending #lg #liquidglass #future") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Quick Hashtag Suggestions (tap to add/remove):", color = MgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(suggestedTags) { tag ->
                val isAdded = hashtags.contains(tag, ignoreCase = true)
                LiquidGlassPill(
                    isSelected = isAdded,
                    onClick = {
                        val current = hashtags.trim()
                        if (isAdded) {
                            val updated = current.split(" ").filter { !it.equals(tag, ignoreCase = true) }.joinToString(" ")
                            onHashtagsChange(updated)
                        } else {
                            val updated = if (current.isEmpty()) tag else "$current $tag"
                            onHashtagsChange(updated)
                        }
                    }
                ) {
                    Text(
                        text = tag,
                        color = if (isAdded) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selector
        Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                val isSelected = cat == category
                LiquidGlassPill(
                    isSelected = isSelected,
                    onClick = { onCategoryChange(cat) }
                ) {
                    Text(cat, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audience Selector
        Text("Who can watch this video?", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("PUBLIC" to "Public", "FOLLOWERS" to "Followers", "PRIVATE" to "Private").forEach { (key, label) ->
                val isSelected = audience == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(Brush.linearGradient(listOf(MgCyan, MgPurple)))
                            } else {
                                Modifier.liquidGlass(shape = RoundedCornerShape(12.dp), tier = GlassTier.ULTRA_THIN, borderWidth = 1.dp)
                            }
                        )
                        .clickable { onAudienceChange(key) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permissions Switches Card
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            tier = GlassTier.MEDIUM,
            borderWidth = 1.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SwitchRow("Allow Comments", allowComments, onAllowCommentsChange)
                SwitchRow("Allow Video Downloads", allowDownloads, onAllowDownloadsChange)
                SwitchRow("Allow Duet / Remix", allowRemix, onAllowRemixChange)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Uploading Progress Indicator
        if (isUploading) {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MgCyan,
                    trackColor = MgDarkSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(uploadStatusText, color = MgCyan, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Publish Primary Button
        Button(
            onClick = onPublish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_video_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MgCyan),
            shape = RoundedCornerShape(12.dp),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("PUBLISH TO MG", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = Color.White, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = MgCyan,
                uncheckedThumbColor = MgTextSecondary,
                uncheckedTrackColor = MgDarkSurfaceVariant
            )
        )
    }
}
