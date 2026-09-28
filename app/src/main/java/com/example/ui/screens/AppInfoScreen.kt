package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.HyperLime
import com.example.ui.theme.ObsidianNavy
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StravaOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.glassmorphic

enum class AppInfoType {
    ABOUT_APP,
    PRIVACY_POLICY,
    APP_VERSION
}

@Composable
fun AppInfoScreen(
    type: AppInfoType,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    BackHandler {
        onNavigateBack()
    }

    val headerTitle = when (type) {
        AppInfoType.ABOUT_APP -> "About This App"
        AppInfoType.PRIVACY_POLICY -> "Privacy Policy & Terms"
        AppInfoType.APP_VERSION -> "App Version"
    }

    val headerSubtitle = when (type) {
        AppInfoType.ABOUT_APP -> "AUTHOR & OPEN SOURCE ARCHITECTURE"
        AppInfoType.PRIVACY_POLICY -> "USER AGREEMENT & ON-DEVICE SAFETY"
        AppInfoType.APP_VERSION -> "SYSTEM & BUILD TELEMETRY"
    }

    val themeColor = when (type) {
        AppInfoType.ABOUT_APP -> HyperLime
        AppInfoType.PRIVACY_POLICY -> CyberCyan
        AppInfoType.APP_VERSION -> StravaOrange
    }

    val heroIcon = when (type) {
        AppInfoType.ABOUT_APP -> Icons.Default.DirectionsBike
        AppInfoType.PRIVACY_POLICY -> Icons.Default.Security
        AppInfoType.APP_VERSION -> Icons.Default.Numbers
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianNavy)
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp
            )
    ) {
        // ==========================================
        // 1. TOP BAR (BACK BUTTON & CATEGORY LABEL)
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .glassmorphic(cornerRadius = 21.dp, backgroundColor = Color(0xCC131A29))
                    .clickable { onNavigateBack() }
                    .testTag("app_info_back_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = headerSubtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = themeColor,
                letterSpacing = 1.2.sp
            )

            // Balance spacer
            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 2. SCROLLABLE UNIFIED CONTENT CONTAINER
        // ==========================================
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Hero Icon & Title Section (Shared Template)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.15f))
                        .border(1.5.dp, themeColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = heroIcon,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = "BikeRoute Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // CONTENT SPECIFIC TO TYPE
            // ==========================================
            when (type) {
                AppInfoType.ABOUT_APP -> {
                    AboutAppContent(context = context)
                }
                AppInfoType.PRIVACY_POLICY -> {
                    PrivacyPolicyContent()
                }
                AppInfoType.APP_VERSION -> {
                    AppVersionContent()
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ==========================================
// 1. ABOUT THIS APP CONTENT
// ==========================================
@Composable
private fun AboutAppContent(context: Context) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Author & GitHub Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0xD9121826),
                    borderColor = GlassBorderSubtle
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(HyperLime.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Author",
                                tint = HyperLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "LEAD AUTHOR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = TextMuted
                            )
                            Text(
                                text = "@irfanrev",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = PureWhite
                            )
                        }
                    }

                    // Open GitHub Link Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(HyperLime)
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/irfanrev"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Opening github.com/irfanrev", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("open_github_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = ObsidianNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GITHUB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = ObsidianNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "GitHub Profile: github.com/irfanrev",
                    style = MaterialTheme.typography.bodySmall,
                    color = HyperLime,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // App Mission Card
        InfoSectionCard(
            title = "THE BIKEROUTE VISION",
            description = "BikeRoute was engineered to solve a common cyclist challenge: finding routes that minimize grueling hill climbs without needing expensive subscriptions or API keys.\n\nDesigned for modern cyclists with high-contrast Strava-inspired aesthetics, local Room data persistence, and native 9:16 Instagram Story telemetry sharing."
        )

        // Architecture Highlights Card
        InfoSectionCard(
            title = "ZERO-COST CLEAN ARCHITECTURE",
            description = "• OpenStreetMap (osmdroid) for open vector mapping\n• OSRM Open Source Routing Machine for smart cyclist paths\n• Open-Elevation API & Topographic Grade calculations\n• Android Foreground Service for persistent offline GPS logging\n• 100% on-device SQLite Room database"
        )
    }
}

// ==========================================
// 2. PRIVACY POLICY & TERMS CONTENT
// ==========================================
@Composable
private fun PrivacyPolicyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Privacy Guarantee Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0xD9121826),
                    borderColor = CyberCyan.copy(alpha = 0.35f)
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Safe",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "100% LOCAL PRIVACY GUARANTEE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = CyberCyan
                    )
                    Text(
                        text = "Zero Cloud Tracking • Zero Ads",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
            }
        }

        InfoSectionCard(
            title = "1. LOCATION DATA USAGE",
            description = "BikeRoute requires location permissions (ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION) solely to calculate your speed, distance, elevation gain, and draw your route trail.\n\nLocation updates run in a Foreground Service only while an active ride is recording. No location data is sent to external servers or advertisers."
        )

        InfoSectionCard(
            title = "2. LOCAL DATA STORAGE",
            description = "All ride histories, coordinate trackpoints, and telemetry stats are persisted locally on your device in an encrypted Room SQLite database. You retain 100% control to export GPX files, save images, or delete rides at any time."
        )

        InfoSectionCard(
            title = "3. TERMS OF USE & RIDING SAFETY",
            description = "By using BikeRoute, you agree that cycling involves inherent outdoor risks. Always keep your attention focused on traffic, obey road regulations, wear a helmet, and avoid operating your device while pedaling."
        )
    }
}

// ==========================================
// 3. APP VERSION CONTENT
// ==========================================
@Composable
private fun AppVersionContent() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Version Hero Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(
                    cornerRadius = 24.dp,
                    backgroundColor = Color(0xD9121826),
                    borderColor = StravaOrange.copy(alpha = 0.35f)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CURRENT VERSION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = TextMuted
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = StravaOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(Build ${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(HyperLime.copy(alpha = 0.18f))
                            .border(1.dp, HyperLime.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LATEST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = HyperLime
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Release Track: Production Ready (Target SDK 36)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Framework Specs
        InfoSectionCard(
            title = "SYSTEM SPECIFICATIONS",
            description = "• Framework: Jetpack Compose (Kotlin 2.0+)\n• Architecture: MVVM + Clean Repository Pattern\n• Map Engine: osmdroid Mapnik Dark Matrix\n• Database: AndroidX Room Local Persistence\n• Minimum SDK: Android 7.0 (API 24)"
        )

        // Version 2.0 Changelog Card
        InfoSectionCard(
            title = "VERSION 2.0 CHANGELOG",
            description = "• Complete Gen Z Athletic UI revamp (Deep Obsidian & Hyper Lime)\n• Interactive Canvas Elevation Profile with cubic curves & peak markers\n• 3 Custom 9:16 Instagram Story export templates\n• Live in-sheet 9:16 image preview & Gallery download engine\n• About App, Privacy Agreement, and App Version hub"
        )
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                cornerRadius = 20.dp,
                backgroundColor = Color(0xCC131A29),
                borderColor = GlassBorderSubtle
            )
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = PureWhite,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}
