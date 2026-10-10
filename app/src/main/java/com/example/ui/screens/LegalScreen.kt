package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MgMonogramLogo
import com.example.ui.theme.MgCyan
import com.example.ui.theme.MgDarkSurface
import com.example.ui.theme.MgDarkSurfaceVariant
import com.example.ui.theme.MgPitchBlack
import com.example.ui.theme.MgPurple
import com.example.ui.theme.MgTextSecondary

@Composable
fun LegalScreen(
    docType: String,
    onBack: () -> Unit
) {
    val title = when (docType) {
        "terms" -> "Terms of Service"
        "privacy" -> "Privacy Policy"
        "guidelines" -> "Community Guidelines"
        "copyright" -> "Copyright & IP Policy"
        "cookies" -> "Cookie Policy"
        "contact" -> "Contact MG Support"
        "landing" -> "MG Platform Overview"
        else -> "About MG Platform"
    }

    val content = when (docType) {
        "terms" -> """
            Welcome to MG (the "Platform"). By creating an account or accessing MG services, you agree to comply with and be bound by these Terms of Service.
            
            1. User Accounts
            To access features including video publishing, social feeds, and direct messaging, you must establish an account. You agree to provide accurate registration details and maintain the security of your credentials.
            
            2. Content Ownership & License
            You retain all copyright and intellectual property rights in the original video, audio, and graphics content you publish to MG. By sharing content, you grant MG a worldwide, royalty-free license to distribute, stream, display, and syndicate your content on the platform.
            
            3. Platform Conduct
            Users must not upload harmful, infringing, deceptive, spam, or unlawful material. Violation of these terms may result in content removal, account suspension, or permanent termination.
            
            4. Service Availability & Modifications
            MG continuously iterates video technologies and features. We reserve the right to modify or discontinue features with appropriate notice.
        """.trimIndent()

        "privacy" -> """
            MG Privacy Policy
            
            Your privacy and data security are central to the design of MG.
            
            1. Data Collection
            We collect the information you provide directly: username, display name, email, profile bio, uploaded video metadata, likes, comments, and direct message communications.
            
            2. Usage of Data
            Your data powers personalized "For You" recommendations, creator analytics, security verification, and platform integrity checks.
            
            3. Data Protection
            We employ modern encryption standards for media storage, authentication credentials, and database records. We never sell your personal contact records to third-party brokers.
            
            4. User Rights & Account Deletion
            You have full control over your privacy settings, including setting your account private and managing download permissions. You may permanently delete your account and all associated media directly within Settings.
        """.trimIndent()

        "guidelines" -> """
            MG Community Guidelines
            
            Our mission is to foster a safe, creative, and inspiring global short-form video ecosystem.
            
            1. Respect & Inclusion
            Harassment, hate speech, bullying, discrimination, and targeted abuse are strictly prohibited.
            
            2. Authentic Engagement
            Do not engage in artificial view generation, spam messaging, or coordinated manipulative behavior. MG uses automated anti-spam view verification algorithms.
            
            3. Safety First
            Content displaying dangerous activities without context, self-harm, violent extremism, or exploitation will be immediately removed and reported to authorities where required.
            
            4. Content Reporting
            Every video features a 1-tap Report menu. Our Trust & Safety moderation team reviews flagged submissions promptly.
        """.trimIndent()

        "copyright" -> """
            MG Copyright & Intellectual Property Policy
            
            MG respects the rights of artists, musicians, and creators worldwide.
            
            1. DMCA & International Copyright
            We expeditiously respond to notices of alleged copyright infringement under applicable international copyright frameworks.
            
            2. Submitting an Infringement Notice
            If your copyrighted music, video footage, or artwork has been reproduced on MG without authorization, submit a report via the in-app video report dialog selecting "Copyright infringement" or contact copyright@mg.app.
            
            3. Repeat Infringer Policy
            Accounts with repeated substantiated copyright violations are subject to feature restrictions or permanent banning.
        """.trimIndent()

        "cookies" -> """
            MG Cookie & Cache Policy
            
            1. Functional Caching
            MG stores local cache tokens, session identifiers, and cached video thumbnails on your device to guarantee instantaneous playback and reduce bandwidth consumption.
            
            2. Preferences
            Preferences including your Dark Mode toggle, volume state, and language selection are securely maintained on your device.
            
            3. Security Verification
            Security tokens verify session authenticity and prevent automated bot spam.
        """.trimIndent()

        "contact" -> """
            Contact MG Operations & Creator Support
            
            General Support: support@mg.app
            Creator Partnerships: creators@mg.app
            Trust & Safety Moderation: safety@mg.app
            Legal & Copyright: legal@mg.app
            
            Our global response team operates 24/7 across North America, Europe, and Asia.
        """.trimIndent()

        "landing" -> """
            MG — Create. Discover. Connect.
            
            A new generation social video platform built for creators and communities.
            
            • Discover: Dynamic, adaptive short-form video feeds engineered with low latency and anti-spam verification.
            • Create: Built-in Studio with precision timeline trimmer, 6 color presets, speed adjustments, and text overlays.
            • Connect: Real-time 1-on-1 direct messaging, nested video comment discussions, and community follower graphs.
            • Creator Studio: Full audience analytics, retention curves, top video metrics, and draft management.
            • Trust & Privacy: Granular audience visibility controls, one-tap reporting, and administrative moderation.
        """.trimIndent()

        else -> """
            About MG
            
            Create. Discover. Connect.
            
            MG is a next-generation international short-form video platform engineered from the ground up for high speed, creator monetization, and community depth.
            
            Built with modern Android Jetpack Compose, reactive Room architecture, and a streamlined dark-luxury design language, MG sets a new benchmark in mobile video entertainment.
            
            Version: 1.0.0 (Global Release)
            Engine: MG Video Core Architecture
            © 2026 MG International. All rights reserved.
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MgPitchBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
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
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (docType == "about" || docType == "landing") {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MgMonogramLogo(size = 72.dp, animatedGlow = true)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("MG", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                        Text("CREATE • DISCOVER • CONNECT", color = MgCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = content,
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
