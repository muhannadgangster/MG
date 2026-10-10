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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun SettingsScreen(
    viewModel: MgViewModel,
    onBack: () -> Unit,
    onNavigateToLegal: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var displayName by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var isPrivate by remember { mutableStateOf(currentUser?.isPrivate ?: false) }
    var allowComments by remember { mutableStateOf(currentUser?.allowComments ?: true) }
    var allowMessages by remember { mutableStateOf(currentUser?.allowMessages ?: true) }
    var allowDownloads by remember { mutableStateOf(currentUser?.allowDownloads ?: true) }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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
            Text("Settings & Privacy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Edit Profile Card
            Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Button(
                        onClick = {
                            currentUser?.let { user ->
                                val updated = user.copy(displayName = displayName.trim(), bio = bio.trim())
                                viewModel.updateProfile(updated)
                                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MgCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Appearance & Internationalization
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val selectedLanguage by viewModel.selectedLanguage.collectAsState()
            var showLanguageDialog by remember { mutableStateOf(false) }

            Text("Appearance & Language", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingsSwitchRow("Dark Theme", "Sleek pitch-black luxury aesthetic", isDarkMode) {
                        viewModel.setDarkMode(it)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Language", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Global internationalization", color = MgTextSecondary, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedLanguage, color = MgCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MgTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Privacy Controls
            Text("Privacy & Safety", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingsSwitchRow("Private Account", "Only approved followers can view your videos", isPrivate) {
                        isPrivate = it
                        currentUser?.let { u -> viewModel.updateProfile(u.copy(isPrivate = it)) }
                    }
                    SettingsSwitchRow("Allow Comments", "Enable public comments on your content", allowComments) {
                        allowComments = it
                        currentUser?.let { u -> viewModel.updateProfile(u.copy(allowComments = it)) }
                    }
                    SettingsSwitchRow("Direct Messages", "Allow friends & creators to message you", allowMessages) {
                        allowMessages = it
                        currentUser?.let { u -> viewModel.updateProfile(u.copy(allowMessages = it)) }
                    }
                    SettingsSwitchRow("Allow Downloads", "Allow others to download your public videos", allowDownloads) {
                        allowDownloads = it
                        currentUser?.let { u -> viewModel.updateProfile(u.copy(allowDownloads = it)) }
                    }
                }
            }

            // Legal & Policies
            Text("About & Legal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
            ) {
                Column {
                    SettingsLinkRow("MG Platform Showcase & Landing", Icons.Default.Info) { onNavigateToLegal("landing") }
                    SettingsLinkRow("Terms of Service", Icons.Default.Description) { onNavigateToLegal("terms") }
                    SettingsLinkRow("Privacy Policy", Icons.Default.PrivacyTip) { onNavigateToLegal("privacy") }
                    SettingsLinkRow("Community Guidelines", Icons.Default.Gavel) { onNavigateToLegal("guidelines") }
                    SettingsLinkRow("Copyright Policy", Icons.Default.Security) { onNavigateToLegal("copyright") }
                    SettingsLinkRow("Cookie Policy", Icons.Default.Description) { onNavigateToLegal("cookies") }
                    SettingsLinkRow("Contact MG Support", Icons.Default.Person) { onNavigateToLegal("contact") }
                    SettingsLinkRow("About MG", Icons.Default.Info) { onNavigateToLegal("about") }
                }
            }

            if (showLanguageDialog) {
                val languages = listOf("English", "Urdu (اردو)", "Arabic (العربية)", "Spanish (Español)", "French (Français)", "German (Deutsch)", "Chinese (中文)", "Turkish (Türkçe)")
                AlertDialog(
                    onDismissRequest = { showLanguageDialog = false },
                    containerColor = MgDarkSurface,
                    title = { Text("Select Language", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            languages.forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.setLanguage(lang.substringBefore(" "))
                                            Toast.makeText(context, "Language set to ${lang.substringBefore(" ")}", Toast.LENGTH_SHORT).show()
                                            showLanguageDialog = false
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(lang, color = if (selectedLanguage.startsWith(lang.substringBefore(" "))) MgCyan else Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel", color = MgTextSecondary) }
                    }
                )
            }

            // Account Actions
            Text("Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MgDarkSurface)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.logout() }
                            .padding(16.dp)
                            .testTag("logout_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Log Out", tint = MgCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Log Out", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF262E3E)))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteAccountDialog = true }
                            .padding(16.dp)
                            .testTag("delete_account_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account", tint = MgHeartRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete Account", color = MgHeartRed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = MgDarkSurface,
            icon = {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MgHeartRed, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Delete MG Account?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Warning: This action is permanent and irreversible. All your uploaded videos, followers, likes, drafts, and direct messages will be deleted from MG systems.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount { success ->
                            if (success) {
                                Toast.makeText(context, "Account deleted.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MgHeartRed)
                ) {
                    Text("Permanently Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = MgTextSecondary)
                }
            }
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = MgTextSecondary, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
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

@Composable
fun SettingsLinkRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MgTextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 14.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MgTextSecondary, modifier = Modifier.size(18.dp))
    }
}
