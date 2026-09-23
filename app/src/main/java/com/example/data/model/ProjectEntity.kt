package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My Website App",
    val packageName: String = "com.web2apk.mywebsite",
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val developerName: String = "Web2APK Developer",
    val description: String = "Packaged with Web2APK Builder",
    val sourceType: String = "URL", // "URL" or "ZIP"
    val sourceUrl: String = "https://example.com",
    val startHtmlFile: String = "index.html",
    val iconUri: String? = null,
    val splashBgColor: String = "#0F172A",
    val splashDuration: Float = 1.5f,
    val splashTitle: String = "My Website App",
    val splashSubtitle: String = "Powered by Web2APK",
    val splashMode: String = "LOGO_NAME", // "LOGO_ONLY", "LOGO_NAME", "LOGO_NAME_SUBTITLE"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val orientation: String = "AUTO", // "AUTO", "PORTRAIT", "LANDSCAPE"
    val primaryColorHex: String = "#2563EB",
    val accentColorHex: String = "#10B981",
    val statusBarColorHex: String = "#1E293B",
    val navBarColorHex: String = "#0F172A",
    val isTvMode: Boolean = false,
    val jsEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val zoomEnabled: Boolean = true,
    val pullToRefreshEnabled: Boolean = true,
    val fileUploadEnabled: Boolean = true,
    val fullscreenVideoEnabled: Boolean = true,
    val permCamera: Boolean = false,
    val permMic: Boolean = false,
    val permLocation: Boolean = false,
    val permNotifications: Boolean = false,
    val forcedAutoplay: Boolean = true,
    val desktopModeEnabled: Boolean = false,
    val externalLinksMode: String = "BROWSER", // "INSIDE", "BROWSER", "ASK"
    val backNavMode: String = "HISTORY", // "HISTORY", "CONFIRM", "IMMEDIATE"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
