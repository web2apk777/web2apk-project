/**
 * Web2APK Server Project Generator
 * Synthesizes a compile-ready Gradle/Kotlin Android project from user configurations.
 */
const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');

class ServerProjectGenerator {
  static generateProject(projectConfig, outputDirectory) {
    const packagePath = projectConfig.packageName.replace(/\./g, '/');
    const appDir = path.join(outputDirectory, 'app');
    const srcDir = path.join(appDir, 'src', 'main', 'java', packagePath);
    const resDir = path.join(appDir, 'src', 'main', 'res');
    const assetsDir = path.join(appDir, 'src', 'main', 'assets');

    // Create directory tree
    fs.mkdirSync(srcDir, { recursive: true });
    fs.mkdirSync(path.join(resDir, 'values'), { recursive: true });
    fs.mkdirSync(path.join(resDir, 'drawable'), { recursive: true });
    fs.mkdirSync(assetsDir, { recursive: true });

    // 1. Root build.gradle.kts
    fs.writeFileSync(path.join(outputDirectory, 'build.gradle.kts'), `
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}
    `.trim());

    // 2. settings.gradle.kts
    fs.writeFileSync(path.join(outputDirectory, 'settings.gradle.kts'), `
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
rootProject.name = "${projectConfig.name.replace(/"/g, '\\"')}"
include(":app")
    `.trim());

    // 3. app/build.gradle.kts
    fs.writeFileSync(path.join(appDir, 'build.gradle.kts'), `
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "${projectConfig.packageName}"
    compileSdk = 34

    defaultConfig {
        applicationId = "${projectConfig.packageName}"
        minSdk = 24
        targetSdk = 34
        versionCode = ${projectConfig.versionCode || 1}
        versionName = "${projectConfig.versionName || '1.0.0'}"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    `.trim());

    // 4. AndroidManifest.xml
    const permissions = ['android.permission.INTERNET', 'android.permission.ACCESS_NETWORK_STATE'];
    if (projectConfig.permCamera) permissions.push('android.permission.CAMERA');
    if (projectConfig.permMic) permissions.push('android.permission.RECORD_AUDIO');
    if (projectConfig.permLocation) {
      permissions.push('android.permission.ACCESS_FINE_LOCATION');
      permissions.push('android.permission.ACCESS_COARSE_LOCATION');
    }

    fs.writeFileSync(path.join(appDir, 'src', 'main', 'AndroidManifest.xml'), `
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    ${permissions.map(p => `<uses-permission android:name="${p}" />`).join('\n    ')}

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.App"
        android:usesCleartextTraffic="${projectConfig.sourceUrl?.startsWith('http://')}">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize"
            android:screenOrientation="${projectConfig.orientation === 'PORTRAIT' ? 'portrait' : projectConfig.orientation === 'LANDSCAPE' ? 'landscape' : 'unspecified'}">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                ${projectConfig.isTvMode ? '<category android:name="android.intent.category.LEANBACK_LAUNCHER" />' : ''}
            </intent-filter>
        </activity>
    </application>
</manifest>
    `.trim());

    // 5. MainActivity.kt
    fs.writeFileSync(path.join(srcDir, 'MainActivity.kt'), `
package ${projectConfig.packageName}

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = ${projectConfig.jsEnabled !== false}
            domStorageEnabled = ${projectConfig.domStorageEnabled !== false}
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }

        ${projectConfig.sourceType === 'ZIP' ? `
        webView.loadUrl("file:///android_asset/${projectConfig.startHtmlFile || 'index.html'}")
        ` : `
        webView.loadUrl("${projectConfig.sourceUrl || 'https://example.com'}")
        `}
    }
}
    `.trim());

    // 6. Resources
    fs.writeFileSync(path.join(resDir, 'values', 'strings.xml'), `
<resources>
    <string name="app_name">${projectConfig.name.replace(/"/g, '\\"')}</string>
</resources>
    `.trim());

    fs.writeFileSync(path.join(resDir, 'values', 'themes.xml'), `
<resources>
    <style name="Theme.App" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:statusBarColor">${projectConfig.statusBarColorHex || '#1E293B'}</item>
        <item name="android:navigationBarColor">${projectConfig.navBarColorHex || '#0F172A'}</item>
    </style>
</resources>
    `.trim());

    return outputDirectory;
  }
}

module.exports = ServerProjectGenerator;
