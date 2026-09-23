package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.BuildEntity
import com.example.ui.Web2ApkViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildHistoryScreen(
    viewModel: Web2ApkViewModel,
    onBack: () -> Unit
) {
    val builds by viewModel.builds.collectAsState()
    val context = LocalContext.current
    var selectedBuildForLogs by remember { mutableStateOf<BuildEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build History (${builds.size})", fontWeight = FontWeight.Bold) },
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
        if (builds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No builds generated yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(builds) { build ->
                    BuildHistoryItemCard(
                        build = build,
                        onShare = {
                            if (build.apkUrl.isNotBlank()) {
                                shareApkFile(context, build.apkUrl, build.appName)
                            }
                        },
                        onViewLogs = {
                            selectedBuildForLogs = build
                        },
                        onDelete = {
                            viewModel.deleteBuild(build)
                        }
                    )
                }
            }
        }

        // Build Logs Viewer Dialog
        selectedBuildForLogs?.let { build ->
            AlertDialog(
                onDismissRequest = { selectedBuildForLogs = null },
                title = {
                    Text("${build.appName} - Logs", fontWeight = FontWeight.Bold)
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF030712))
                            .padding(12.dp)
                    ) {
                        LazyColumn {
                            items(build.buildLogs.lines()) { line ->
                                Text(
                                    text = line,
                                    color = Color(0xFFD1D5DB),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedBuildForLogs = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun BuildHistoryItemCard(
    build: BuildEntity,
    onShare: () -> Unit,
    onViewLogs: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(build.startedAt))

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = build.appName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "${build.packageName} (v${build.versionName})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = build.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Date: $dateStr", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Size: ${build.apkSizeMb}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewLogs,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Logs", fontSize = 12.sp)
                    }

                    if (build.status == "COMPLETE") {
                        Button(
                            onClick = onShare,
                            colors = ButtonDefaults.buttonColors(containerColor = TechBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share APK", fontSize = 12.sp)
                        }
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusError)
                }
            }
        }
    }
}

private fun shareApkFile(context: Context, apkPath: String, appName: String) {
    try {
        val file = File(apkPath)
        if (!file.exists()) {
            Toast.makeText(context, "APK file not found on device storage", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$appName APK")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share APK via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
