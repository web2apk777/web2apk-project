package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Web2ApkViewModel
import com.example.ui.components.GradientCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: Web2ApkViewModel,
    onBack: () -> Unit
) {
    val projects by viewModel.projects.collectAsState()
    val builds by viewModel.builds.collectAsState()
    val maxUpload by viewModel.adminMaxUploadMb.collectAsState()
    val timeoutMin by viewModel.adminBuildTimeoutMin.collectAsState()
    val maintenance by viewModel.adminMaintenanceMode.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    var tempMaxUpload by remember(maxUpload) { mutableStateOf(maxUpload.toString()) }
    var tempTimeout by remember(timeoutMin) { mutableStateOf(timeoutMin.toString()) }
    var tempMaintenance by remember(maintenance) { mutableStateOf(maintenance) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server & CI Admin Dashboard", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Server Health Card
            item {
                GradientCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (maintenance) StatusWarning else EmeraldAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (maintenance) "Server Mode: Maintenance" else "Engine Status: Healthy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TechBluePrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "WORKERS: 4 ONLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TechBlueLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Active Projects", "${projects.size}")
                        MetricItem("Builds Logged", "${builds.size}")
                        MetricItem("Avg Build Time", "36s")
                        MetricItem("Storage Used", "184 MB")
                    }
                }
            }

            // System Limits & Parameters
            item {
                Text(
                    text = "System Governance & Resource Limits",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                GradientCard {
                    OutlinedTextField(
                        value = tempMaxUpload,
                        onValueChange = { tempMaxUpload = it },
                        label = { Text("Maximum ZIP Upload Size (MB)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempTimeout,
                        onValueChange = { tempTimeout = it },
                        label = { Text("Build Process Timeout (Minutes)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Maintenance Mode", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Temporarily pause incoming build requests", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = tempMaintenance,
                            onCheckedChange = { tempMaintenance = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val upload = tempMaxUpload.toIntOrNull() ?: 50
                            val timeout = tempTimeout.toIntOrNull() ?: 45
                            viewModel.updateAdminConfig(upload, timeout, tempMaintenance)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechBluePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply System Configuration")
                    }
                }
            }

            // GitHub Actions Secrets Helper
            item {
                Text(
                    text = "GitHub Actions CI/CD Secrets Setup",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                GradientCard {
                    Text(
                        text = "To enable remote automated builds in your GitHub repository, configure these Secrets in Repository Settings -> Secrets and variables -> Actions:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecretItem(
                        name = "KEYSTORE_BASE64",
                        desc = "Base64-encoded release .jks or .keystore file for signing APKs.",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("KEYSTORE_BASE64"))
                            viewModel.showSnackbar("Secret name copied")
                        }
                    )
                    SecretItem(
                        name = "KEYSTORE_PASSWORD",
                        desc = "The master password protecting your release keystore file.",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("KEYSTORE_PASSWORD"))
                            viewModel.showSnackbar("Secret name copied")
                        }
                    )
                    SecretItem(
                        name = "KEY_ALIAS",
                        desc = "The alias assigned to your release signing key.",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("KEY_ALIAS"))
                            viewModel.showSnackbar("Secret name copied")
                        }
                    )
                    SecretItem(
                        name = "KEY_PASSWORD",
                        desc = "The individual key password matching KEY_ALIAS.",
                        onCopy = {
                            clipboardManager.setText(AnnotatedString("KEY_PASSWORD"))
                            viewModel.showSnackbar("Secret name copied")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SecretItem(name: String, desc: String, onCopy: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF030712))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TechBlueLight)
            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 11.sp, color = Color(0xFF9CA3AF))
    }
}
