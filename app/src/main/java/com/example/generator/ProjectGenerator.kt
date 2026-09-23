package com.example.generator

import com.example.data.model.ProjectEntity
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectGenerator {

    /**
     * Generates a complete, ready-to-compile Android project ZIP containing
     * all Kotlin source files, Gradle scripts, Manifest, and Resources.
     */
    fun generateProjectZip(project: ProjectEntity): ByteArray {
        val bos = ByteArrayOutputStream()
        val zos = ZipOutputStream(bos)

        val packagePath = project.packageName.replace('.', '/')

        // 1. Root build.gradle.kts
        addZipFile(zos, "build.gradle.kts", generateRootBuildGradle())

        // 2. settings.gradle.kts
        addZipFile(zos, "settings.gradle.kts", generateSettingsGradle(project.name))

        // 3. gradle.properties
        addZipFile(zos, "gradle.properties", generateGradleProperties())

        // 4. app/build.gradle.kts
        addZipFile(zos, "app/build.gradle.kts", generateAppBuildGradle(project))

        // 5. app/src/main/AndroidManifest.xml
        addZipFile(zos, "app/src/main/AndroidManifest.xml", generateAndroidManifest(project))

        // 6. Kotlin MainActivity
        addZipFile(zos, "app/src/main/java/$packagePath/MainActivity.kt", generateMainActivity(project))

        // 7. Resources
        addZipFile(zos, "app/src/main/res/values/strings.xml", generateStringsXml(project))
        addZipFile(zos, "app/src/main/res/values/colors.xml", generateColorsXml(project))
        addZipFile(zos, "app/src/main/res/values/themes.xml", generateThemesXml(project))
        addZipFile(zos, "app/src/main/res/values-v31/themes.xml", generateThemesV31Xml(project))
        addZipFile(zos, "app/src/main/res/drawable/ic_launcher_background.xml", generateIconBackgroundXml(project))
        addZipFile(zos, "app/src/main/res/drawable/ic_launcher_foreground.xml", generateIconForegroundXml())

        // 8. Sample Web asset if ZIP source
        if (project.sourceType == "ZIP") {
            addZipFile(zos, "app/src/main/assets/${project.startHtmlFile}", generateDefaultIndexHtml(project))
        }

        // 9. README
        addZipFile(zos, "README.md", generateProjectReadme(project))

        zos.finish()
        zos.close()
        return bos.toByteArray()
    }

    private fun addZipFile(zos: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    fun generateRootBuildGradle(): String = """
        // Top-level build file for generated Web2APK Project
        plugins {
            alias(libs.plugins.android.application) apply false
            alias(libs.plugins.kotlin.android) apply false
        }
    """.trimIndent()

    fun generateSettingsGradle(appName: String): String = """
        pluginManagement {
            repositories {
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }
        }
        rootProject.name = "${appName.replace("\"", "\\\"")}"
        include(":app")
    """.trimIndent()

    fun generateGradleProperties(): String = """
        org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
        android.useAndroidX=true
        android.nonTransitiveRClass=true
    """.trimIndent()

    fun generateAppBuildGradle(project: ProjectEntity): String = """
        plugins {
            id("com.android.application")
            id("org.jetbrains.kotlin.android")
        }

        android {
            namespace = "${project.packageName}"
            compileSdk = 34

            defaultConfig {
                applicationId = "${project.packageName}"
                minSdk = 24
                targetSdk = 34
                versionCode = ${project.versionCode}
                versionName = "${project.versionName}"

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            buildTypes {
                release {
                    isMinifyEnabled = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    signingConfig = signingConfigs.getByName("debug") // Replace with upload-key for release
                }
                debug {
                    isDebuggable = true
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            kotlinOptions {
                jvmTarget = "17"
            }
        }

        dependencies {
            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.appcompat:appcompat:1.6.1")
            implementation("com.google.android.material:material:1.11.0")
            implementation("androidx.webkit:webkit:1.10.0")
            implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
        }
    """.trimIndent()

    fun generateAndroidManifest(project: ProjectEntity): String {
        val permissions = buildList {
            add("android.permission.INTERNET")
            add("android.permission.ACCESS_NETWORK_STATE")
            if (project.permCamera) add("android.permission.CAMERA")
            if (project.permMic) add("android.permission.RECORD_AUDIO")
            if (project.permLocation) {
                add("android.permission.ACCESS_FINE_LOCATION")
                add("android.permission.ACCESS_COARSE_LOCATION")
            }
            if (project.permNotifications) add("android.permission.POST_NOTIFICATIONS")
        }

        val screenOrientation = when (project.orientation) {
            "PORTRAIT" -> "portrait"
            "LANDSCAPE" -> "landscape"
            else -> "unspecified"
        }

        val tvAttributes = if (project.isTvMode) {
            """
            android:banner="@drawable/ic_launcher_foreground"
            android:isGame="false"
            """
        } else ""

        val tvFeatures = if (project.isTvMode) {
            """
            <uses-feature android:name="android.software.leanback" android:required="false" />
            <uses-feature android:name="android.hardware.touchscreen" android:required="false" />
            """
        } else ""

        return """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools">

                ${permissions.joinToString("\n    ") { "<uses-permission android:name=\"$it\" />" }}
                $tvFeatures

                <application
                    android:allowBackup="true"
                    android:icon="@mipmap/ic_launcher"
                    android:label="@string/app_name"
                    android:roundIcon="@mipmap/ic_launcher_round"
                    android:supportsRtl="true"
                    android:theme="@style/Theme.App.Starting"
                    android:usesCleartextTraffic="${project.sourceUrl.startsWith("http://")}"
                    $tvAttributes>
                    <activity
                        android:name=".MainActivity"
                        android:exported="true"
                        android:configChanges="orientation|screenSize|keyboardHidden"
                        android:screenOrientation="$screenOrientation">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                            ${if (project.isTvMode) "<category android:name=\"android.intent.category.LEANBACK_LAUNCHER\" />" else ""}
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
        """.trimIndent()
    }

    fun generateMainActivity(project: ProjectEntity): String {
        return """
            package ${project.packageName}

            import android.annotation.SuppressLint
            import android.content.Intent
            import android.graphics.Bitmap
            import android.net.Uri
            import android.os.Bundle
            import android.view.KeyEvent
            import android.view.View
            import android.webkit.*
            import android.widget.ProgressBar
            import android.widget.Toast
            import androidx.activity.OnBackPressedCallback
            import androidx.appcompat.app.AlertDialog
            import androidx.appcompat.app.AppCompatActivity
            import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
            import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

            class MainActivity : AppCompatActivity() {

                private lateinit var webView: WebView
                private lateinit var progressBar: ProgressBar
                private var swipeRefreshLayout: SwipeRefreshLayout? = null

                @SuppressLint("SetJavaScriptEnabled")
                override fun onCreate(savedInstanceState: Bundle?) {
                    val splashScreen = installSplashScreen()
                    super.onCreate(savedInstanceState)
                    setTheme(R.style.Theme_App)

                    // Setup root layout dynamically
                    val rootLayout = android.widget.FrameLayout(this)
                    rootLayout.layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    webView = WebView(this).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        ${if (project.isTvMode) "isFocusable = true; isFocusableInTouchMode = true" else ""}
                    }

                    progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                        layoutParams = android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                            12
                        )
                        max = 100
                        visibility = View.GONE
                    }

                    ${if (project.pullToRefreshEnabled) """
                    swipeRefreshLayout = SwipeRefreshLayout(this).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        addView(webView)
                        setOnRefreshListener { webView.reload() }
                    }
                    rootLayout.addView(swipeRefreshLayout)
                    """ else """
                    rootLayout.addView(webView)
                    """}
                    rootLayout.addView(progressBar)
                    setContentView(rootLayout)

                    configureWebView()
                    setupBackNavigation()
                    loadContent()
                }

                @SuppressLint("SetJavaScriptEnabled")
                private fun configureWebView() {
                    webView.settings.apply {
                        javaScriptEnabled = ${project.jsEnabled}
                        domStorageEnabled = ${project.domStorageEnabled}
                        databaseEnabled = ${project.domStorageEnabled}
                        setSupportZoom(${project.zoomEnabled})
                        builtInZoomControls = ${project.zoomEnabled}
                        displayZoomControls = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                    }

                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            progressBar.visibility = View.VISIBLE
                            progressBar.progress = 10
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            progressBar.visibility = View.GONE
                            swipeRefreshLayout?.isRefreshing = false
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val uri = request?.url ?: return false
                            val scheme = uri.scheme ?: ""

                            if (scheme == "tel" || scheme == "mailto" || scheme == "whatsapp") {
                                try {
                                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    return true
                                } catch (e: Exception) {
                                    Toast.makeText(this@MainActivity, "No app found to handle link", Toast.LENGTH_SHORT).show()
                                    return true
                                }
                            }

                            ${if (project.externalLinksMode == "BROWSER") """
                            val currentHost = Uri.parse("${project.sourceUrl}").host
                            if (uri.host != null && currentHost != null && !uri.host!!.contains(currentHost)) {
                                startActivity(Intent(Intent.ACTION_VIEW, uri))
                                return true
                            }
                            """ else ""}

                            return false
                        }
                    }

                    webView.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progressBar.progress = newProgress
                        }

                        ${if (project.permLocation) """
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, true, false)
                        }
                        """ else ""}
                    }
                }

                private fun loadContent() {
                    ${if (project.sourceType == "ZIP") """
                    webView.loadUrl("file:///android_asset/${project.startHtmlFile}")
                    """ else """
                    webView.loadUrl("${project.sourceUrl}")
                    """}
                }

                private fun setupBackNavigation() {
                    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            ${when (project.backNavMode) {
                                "CONFIRM" -> """
                                    if (webView.canGoBack()) {
                                        webView.goBack()
                                    } else {
                                        AlertDialog.Builder(this@MainActivity)
                                            .setTitle("Exit App")
                                            .setMessage("Do you want to exit ${project.name}?")
                                            .setPositiveButton("Exit") { _, _ -> finish() }
                                            .setNegativeButton("Cancel", null)
                                            .show()
                                    }
                                """
                                "IMMEDIATE" -> """
                                    finish()
                                """
                                else -> """
                                    if (webView.canGoBack()) {
                                        webView.goBack()
                                    } else {
                                        finish()
                                    }
                                """
                            }}
                        }
                    })
                }

                ${if (project.isTvMode) """
                override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
                    return when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            webView.dispatchKeyEvent(event)
                        }
                        else -> super.onKeyDown(keyCode, event)
                    }
                }
                """ else ""}
            }
        """.trimIndent()
    }

    fun generateStringsXml(project: ProjectEntity): String = """
        <resources>
            <string name="app_name">${project.name.replace("\"", "\\\"")}</string>
        </resources>
    """.trimIndent()

    fun generateColorsXml(project: ProjectEntity): String = """
        <resources>
            <color name="primary">${project.primaryColorHex}</color>
            <color name="accent">${project.accentColorHex}</color>
            <color name="status_bar">${project.statusBarColorHex}</color>
            <color name="nav_bar">${project.navBarColorHex}</color>
            <color name="splash_background">${project.splashBgColor}</color>
        </resources>
    """.trimIndent()

    fun generateThemesXml(project: ProjectEntity): String = """
        <resources>
            <style name="Theme.App" parent="Theme.Material3.DayNight.NoActionBar">
                <item name="colorPrimary">@color/primary</item>
                <item name="colorSecondary">@color/accent</item>
                <item name="android:statusBarColor">@color/status_bar</item>
                <item name="android:navigationBarColor">@color/nav_bar</item>
            </style>

            <style name="Theme.App.Starting" parent="Theme.SplashScreen">
                <item name="windowSplashScreenBackground">@color/splash_background</item>
                <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
                <item name="postSplashScreenTheme">@style/Theme.App</item>
            </style>
        </resources>
    """.trimIndent()

    fun generateThemesV31Xml(project: ProjectEntity): String = """
        <resources>
            <style name="Theme.App.Starting" parent="Theme.SplashScreen">
                <item name="windowSplashScreenBackground">@color/splash_background</item>
                <item name="windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
                <item name="postSplashScreenTheme">@style/Theme.App</item>
            </style>
        </resources>
    """.trimIndent()

    fun generateIconBackgroundXml(project: ProjectEntity): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <shape xmlns:android="http://schemas.android.com/apk/res/android"
            android:shape="rectangle">
            <gradient
                android:angle="135"
                android:startColor="${project.primaryColorHex}"
                android:endColor="${project.accentColorHex}" />
        </shape>
    """.trimIndent()

    fun generateIconForegroundXml(): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <vector xmlns:android="http://schemas.android.com/apk/res/android"
            android:width="108dp"
            android:height="108dp"
            android:viewportWidth="108"
            android:viewportHeight="108">
            <path
                android:fillColor="#FFFFFF"
                android:pathData="M34,42 L54,28 L74,42 L74,74 L34,74 Z"/>
            <path
                android:fillColor="#10B981"
                android:pathData="M54,48 a8,8 0 1,0 0.001,0 Z"/>
        </vector>
    """.trimIndent()

    fun generateDefaultIndexHtml(project: ProjectEntity): String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${project.name}</title>
            <style>
                body {
                    margin: 0;
                    padding: 40px 20px;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    background: #0F172A;
                    color: #F8FAFC;
                    text-align: center;
                }
                .card {
                    max-width: 480px;
                    margin: 40px auto;
                    background: #1E293B;
                    border-radius: 16px;
                    padding: 32px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.5);
                    border: 1px solid #334155;
                }
                h1 { color: #38BDF8; margin-top: 0; }
                p { color: #94A3B8; line-height: 1.6; }
                .btn {
                    display: inline-block;
                    margin-top: 20px;
                    padding: 12px 24px;
                    background: #2563EB;
                    color: white;
                    text-decoration: none;
                    border-radius: 8px;
                    font-weight: 600;
                }
            </style>
        </head>
        <body>
            <div class="card">
                <h1>${project.name}</h1>
                <p>Welcome to your Android application generated with <strong>Web2APK Builder</strong>.</p>
                <p>Edit this file or upload your complete HTML/CSS/JS package to customize your app.</p>
                <a href="#" class="btn" onclick="alert('Hello from Web2APK!')">Test Native Interaction</a>
            </div>
        </body>
        </html>
    """.trimIndent()

    fun generateProjectReadme(project: ProjectEntity): String = """
        # ${project.name} (Android APK Project)

        Generated automatically by **Web2APK Builder**.

        ## Configuration
        - **Package Name**: `${project.packageName}`
        - **Version**: `${project.versionName}` (`${project.versionCode}`)
        - **Source**: `${project.sourceType}` (${if (project.sourceType == "URL") project.sourceUrl else project.startHtmlFile})
        - **TV Mode**: `${project.isTvMode}`

        ## Building with Gradle
        ```bash
        ./gradlew assembleDebug
        # Or for release:
        ./gradlew assembleRelease
        ```
    """.trimIndent()
}
