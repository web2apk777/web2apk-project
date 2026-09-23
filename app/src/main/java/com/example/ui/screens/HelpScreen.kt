package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class HelpGuide(
    val id: Int,
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val steps: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val guides = remember {
        listOf(
            HelpGuide(
                id = 1,
                title = "Converting a Website URL into an APK",
                summary = "Learn how to wrap responsive web apps, PWAs, or SaaS portals into standalone Android APKs.",
                icon = Icons.Default.Language,
                steps = listOf(
                    "Ensure your web app has a responsive mobile layout (viewport meta tag configured).",
                    "Choose HTTPS whenever possible. Web2APK builder enables strict TLS verification by default.",
                    "If your site requires camera, mic, or geolocation, check the corresponding permission boxes in Step 5.",
                    "Test external links: Configure whether external domains open inside your app or launch the device's native browser."
                )
            ),
            HelpGuide(
                id = 2,
                title = "Packaging Offline HTML/ZIP Projects",
                summary = "Build self-contained APKs that run without an active internet connection.",
                icon = Icons.Default.FolderZip,
                steps = listOf(
                    "Bundle all HTML, CSS, JavaScript, images, and fonts into a root directory.",
                    "Ensure your main entry point is named 'index.html' or specify the custom path in Step 1.",
                    "Use relative links (e.g. href='./about.html', src='images/logo.png') rather than absolute URLs.",
                    "Web2APK applies built-in Path Traversal Protection to prevent file escape attacks."
                )
            ),
            HelpGuide(
                id = 3,
                title = "Custom App Icons & Adaptive Densities",
                summary = "Master Android adaptive icons for circle, squircle, and rounded displays.",
                icon = Icons.Default.CropSquare,
                steps = listOf(
                    "Android 8.0+ uses Adaptive Icons consisting of a foreground and a background layer.",
                    "Keep crucial graphics within the inner 66dp safe zone of the 108dp icon canvas.",
                    "Web2APK Builder automatically renders all 5 standard mipmap densities: mdpi (48px), hdpi (72px), xhdpi (96px), xxhdpi (144px), and xxxhdpi (192px)."
                )
            ),
            HelpGuide(
                id = 4,
                title = "Android 12+ SplashScreen API Setup",
                summary = "Design an instant, seamless launch screen that respects modern Android guidelines.",
                icon = Icons.Default.Bolt,
                steps = listOf(
                    "Android 12 (API 31+) replaces legacy splash activities with the system windowSplashScreen API.",
                    "Select your brand background color in Step 3.",
                    "Web2APK auto-generates both res/values/themes.xml and res/values-v31/themes.xml with postSplashScreenTheme switching."
                )
            ),
            HelpGuide(
                id = 5,
                title = "Package Naming & Play Store Compliance",
                summary = "Select a unique, permanent identifier for your application.",
                icon = Icons.Default.Fingerprint,
                steps = listOf(
                    "Package names follow reverse-domain notation: com.company.appname.",
                    "Must contain at least two segments separated by a dot.",
                    "Must begin with a lowercase letter and contain only lowercase letters, numbers, or underscores.",
                    "Once published, a package name can never be changed in the Google Play Console."
                )
            ),
            HelpGuide(
                id = 6,
                title = "Troubleshooting & Build Debugging",
                summary = "Diagnose build errors, cleartext HTTP blocks, and WebView mixed-content warnings.",
                icon = Icons.Default.BugReport,
                steps = listOf(
                    "Cleartext HTTP: Android 9+ blocks unencrypted http:// connections by default. Use HTTPS or enable the cleartext flag.",
                    "CORS errors: If packaging local HTML that fetches remote APIs, ensure your backend server allows cross-origin requests.",
                    "Review the real-time Console Logs in the Build Progress tab for the exact Gradle task status."
                )
            ),
            HelpGuide(
                id = 7,
                title = "Installing Unknown-Source APKs Safely",
                summary = "A step-by-step guide to sideloading generated APKs onto Android phones and tablets.",
                icon = Icons.Default.InstallMobile,
                steps = listOf(
                    "Android restricts sideloading by default to prevent unauthorized installations.",
                    "When tapping 'Install APK', tap 'Settings' on the system security prompt.",
                    "Enable the toggle 'Allow from this source' for Web2APK Builder.",
                    "Return to the app and confirm the installation. The new app will appear in your launcher!"
                )
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Documentation", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Everything you need to know about turning websites and HTML projects into production Android APKs.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(guides) { guide ->
                HelpGuideCard(guide = guide)
            }
        }
    }
}

@Composable
fun HelpGuideCard(guide: HelpGuide) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TechBluePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = guide.icon,
                        contentDescription = null,
                        tint = TechBlueLight,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = guide.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = guide.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    guide.steps.forEachIndexed { index, step ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = TechBlueLight,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
