package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MgViewModel
import com.example.ui.components.GlassTier
import com.example.ui.components.LgMonogramLogo
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.liquidGlass
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkBackground
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgGreen
import com.example.ui.theme.MgHeartRed
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextMuted
import com.example.ui.theme.MgTextSecondary
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

import androidx.compose.runtime.collectAsState
import com.example.util.AuthValidator
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: MgViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val verificationPending by viewModel.verificationPending.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var verificationStatusMessage by remember { mutableStateOf<String?>(null) }
    var resendCooldownSeconds by remember { mutableIntStateOf(0) }

    var usernameStatus by remember { mutableStateOf<UsernameStatus>(UsernameStatus.Empty) }
    var usernameSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }

    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Cooldown timer for resending verification email
    LaunchedEffect(resendCooldownSeconds) {
        if (resendCooldownSeconds > 0) {
            delay(1000L)
            resendCooldownSeconds--
        }
    }

    // Live username availability check
    LaunchedEffect(username) {
        val clean = username.trim().removePrefix("@")
        if (clean.length < 3) {
            usernameStatus = if (clean.isEmpty()) UsernameStatus.Empty else UsernameStatus.TooShort
            usernameSuggestions = emptyList()
        } else {
            val available = viewModel.checkUsernameAvailability(clean)
            if (available) {
                usernameStatus = UsernameStatus.Available
                usernameSuggestions = emptyList()
            } else {
                usernameStatus = UsernameStatus.Taken
                usernameSuggestions = viewModel.suggestUsernames(clean)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo & Header
            LgMonogramLogo(size = 68.dp, animatedGlow = true)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Welcome to LG",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Create. Discover. Connect.",
                color = MgCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Google Sign-In Primary Button
            Button(
                onClick = {
                    val activity = context as? Activity
                    val credentialManager = CredentialManager.create(context)
                    if (activity != null) {
                        isLoading = true
                        errorMessage = null
                        onGoogleSignInClicked(
                            context = activity,
                            credentialManager = credentialManager,
                            onAuthSuccess = { fbUser ->
                                isLoading = false
                                viewModel.onFirebaseUserAuthenticated(fbUser) {
                                    onAuthSuccess()
                                }
                            },
                            onAuthError = { err ->
                                isLoading = false
                                errorMessage = err
                                showGoogleAccountPicker = true
                            },
                            scope = coroutineScope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    } else {
                        showGoogleAccountPicker = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_signin_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF4285F4),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFF262C3A))
                )
                Text(
                    text = "  OR CONTINUE WITH EMAIL  ",
                    color = MgTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(Color(0xFF262C3A))
                )
            }

            if (verificationPending != null) {
                EmailVerificationCard(
                    email = verificationPending?.email ?: email,
                    isLoading = isLoading,
                    resendCooldownSeconds = resendCooldownSeconds,
                    statusMessage = verificationStatusMessage,
                    errorMessage = errorMessage,
                    onCheckStatus = {
                        isLoading = true
                        errorMessage = null
                        verificationStatusMessage = null
                        viewModel.checkEmailVerification { result ->
                            isLoading = false
                            if (result.isSuccess) {
                                onAuthSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Email is not verified yet. Please check your inbox and tap the link."
                            }
                        }
                    },
                    onResend = {
                        isLoading = true
                        errorMessage = null
                        verificationStatusMessage = null
                        viewModel.resendVerificationEmail { result ->
                            isLoading = false
                            if (result.isSuccess) {
                                resendCooldownSeconds = 60
                                verificationStatusMessage = "A fresh verification email was sent! Please check your inbox."
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to resend verification email."
                            }
                        }
                    },
                    onBackToSignIn = {
                        viewModel.clearVerificationPending()
                        selectedTab = 0
                        errorMessage = null
                        verificationStatusMessage = null
                    }
                )
            } else {
                // Tab Switcher (Sign In vs Create Account)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MgDarkSurface,
                    contentColor = MgCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MgCyan,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            errorMessage = null
                            verificationStatusMessage = null
                        },
                        text = {
                            Text(
                                "Sign In",
                                color = if (selectedTab == 0) Color.White else MgTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            errorMessage = null
                            verificationStatusMessage = null
                        },
                        text = {
                            Text(
                                "Create Account",
                                color = if (selectedTab == 1) Color.White else MgTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Error Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MgHeartRed.copy(alpha = 0.15f))
                            .border(1.dp, MgHeartRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MgHeartRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = MgHeartRed,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Registration specific fields
                if (selectedTab == 1) {
                    // Display Name
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Alex Rivera") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MgTextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_display_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = MgDarkSurface,
                            unfocusedContainerColor = MgDarkSurface,
                            focusedBorderColor = MgCyan,
                            unfocusedBorderColor = Color(0xFF2B3346)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username with live check
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        placeholder = { Text("e.g. alexrivera") },
                        prefix = { Text("@", color = MgCyan, fontWeight = FontWeight.Bold) },
                        trailingIcon = {
                            when (usernameStatus) {
                                UsernameStatus.Available -> Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = MgGreen)
                                UsernameStatus.Taken -> Icon(Icons.Default.Warning, contentDescription = "Taken", tint = MgHeartRed)
                                else -> null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_username"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = MgDarkSurface,
                            unfocusedContainerColor = MgDarkSurface,
                            focusedBorderColor = if (usernameStatus == UsernameStatus.Taken) MgHeartRed else MgCyan,
                            unfocusedBorderColor = Color(0xFF2B3346)
                        ),
                        singleLine = true
                    )

                    // Username suggestions if taken
                    if (usernameStatus == UsernameStatus.Taken && usernameSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Username already taken. Available options:",
                                color = MgHeartRed,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                usernameSuggestions.forEach { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MgDarkSurfaceVariant)
                                            .border(1.dp, MgCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                            .clickable { username = suggestion }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("@$suggestion", color = MgCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("name@gmail.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MgTextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = MgDarkSurface,
                        unfocusedContainerColor = MgDarkSurface,
                        focusedBorderColor = MgCyan,
                        unfocusedBorderColor = Color(0xFF2B3346)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MgTextSecondary) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = MgTextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (selectedTab == 1) ImeAction.Next else ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = MgDarkSurface,
                        unfocusedContainerColor = MgDarkSurface,
                        focusedBorderColor = MgCyan,
                        unfocusedBorderColor = Color(0xFF2B3346)
                    ),
                    singleLine = true
                )

                if (selectedTab == 1) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MgTextSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle confirm password visibility",
                                    tint = MgTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_confirm_password"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = MgDarkSurface,
                            unfocusedContainerColor = MgDarkSurface,
                            focusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword != password) MgHeartRed else MgCyan,
                            unfocusedBorderColor = Color(0xFF2B3346)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Password requirements guide
                    PasswordRequirementsGuide(password = password, confirmPassword = confirmPassword)

                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    // Forgot Password link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text("Forgot password?", color = MgCyan, fontSize = 12.sp)
                        }
                    }
                }

                // Submit Button (Sign In or Register)
                Button(
                    onClick = {
                        errorMessage = null
                        verificationStatusMessage = null

                        if (selectedTab == 0) {
                            val emailVal = AuthValidator.validateEmail(email)
                            if (!emailVal.isValid) {
                                errorMessage = emailVal.errorMessage
                                return@Button
                            }
                            if (password.isBlank()) {
                                errorMessage = "Please enter your password."
                                return@Button
                            }

                            isLoading = true
                            viewModel.loginWithEmail(email.trim(), password) { result ->
                                isLoading = false
                                if (result.isSuccess) {
                                    onAuthSuccess()
                                } else {
                                    val ex = result.exceptionOrNull()
                                    errorMessage = ex?.message ?: "Login failed."
                                }
                            }
                        } else {
                            // Strict client-side validation
                            val emailVal = AuthValidator.validateEmail(email)
                            if (!emailVal.isValid) {
                                errorMessage = emailVal.errorMessage
                                return@Button
                            }
                            val usernameVal = AuthValidator.validateUsername(username)
                            if (!usernameVal.isValid) {
                                errorMessage = usernameVal.errorMessage
                                return@Button
                            }
                            if (usernameStatus == UsernameStatus.Taken) {
                                errorMessage = "Username is already taken. Pick an available alternative."
                                return@Button
                            }
                            val passwordVal = AuthValidator.validatePassword(password)
                            if (!passwordVal.isValid) {
                                errorMessage = passwordVal.errorMessage
                                return@Button
                            }
                            val confirmVal = AuthValidator.validateConfirmPassword(password, confirmPassword)
                            if (!confirmVal.isValid) {
                                errorMessage = confirmVal.errorMessage
                                return@Button
                            }

                            isLoading = true
                            viewModel.registerWithEmail(email.trim(), password, username.trim(), displayName.trim()) { result ->
                                isLoading = false
                                if (result.isSuccess) {
                                    errorMessage = null
                                    resendCooldownSeconds = 60
                                    verificationStatusMessage = "A verification email has been dispatched to ${email.trim()}."
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed."
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_submit_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MgCyan),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (selectedTab == 0) "Sign In to LG" else "Create LG Account",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Demo Credentials Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MgDarkSurface)
                    .border(1.dp, Color(0xFF262E40), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MgCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Quick Access:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.loginWithGoogle("alex@tech.mg", "Alex Rivera") { onAuthSuccess() }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Creator Account", fontSize = 11.sp, color = MgCyan)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.loginWithGoogle("admin@lg.app", "LG Platform Admin") { onAuthSuccess() }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Admin Account", fontSize = 11.sp, color = MgPurple)
                        }
                    }
                }
            }
        }
    }

    // Google Account Picker Dialog
    if (showGoogleAccountPicker) {
        AlertDialog(
            onDismissRequest = { showGoogleAccountPicker = false },
            containerColor = MgDarkSurface,
            title = {
                Text("Choose a Google Account", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    GoogleAccountRow("adv.chaudharymustafa@gmail.com", "Mustafa Chaudhary") {
                        showGoogleAccountPicker = false
                        viewModel.loginWithGoogle("adv.chaudharymustafa@gmail.com", "Mustafa Chaudhary") {
                            onAuthSuccess()
                        }
                    }
                    GoogleAccountRow("creator.lg@gmail.com", "LG Creator") {
                        showGoogleAccountPicker = false
                        viewModel.loginWithGoogle("creator.lg@gmail.com", "LG Creator") {
                            onAuthSuccess()
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleAccountPicker = false }) {
                    Text("Cancel", color = MgTextSecondary)
                }
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(email) }
        var resetSent by remember { mutableStateOf(false) }
        var resetError by remember { mutableStateOf<String?>(null) }
        var isResetting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = MgDarkSurface,
            title = {
                Text("Reset Password", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    if (resetSent) {
                        Text(
                            "A secure recovery link has been dispatched to $resetEmail. Please check your inbox.",
                            color = MgGreen,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            "Enter your LG registered email address to receive password recovery instructions.",
                            color = MgTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = {
                                resetEmail = it
                                resetError = null
                            },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            isError = resetError != null
                        )
                        if (resetError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(resetError ?: "", color = MgHeartRed, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                if (!resetSent) {
                    Button(
                        onClick = {
                            val validation = AuthValidator.validateEmail(resetEmail)
                            if (!validation.isValid) {
                                resetError = validation.errorMessage
                                return@Button
                            }
                            isResetting = true
                            resetError = null
                            viewModel.sendPasswordReset(resetEmail.trim()) { res ->
                                isResetting = false
                                if (res.isSuccess) {
                                    resetSent = true
                                } else {
                                    resetError = res.exceptionOrNull()?.message ?: "Failed to send reset link."
                                }
                            }
                        },
                        enabled = !isResetting,
                        colors = ButtonDefaults.buttonColors(containerColor = MgCyan)
                    ) {
                        if (isResetting) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Send Link", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { showForgotPasswordDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MgCyan)
                    ) {
                        Text("Done", color = Color.Black)
                    }
                }
            },
            dismissButton = {
                if (!resetSent) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel", color = MgTextSecondary)
                    }
                }
            }
        )
    }
}

@Composable
private fun GoogleAccountRow(
    email: String,
    name: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MgDarkSurfaceVariant)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF4285F4)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(email, color = MgTextSecondary, fontSize = 12.sp)
        }
    }
}

enum class UsernameStatus {
    Empty, TooShort, Available, Taken
}

fun onGoogleSignInClicked(
    context: Activity,
    credentialManager: CredentialManager,
    onAuthSuccess: (com.google.firebase.auth.FirebaseUser) -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                val user = authResult.user
                if (user != null) {
                    onAuthSuccess(user)
                } else {
                    onAuthError("Firebase user is null after sign in")
                }
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

@Composable
private fun EmailVerificationCard(
    email: String,
    isLoading: Boolean,
    resendCooldownSeconds: Int,
    statusMessage: String?,
    errorMessage: String?,
    onCheckStatus: () -> Unit,
    onResend: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MgDarkSurface)
            .border(1.dp, MgCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MgCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MgCyan,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Verify Your Email",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A genuine Firebase verification email has been sent to:",
                color = MgTextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MgDarkSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = email,
                    color = MgCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Please open the verification link in your email inbox to activate your account. Once verified, tap the button below.",
                color = MgTextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusMessage,
                    color = MgGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MgHeartRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Check Status Button
            Button(
                onClick = onCheckStatus,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("check_verification_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MgCyan)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Verification Status", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Resend Email Button
            OutlinedButton(
                onClick = onResend,
                enabled = !isLoading && resendCooldownSeconds == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("resend_verification_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                val buttonText = if (resendCooldownSeconds > 0) {
                    "Resend in ${resendCooldownSeconds}s"
                } else {
                    "Resend Verification Email"
                }
                Text(buttonText, color = if (resendCooldownSeconds == 0) MgCyan else MgTextMuted, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToSignIn) {
                Text("Back to Sign In", color = MgTextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PasswordRequirementsGuide(password: String, confirmPassword: String) {
    val lengthOk = password.length in 8..64
    val uppercaseOk = password.any { it.isUpperCase() }
    val lowercaseOk = password.any { it.isLowerCase() }
    val digitOk = password.any { it.isDigit() }
    val specialOk = password.any { !it.isLetterOrDigit() }
    val matchOk = password.isNotEmpty() && password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MgDarkSurfaceVariant.copy(alpha = 0.6f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Password Requirements:", color = MgTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RequirementItem(label = "8-64 chars", satisfied = lengthOk)
            RequirementItem(label = "Uppercase (A-Z)", satisfied = uppercaseOk)
            RequirementItem(label = "Lowercase (a-z)", satisfied = lowercaseOk)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RequirementItem(label = "Number (0-9)", satisfied = digitOk)
            RequirementItem(label = "Special (!@#$)", satisfied = specialOk)
            if (confirmPassword.isNotEmpty()) {
                RequirementItem(label = "Passwords match", satisfied = matchOk)
            }
        }
    }
}

@Composable
private fun RequirementItem(label: String, satisfied: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (satisfied) MgGreen else MgTextMuted)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (satisfied) MgGreen else MgTextMuted,
            fontSize = 10.sp,
            fontWeight = if (satisfied) FontWeight.Bold else FontWeight.Normal
        )
    }
}
