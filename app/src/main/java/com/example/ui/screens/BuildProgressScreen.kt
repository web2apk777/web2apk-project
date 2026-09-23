package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.builder.ActiveBuildProgress
import com.example.builder.BuildManager
import com.example.builder.BuildStageState
import com.example.ui.Web2ApkViewModel
import com.example.ui.components.GradientCard
import com.example.ui.theme.*
import java.io.File
import java.util.zip.ZipFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildProgressScreen(
    viewModel: Web2ApkViewModel,
    onBackToDashboard: () -> Unit,
    onLaunchApp: () -> Unit
) {
    val activeBuild by viewModel.activeBuild.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showInstallHelpDialog by remember { mutableStateOf(false) }
    var showCloudBuildDialog by remember { mutableStateOf(false) }
    var showLogsExpanded by remember { mutableStateOf(false) }

    // File picker to install already-compiled APK downloaded from GitHub or external server
    val pickApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            installApkFromUri(context, it)
        }
    }

    val build = activeBuild

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (build?.isSuccess == true) "APK Build Complete" else "Compiling Android APK",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        BuildManager.resetActiveBuild()
                        onBackToDashboard()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (build == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No active build pipeline in progress.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackToDashboard) {
                        Text("Return to Dashboard")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Progress Card
                item {
                    if (build.isSuccess) {
                        SuccessHeaderCard(build = build)
                    } else {
                        ActiveProgressHeaderCard(build = build)
                    }
                }

                // Action Buttons when Build Ready
                if (build.isSuccess && build.generatedApkPath != null) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Primary Action: Launch & Test App Immediately in Standalone Player
                            Button(
                                onClick = onLaunchApp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch & Test App Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        shareApk(context, build.generatedApkPath, build.project.name)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                    .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export ZIP", fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        val file = File(build.generatedApkPath)
                                        if (isCompiledBinaryApk(file)) {
                                            installApk(context, build.generatedApkPath) {
                                                showInstallHelpDialog = true
                                            }
                                        } else {
                                            // Show guided dialog explaining how to get compiled APK from GitHub Actions
                                            showCloudBuildDialog = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TechBluePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.InstallMobile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Install APK", fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(build.sha256Fingerprint))
                                        viewModel.showSnackbar("SHA-256 fingerprint copied!")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy SHA-256", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showCloudBuildDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Build & Install Help", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 10-Stage Visual Progression Pipeline
                item {
                    Text(
                        text = "Build Pipeline Stages",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(build.stages) { stage ->
                    BuildStageRow(stage = stage)
                }

                // Terminal Console Log Viewer
                item {
                    GradientCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = TechBlueLight)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Console Logs (${build.terminalLogs.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(onClick = { showLogsExpanded = !showLogsExpanded }) {
                                Text(if (showLogsExpanded) "Collapse" else "Expand", color = TechBlueLight)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = if (showLogsExpanded) 400.dp else 160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF030712))
                                .padding(12.dp)
                        ) {
                            val listState = rememberLazyListState()
                            LaunchedEffect(build.terminalLogs.size) {
                                if (build.terminalLogs.isNotEmpty()) {
                                    listState.animateScrollToItem(build.terminalLogs.size - 1)
                                }
                            }

                            LazyColumn(state = listState) {
                                items(build.terminalLogs) { logLine ->
                                    Text(
                                        text = logLine,
                                        color = if (logLine.contains("STAGE") || logLine.contains("✓")) EmeraldAccent else Color(0xFFD1D5DB),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cloud Build & Installation Helper Dialog
        if (showCloudBuildDialog) {
            AlertDialog(
                onDismissRequest = { showCloudBuildDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = TechBlueLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Android APK Installation Guide", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Why Android shows 'Problem parsing package':",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TechBlueLight
                        )
                        Text(
                            text = "Android OS requires compiled binary bytecode (classes.dex) and a compiled binary manifest to install an app. An uncompiled project bundle cannot be installed directly by Android PackageInstaller.",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        Text(
                            text = "Two Ways to Run & Install Your App:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("1. 🚀 Launch in Standalone Player", fontWeight = FontWeight.Bold, color = EmeraldAccent)
                                Text("Test your app immediately in full-screen mode on this phone with full JavaScript, zoom, and orientation support.", fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("2. ☁️ GitHub Actions Automated Build", fontWeight = FontWeight.Bold, color = TechBlueLight)
                                Text("Push your repository to GitHub. The included workflow (.github/workflows/build-apk.yml) automatically compiles, signs, and generates the real .apk in the GitHub Actions tab for direct download!", fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCloudBuildDialog = false
                            onLaunchApp()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Launch App Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCloudBuildDialog = false
                        pickApkLauncher.launch("application/vnd.android.package-archive")
                    }) {
                        Text("Select .APK File")
                    }
                }
            )
        }

        // Sideloading Instructions Dialog
        if (showInstallHelpDialog) {
            AlertDialog(
                onDismissRequest = { showInstallHelpDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = TechBlueLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sideloading Permissions", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("If Android blocks APK installation:")
                        Text("1. When the prompt appears, tap 'Settings'.", fontSize = 13.sp)
                        Text("2. Enable 'Allow from this source' for Web2APK Builder.", fontSize = 13.sp)
                        Text("3. Return to this screen and tap 'Install APK' again.", fontSize = 13.sp)
                    }
                },
                confirmButton = {
                    Button(onClick = { showInstallHelpDialog = false }) {
                        Text("Understood")
                    }
                }
            )
        }
    }
}

@Composable
fun SuccessHeaderCard(build: ActiveBuildProgress) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TechBluePrimary,
                        TechBlueDark.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldAccent.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "READY FOR DISTRIBUTION",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${build.project.name} Project Ready!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Package: ${build.project.packageName} (v${build.project.versionName})",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("PACKAGE SIZE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(build.apkSizeMb, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("ARCHITECTURE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("Universal Package", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column {
                    Text("TARGET", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                    Text("Android 14 (API 34)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActiveProgressHeaderCard(build: ActiveBuildProgress) {
    GradientCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Building '${build.project.name}'",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Stage ${build.currentStage} of ${build.totalStages}: ${build.stages.getOrNull(build.currentStage - 1)?.stageName ?: ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            CircularProgressIndicator(
                progress = { build.progressPercent },
                color = TechBluePrimary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { build.progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = TechBluePrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${(build.progressPercent * 100).toInt()}% completed",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TechBlueLight
        )
    }
}

@Composable
fun BuildStageRow(stage: BuildStageState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (stage.isInProgress) TechBluePrimary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        stage.isCompleted -> EmeraldAccent
                        stage.isInProgress -> TechBluePrimary
                        else -> Color.Gray.copy(alpha = 0.3f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (stage.isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            } else if (stage.isInProgress) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
            } else {
                Text("${stage.stageNumber}", fontSize = 11.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stage.stageName,
                fontWeight = if (stage.isInProgress) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (stage.isInProgress) TechBlueLight else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stage.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun isCompiledBinaryApk(file: File): Boolean {
    if (!file.exists() || file.length() < 1000) return false
    return try {
        ZipFile(file).use { zip ->
            zip.getEntry("classes.dex") != null && zip.getEntry("AndroidManifest.xml") != null
        }
    } catch (e: Exception) {
        false
    }
}

private fun shareApk(context: Context, apkPath: String, appName: String) {
    try {
        val file = File(apkPath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$appName Android Project Package")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Project Package via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot share package: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun installApk(context: Context, apkPath: String, onHelpRequested: () -> Unit) {
    try {
        val file = File(apkPath)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    } catch (e: Exception) {
        onHelpRequested()
    }
}

private fun installApkFromUri(context: Context, uri: Uri) {
    try {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot launch installer: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
