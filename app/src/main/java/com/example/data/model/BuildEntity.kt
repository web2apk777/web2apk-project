package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "builds")
data class BuildEntity(
    @PrimaryKey
    val id: String = "BUILD-" + UUID.randomUUID().toString().take(8).uppercase(),
    val projectId: String,
    val appName: String,
    val packageName: String,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val status: String = "QUEUED", // "QUEUED", "PREPARING", "BUILDING", "SIGNING", "UPLOADING", "COMPLETE", "FAILED"
    val apkUrl: String = "",
    val apkSizeMb: String = "16.8 MB",
    val sha256: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val buildType: String = "DEBUG", // "DEBUG", "RELEASE"
    val buildLogs: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val errorReason: String? = null
)
