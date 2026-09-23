package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.Web2ApkViewModel
import com.example.ui.components.parseHexColor
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AppPreviewTestScreen(
    viewModel: Web2ApkViewModel,
    onBack: () -> Unit
) {
    val draft by viewModel.draftProject.collectAsState()
    val previewDevice by viewModel.previewDeviceType.collectAsState()
    val orientation by viewModel.previewOrientation.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    val statusBarColor = remember(draft.statusBarColorHex) { parseHexColor(draft.statusBarColorHex) }

    // Intercept back button for in-webview navigation or exiting fullscreen
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else if (isFullscreen) {
            isFullscreen = false
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Live App Player",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "${draft.name} (${if (previewDevice == "TV") "Android TV" else "Mobile $orientation"})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Fullscreen standalone app toggle
                        IconButton(onClick = { isFullscreen = true }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen Standalone Mode", tint = EmeraldAccent)
                        }

                        IconButton(onClick = {
                            viewModel.setPreviewDevice(if (previewDevice == "PHONE") "TV" else "PHONE")
                        }) {
                            Icon(
                                imageVector = if (previewDevice == "TV") Icons.Default.Smartphone else Icons.Default.Tv,
                                contentDescription = "Toggle TV/Phone",
                                tint = TechBlueLight
                            )
                        }

                        if (previewDevice == "PHONE") {
                            IconButton(onClick = { viewModel.togglePreviewOrientation() }) {
                                Icon(Icons.Default.ScreenRotation, contentDescription = "Rotate", tint = TechBlueLight)
                            }
                        }

                        IconButton(onClick = { webViewInstance?.reload() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFullscreen) Color.Black else Color(0xFF030712))
                .padding(if (isFullscreen) PaddingValues(0.dp) else paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val isLandscape = orientation == "LANDSCAPE" || previewDevice == "TV"
            val frameModifier = if (isFullscreen) {
                Modifier.fillMaxSize()
            } else if (isLandscape) {
                Modifier
                    .fillMaxWidth(0.96f)
                    .heightIn(max = 300.dp)
            } else {
                Modifier
                    .fillMaxWidth(0.88f)
                    .fillMaxHeight(0.94f)
            }

            // Simulated Device Shell or Fullscreen Canvas
            Box(
                modifier = frameModifier
                    .then(
                        if (!isFullscreen) {
                            Modifier
                                .clip(RoundedCornerShape(if (previewDevice == "TV") 16.dp else 28.dp))
                                .border(
                                    width = if (previewDevice == "TV") 8.dp else 6.dp,
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(if (previewDevice == "TV") 16.dp else 28.dp)
                                )
                        } else {
                            Modifier
                        }
                    )
                    .background(Color.Black)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Simulated Android Status Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isFullscreen) 28.dp else 24.dp)
                            .background(statusBarColor)
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (previewDevice == "TV") "Android TV 14" else draft.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Icon(Icons.Default.BatteryFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }

                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(3.dp),
                            color = TechBluePrimary
                        )
                    }

                    // Android WebView inside player
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = draft.jsEnabled
                                settings.domStorageEnabled = draft.domStorageEnabled
                                settings.setSupportZoom(draft.zoomEnabled)
                                settings.builtInZoomControls = draft.zoomEnabled
                                settings.displayZoomControls = false
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isLoading = true
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                    }
                                }

                                if (draft.sourceType == "URL" && draft.sourceUrl.isNotBlank()) {
                                    loadUrl(draft.sourceUrl)
                                } else {
                                    loadDataWithBaseURL(
                                        null,
                                        """
                                        <!DOCTYPE html>
                                        <html>
                                        <head>
                                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                            <style>
                                                body {
                                                    background: #0F172A;
                                                    color: #F8FAFC;
                                                    font-family: system-ui, -apple-system, sans-serif;
                                                    text-align: center;
                                                    padding: 40px 16px;
                                                    margin: 0;
                                                }
                                                .card {
                                                    background: #1E293B;
                                                    border-radius: 16px;
                                                    padding: 24px;
                                                    margin-top: 20px;
                                                    border: 1px solid #334155;
                                                }
                                                h2 { color: #38BDF8; margin-top: 0; }
                                                p { color: #94A3B8; font-size: 14px; line-height: 1.5; }
                                                .btn {
                                                    background: #2563EB;
                                                    color: white;
                                                    border: none;
                                                    padding: 10px 20px;
                                                    border-radius: 8px;
                                                    font-weight: bold;
                                                    cursor: pointer;
                                                    margin-top: 12px;
                                                }
                                            </style>
                                        </head>
                                        <body>
                                            <h2>${draft.name}</h2>
                                            <p>Web2APK Offline HTML5 Player</p>
                                            <div class="card">
                                                <p>Entry File: <code>${draft.startHtmlFile}</code></p>
                                                <p>Package: <code>${draft.packageName}</code></p>
                                                <button class="btn" onclick="alert('Offline Web App running inside standalone player!')">Test Interaction</button>
                                            </div>
                                        </body>
                                        </html>
                                        """.trimIndent(),
                                        "text/html",
                                        "utf-8",
                                        null
                                    )
                                }
                                webViewInstance = this
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }

            // Floating controls overlay when in Fullscreen mode
            if (isFullscreen) {
                FloatingActionButton(
                    onClick = { isFullscreen = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .size(46.dp),
                    shape = CircleShape,
                    containerColor = TechBluePrimary.copy(alpha = 0.85f),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen")
                }
            }
        }
    }
}
