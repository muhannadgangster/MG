package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgTextSecondary

@Composable
fun VideoLongPressDialog(
    onDismiss: () -> Unit,
    onNotInterested: () -> Unit,
    onCopyLink: () -> Unit,
    onSaveVideo: () -> Unit,
    onOpenReport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MgDarkSurface,
        title = {
            Text("Video Options", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogOptionRow(Icons.Default.NotInterested, "Not Interested", onNotInterested)
                DialogOptionRow(Icons.Default.ContentCopy, "Copy Link", onCopyLink)
                DialogOptionRow(Icons.Default.BookmarkBorder, "Save to Collection", onSaveVideo)
                DialogOptionRow(Icons.Default.Flag, "Report Video", onOpenReport, isDestructive = true)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MgTextSecondary)
            }
        }
    )
}

@Composable
fun ReportVideoDialog(
    videoTitle: String,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String) -> Unit
) {
    val reportReasons = listOf(
        "Spam or misleading",
        "Harassment or bullying",
        "Hate speech or symbols",
        "Violence or dangerous content",
        "Sexual or nudity content",
        "Copyright infringement",
        "Scam or fraud",
        "Other violation"
    )
    var selectedReason by remember { mutableStateOf(reportReasons[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MgDarkSurface,
        icon = {
            Icon(Icons.Default.Warning, contentDescription = "Report", tint = MgHeartRed, modifier = Modifier.size(32.dp))
        },
        title = {
            Text("Report Content", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column {
                Text(
                    text = "Help us keep MG safe. Why are you reporting \"${videoTitle.take(30)}\"?",
                    color = MgTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    reportReasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MgHeartRed,
                                    unselectedColor = MgTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = reason, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitReport(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = MgHeartRed)
            ) {
                Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MgTextSecondary)
            }
        }
    )
}

@Composable
private fun DialogOptionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isDestructive) MgHeartRed else MgCyan,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            color = if (isDestructive) MgHeartRed else Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
