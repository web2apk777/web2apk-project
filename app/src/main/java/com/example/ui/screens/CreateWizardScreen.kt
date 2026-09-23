package com.example.ui.screens

import android.webkit.URLUtil
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Web2ApkViewModel
import com.example.ui.components.ColorPickerRow
import com.example.ui.components.GradientCard
import com.example.ui.components.StepperHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWizardScreen(
    viewModel: Web2ApkViewModel,
    onBackToDashboard: () -> Unit,
    onStartBuild: (String) -> Unit,
    onOpenPreviewSimulator: () -> Unit
) {
    val draft by viewModel.draftProject.collectAsState()
    val currentStep by viewModel.currentWizardStep.collectAsState()
    val rightsAccepted by viewModel.rightsAccepted.collectAsState()

    var buildType by remember { mutableStateOf("DEBUG") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New APK Wizard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.saveDraftProject() }) {
                        Text("Save Draft", color = TechBlueLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.setWizardStep(currentStep - 1) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStep < 6) {
                        Button(
                            onClick = {
                                if (currentStep == 2 && !viewModel.isValidPackageName(draft.packageName)) {
                                    viewModel.showSnackbar("Please enter a valid package name (e.g. com.example.app)")
                                    return@Button
                                }
                                viewModel.setWizardStep(currentStep + 1)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TechBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("wizard_next_button")
                        ) {
                            Text("Next Step")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { onStartBuild(buildType) },
                            enabled = rightsAccepted,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldAccent,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("build_apk_final_button")
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Build APK Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            StepperHeader(
                currentStep = currentStep,
                onStepClick = { step -> viewModel.setWizardStep(step) }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    1 -> Step1SourceSelection(viewModel, draft, onOpenPreviewSimulator)
                    2 -> Step2AppInformation(viewModel, draft)
                    3 -> Step3IconAndSplash(viewModel, draft)
                    4 -> Step4AppearanceAndTv(viewModel, draft)
                    5 -> Step5WebViewSettings(viewModel, draft)
                    6 -> Step6ReviewAndBuild(
                        viewModel = viewModel,
                        draft = draft,
                        rightsAccepted = rightsAccepted,
                        buildType = buildType,
                        onBuildTypeChange = { buildType = it },
                        onOpenSimulator = onOpenPreviewSimulator
                    )
                }
            }
        }
    }
}

@Composable
fun Step1SourceSelection(
    viewModel: Web2ApkViewModel,
    draft: com.example.data.model.ProjectEntity,
    onOpenPreviewSimulator: () -> Unit
) {
    Text(
        text = "Step 1: Choose Source",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
    Text(
        text = "Select whether your Android app will load a live website URL or an offline HTML project package.",
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // Source Selector Cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SourceCard(
            title = "Website URL",
            desc = "Live web app or website",
            icon = Icons.Default.Language,
            isSelected = draft.sourceType == "URL",
            modifier = Modifier.weight(1f),
            onClick = { viewModel.updateSource("URL", draft.sourceUrl, draft.startHtmlFile) }
        )
        SourceCard(
            title = "HTML / ZIP",
            desc = "Offline HTML5 package",
            icon = Icons.Default.FolderZip,
            isSelected = draft.sourceType == "ZIP",
            modifier = Modifier.weight(1f),
            onClick = { viewModel.updateSource("ZIP", draft.sourceUrl, draft.startHtmlFile) }
        )
    }

    if (draft.sourceType == "URL") {
        GradientCard {
            Text("Website Address (URL)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = draft.sourceUrl,
                onValueChange = { viewModel.updateSource("URL", it, draft.startHtmlFile) },
                label = { Text("https://yourwebsite.com") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("source_url_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            val isHttp = draft.sourceUrl.startsWith("http://")
            val isValidUrl = URLUtil.isValidUrl(draft.sourceUrl)

            if (isHttp) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StatusWarning.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Warning: Cleartext HTTP is discouraged. Cleartext traffic flag will be enabled in AndroidManifest.",
                            fontSize = 12.sp,
                            color = StatusWarning
                        )
                    }
                }
            } else if (draft.sourceUrl.startsWith("https://")) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldAccent.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Secure HTTPS connection detected. Strict TLS verification active.",
                            fontSize = 12.sp,
                            color = EmeraldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onOpenPreviewSimulator,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test & Preview URL in Live Simulator")
            }
        }
    } else {
        GradientCard {
            Text("HTML5 / ZIP Package Configuration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Packaged directly into app assets. Completely offline-capable with zero external hosting dependencies.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = draft.startHtmlFile,
                onValueChange = { viewModel.updateSource("ZIP", draft.sourceUrl, it) },
                label = { Text("Entry HTML File") },
                supportingText = { Text("Default is index.html. The generator will link to file:///android_asset/${draft.startHtmlFile}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TechBluePrimary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = TechBlueLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Built-in Path Traversal Protection: Sanitizes file extracts to prevent root escape attacks.",
                        fontSize = 12.sp,
                        color = TechBlueLight
                    )
                }
            }
        }
    }
}

@Composable
fun Step2AppInformation(
    viewModel: Web2ApkViewModel,
    draft: com.example.data.model.ProjectEntity
) {
    Text(
        text = "Step 2: App Information",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    GradientCard {
        OutlinedTextField(
            value = draft.name,
            onValueChange = { viewModel.updateAppName(it) },
            label = { Text("Application Name") },
            placeholder = { Text("e.g. My Portfolio") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_name_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        val isValidPkg = viewModel.isValidPackageName(draft.packageName)
        OutlinedTextField(
            value = draft.packageName,
            onValueChange = { viewModel.updatePackageName(it) },
            label = { Text("Package Name (Unique ID)") },
            placeholder = { Text("com.web2apk.myapp") },
            supportingText = {
                if (!isValidPkg) {
                    Text("Invalid format. Must be reverse-domain like com.example.app", color = StatusError)
                } else {
                    Text("Auto-generated or custom. Must be globally unique for Play Store.", color = EmeraldAccent)
                }
            },
            isError = !isValidPkg,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("package_name_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = draft.versionName,
                onValueChange = { viewModel.updateVersionInfo(it, draft.versionCode, draft.developerName, draft.description) },
                label = { Text("Version Name") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = draft.versionCode.toString(),
                onValueChange = {
                    val code = it.toIntOrNull() ?: 1
                    viewModel.updateVersionInfo(draft.versionName, code, draft.developerName, draft.description)
                },
                label = { Text("Version Code") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = draft.developerName,
            onValueChange = { viewModel.updateVersionInfo(draft.versionName, draft.versionCode, it, draft.description) },
            label = { Text("Developer / Organization Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = draft.description,
            onValueChange = { viewModel.updateVersionInfo(draft.versionName, draft.versionCode, draft.developerName, it) },
            label = { Text("App Description") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2
        )
    }
}

@Composable
fun Step3IconAndSplash(
    viewModel: Web2ApkViewModel,
    draft: com.example.data.model.ProjectEntity
) {
    Text(
        text = "Step 3: App Icon & Splash Screen",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    GradientCard {
        Text("Custom App Icon & Mipmaps", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Web2APK Builder renders adaptive icons for mdpi, hdpi, xhdpi, xxhdpi, and xxxhdpi densities automatically.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconPreviewShape("Squircle", RoundedCornerShape(18.dp), draft)
            IconPreviewShape("Circle", CircleShape, draft)
            IconPreviewShape("Square", RoundedCornerShape(8.dp), draft)
        }
    }

    GradientCard {
        Text("Android 12+ SplashScreen Setup", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(12.dp))

        ColorPickerRow(
            label = "Splash Screen Background Color",
            currentColorHex = draft.splashBgColor,
            onColorSelected = {
                viewModel.updateSplashConfig(it, draft.splashDuration, draft.splashTitle, draft.splashSubtitle, draft.splashMode)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Display Duration: ${String.format("%.1f", draft.splashDuration)} seconds", fontSize = 14.sp)
        Slider(
            value = draft.splashDuration,
            onValueChange = {
                viewModel.updateSplashConfig(draft.splashBgColor, it, draft.splashTitle, draft.splashSubtitle, draft.splashMode)
            },
            valueRange = 1.0f..3.0f,
            steps = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Splash Branding Layout", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "LOGO_ONLY" to "Logo Only",
                "LOGO_NAME" to "Logo + Name",
                "LOGO_NAME_SUBTITLE" to "Full Branding"
            ).forEach { (mode, label) ->
                val isSelected = draft.splashMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.updateSplashConfig(draft.splashBgColor, draft.splashDuration, draft.splashTitle, draft.splashSubtitle, mode)
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
fun IconPreviewShape(
    label: String,
    shape: androidx.compose.ui.graphics.Shape,
    draft: com.example.data.model.ProjectEntity
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(shape)
                .background(TechBluePrimary)
                .border(2.dp, EmeraldAccent, shape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = draft.name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun Step4AppearanceAndTv(
    viewModel: Web2ApkViewModel,
    draft: com.example.data.model.ProjectEntity
) {
    Text(
        text = "Step 4: Appearance & Device Mode",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    // Android TV / Fire TV Mode Card
    GradientCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tv, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Android TV & Fire TV Mode", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enables Leanback launcher, remote D-pad focus traversal, large-screen responsive layout, and television banner.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = draft.isTvMode,
                onCheckedChange = { isChecked ->
                    viewModel.updateAppearance(
                        themeMode = draft.themeMode,
                        orientation = if (isChecked) "LANDSCAPE" else draft.orientation,
                        primary = draft.primaryColorHex,
                        accent = draft.accentColorHex,
                        statusBar = draft.statusBarColorHex,
                        navBar = draft.navBarColorHex,
                        isTvMode = isChecked
                    )
                },
                modifier = Modifier.testTag("tv_mode_switch")
            )
        }
    }

    GradientCard {
        Text("Screen Orientation", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("AUTO" to "Auto-Rotate", "PORTRAIT" to "Portrait Only", "LANDSCAPE" to "Landscape Only").forEach { (orient, label) ->
                val isSelected = draft.orientation == orient
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.updateAppearance(
                            draft.themeMode,
                            orient,
                            draft.primaryColorHex,
                            draft.accentColorHex,
                            draft.statusBarColorHex,
                            draft.navBarColorHex,
                            draft.isTvMode
                        )
                    },
                    label = { Text(label, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ColorPickerRow(
            label = "Primary Brand Color",
            currentColorHex = draft.primaryColorHex,
            onColorSelected = {
                viewModel.updateAppearance(draft.themeMode, draft.orientation, it, draft.accentColorHex, draft.statusBarColorHex, draft.navBarColorHex, draft.isTvMode)
            }
        )

        ColorPickerRow(
            label = "Accent / Button Color",
            currentColorHex = draft.accentColorHex,
            onColorSelected = {
                viewModel.updateAppearance(draft.themeMode, draft.orientation, draft.primaryColorHex, it, draft.statusBarColorHex, draft.navBarColorHex, draft.isTvMode)
            }
        )

        ColorPickerRow(
            label = "Android Status Bar Color",
            currentColorHex = draft.statusBarColorHex,
            onColorSelected = {
                viewModel.updateAppearance(draft.themeMode, draft.orientation, draft.primaryColorHex, draft.accentColorHex, it, draft.navBarColorHex, draft.isTvMode)
            }
        )
    }
}

@Composable
fun Step5WebViewSettings(
    viewModel: Web2ApkViewModel,
    draft: com.example.data.model.ProjectEntity
) {
    Text(
        text = "Step 5: WebView Settings & Permissions",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    GradientCard {
        Text("WebView Core Features", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(12.dp))

        ToggleRow("Enable JavaScript", "Required for modern interactive web apps", draft.jsEnabled) {
            viewModel.updateWebViewSettings(it, draft.domStorageEnabled, draft.zoomEnabled, draft.pullToRefreshEnabled, draft.fileUploadEnabled, draft.fullscreenVideoEnabled, draft.forcedAutoplay, draft.desktopModeEnabled, draft.backNavMode, draft.externalLinksMode)
        }
        ToggleRow("DOM / LocalStorage", "Persist user logins, sessions, and client databases", draft.domStorageEnabled) {
            viewModel.updateWebViewSettings(draft.jsEnabled, it, draft.zoomEnabled, draft.pullToRefreshEnabled, draft.fileUploadEnabled, draft.fullscreenVideoEnabled, draft.forcedAutoplay, draft.desktopModeEnabled, draft.backNavMode, draft.externalLinksMode)
        }
        ToggleRow("Pull-to-Refresh Gesture", "Allows user to swipe down from top to reload the page", draft.pullToRefreshEnabled) {
            viewModel.updateWebViewSettings(draft.jsEnabled, draft.domStorageEnabled, draft.zoomEnabled, it, draft.fileUploadEnabled, draft.fullscreenVideoEnabled, draft.forcedAutoplay, draft.desktopModeEnabled, draft.backNavMode, draft.externalLinksMode)
        }
        ToggleRow("Zoom Controls & Pinch", "Enables multitouch pinch zooming on pages", draft.zoomEnabled) {
            viewModel.updateWebViewSettings(draft.jsEnabled, draft.domStorageEnabled, it, draft.pullToRefreshEnabled, draft.fileUploadEnabled, draft.fullscreenVideoEnabled, draft.forcedAutoplay, draft.desktopModeEnabled, draft.backNavMode, draft.externalLinksMode)
        }
        ToggleRow("File Uploads & Media Chooser", "Enables <input type='file'> HTML dialogs", draft.fileUploadEnabled) {
            viewModel.updateWebViewSettings(draft.jsEnabled, draft.domStorageEnabled, draft.zoomEnabled, draft.pullToRefreshEnabled, it, draft.fullscreenVideoEnabled, draft.forcedAutoplay, draft.desktopModeEnabled, draft.backNavMode, draft.externalLinksMode)
        }
        ToggleRow("Forced Autoplay", "Autoplay media without user interaction", draft.forcedAutoplay) {
            viewModel.updateWebViewSettings(draft.jsEnabled, draft.domStorageEnabled, draft.zoomEnabled, draft.pullToRefreshEnabled, draft.fileUploadEnabled, draft.fullscreenVideoEnabled, it, draft.desktopModeEnabled, draft.backNavMode, draft.externalLinksMode)
        }
        ToggleRow("Desktop Mode", "Request desktop versions of websites", draft.desktopModeEnabled) {
            viewModel.updateWebViewSettings(draft.jsEnabled, draft.domStorageEnabled, draft.zoomEnabled, draft.pullToRefreshEnabled, draft.fileUploadEnabled, draft.fullscreenVideoEnabled, draft.forcedAutoplay, it, draft.backNavMode, draft.externalLinksMode)
        }
    }

    GradientCard {
        Text("Android System Permissions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Declare only permissions your web application genuinely requires.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        ToggleRow("Camera Access (HTML5 WebRTC)", "android.permission.CAMERA", draft.permCamera) {
            viewModel.updatePermissions(it, draft.permMic, draft.permLocation, draft.permNotifications)
        }
        ToggleRow("Microphone Access (Audio Record)", "android.permission.RECORD_AUDIO", draft.permMic) {
            viewModel.updatePermissions(draft.permCamera, it, draft.permLocation, draft.permNotifications)
        }
        ToggleRow("Geolocation (GPS Location)", "ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION", draft.permLocation) {
            viewModel.updatePermissions(draft.permCamera, draft.permMic, it, draft.permNotifications)
        }
        ToggleRow("Push Notifications", "android.permission.POST_NOTIFICATIONS", draft.permNotifications) {
            viewModel.updatePermissions(draft.permCamera, draft.permMic, draft.permLocation, it)
        }
    }

    GradientCard {
        Text("Navigation & Back Button Behavior", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Text("When user presses the device back button:", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "HISTORY" to "Web History First",
                "CONFIRM" to "Ask Confirm Exit",
                "IMMEDIATE" to "Exit Immediately"
            ).forEach { (mode, label) ->
                FilterChip(
                    selected = draft.backNavMode == mode,
                    onClick = {
                        viewModel.updateWebViewSettings(
                            draft.jsEnabled,
                            draft.domStorageEnabled,
                            draft.zoomEnabled,
                            draft.pullToRefreshEnabled,
                            draft.fileUploadEnabled,
                            draft.fullscreenVideoEnabled,
                            draft.forcedAutoplay,
                            draft.desktopModeEnabled,
                            mode,
                            draft.externalLinksMode
                        )
                    },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }
    }
}

@Composable
fun Step6ReviewAndBuild(
    viewModel: Web2ApkViewModel,
    draft: com.example.data.model.ProjectEntity,
    rightsAccepted: Boolean,
    buildType: String,
    onBuildTypeChange: (String) -> Unit,
    onOpenSimulator: () -> Unit
) {
    Text(
        text = "Step 6: Review & Build APK",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    // Configuration Summary Card
    GradientCard {
        Text("Build Specifications Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        SummaryRow("App Name", draft.name)
        SummaryRow("Package ID", draft.packageName)
        SummaryRow("Version", "${draft.versionName} (${draft.versionCode})")
        SummaryRow("Source Type", "${draft.sourceType} (${if (draft.sourceType == "URL") draft.sourceUrl else draft.startHtmlFile})")
        SummaryRow("Orientation", draft.orientation)
        SummaryRow("TV Mode", if (draft.isTvMode) "Enabled (Fire TV / Android TV)" else "Disabled")
        SummaryRow("Permissions", buildList {
            if (draft.permCamera) add("Camera")
            if (draft.permMic) add("Mic")
            if (draft.permLocation) add("Location")
            if (draft.permNotifications) add("Notifications")
        }.ifEmpty { listOf("None (Internet only)") }.joinToString(", "))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onOpenSimulator,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Devices, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Interactive Device Simulator")
        }
    }

    // Build Mode Selection
    GradientCard {
        Text("Compilation Target", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BuildTypeCard(
                title = "Debug APK",
                desc = "Fast build, signed with standard Android debug certificate for sideloading & testing.",
                isSelected = buildType == "DEBUG",
                modifier = Modifier.weight(1f),
                onClick = { onBuildTypeChange("DEBUG") }
            )
            BuildTypeCard(
                title = "Release APK",
                desc = "ProGuard optimized, ready for self-hosting, distribution, or release signing.",
                isSelected = buildType == "RELEASE",
                modifier = Modifier.weight(1f),
                onClick = { onBuildTypeChange("RELEASE") }
            )
        }
    }

    // MANDATORY USER RIGHTS NOTICE & ACCEPTANCE CHECKBOX
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = if (rightsAccepted) EmeraldAccent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (rightsAccepted) EmeraldAccent else StatusWarning.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = if (rightsAccepted) EmeraldAccent else StatusWarning)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Required Rights & Compliance Confirmation", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Users are responsible for ensuring they have the necessary rights and permissions to package, distribute, or display the websites, HTML files, images, code, trademarks, and other content they submit. Web2APK Builder does not grant copyright or distribution rights.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setRightsAccepted(!rightsAccepted) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rightsAccepted,
                    onCheckedChange = { viewModel.setRightsAccepted(it) },
                    colors = CheckboxDefaults.colors(checkedColor = EmeraldAccent),
                    modifier = Modifier.testTag("rights_acceptance_checkbox")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I confirm that I have the necessary rights or permission to package and distribute this content.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SourceCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, TechBluePrimary, RoundedCornerShape(16.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) TechBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) TechBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BuildTypeCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .then(if (isSelected) Modifier.border(2.dp, EmeraldAccent, RoundedCornerShape(14.dp)) else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EmeraldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}
