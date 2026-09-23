package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.builder.ActiveBuildProgress
import com.example.builder.BuildManager
import com.example.data.local.AppDatabase
import com.example.data.model.BuildEntity
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class Web2ApkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val projectDao = db.projectDao()
    private val buildDao = db.buildDao()

    val projects: StateFlow<List<ProjectEntity>> = projectDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val builds: StateFlow<List<BuildEntity>> = buildDao.getAllBuilds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBuild: StateFlow<ActiveBuildProgress?> = BuildManager.activeBuild

    // Wizard draft state
    private val _draftProject = MutableStateFlow(ProjectEntity())
    val draftProject: StateFlow<ProjectEntity> = _draftProject.asStateFlow()

    private val _currentWizardStep = MutableStateFlow(1)
    val currentWizardStep: StateFlow<Int> = _currentWizardStep.asStateFlow()

    private val _rightsAccepted = MutableStateFlow(false)
    val rightsAccepted: StateFlow<Boolean> = _rightsAccepted.asStateFlow()

    private val _previewDeviceType = MutableStateFlow("PHONE") // "PHONE", "TV"
    val previewDeviceType: StateFlow<String> = _previewDeviceType.asStateFlow()

    private val _previewOrientation = MutableStateFlow("PORTRAIT") // "PORTRAIT", "LANDSCAPE"
    val previewOrientation: StateFlow<String> = _previewOrientation.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Admin Dashboard state
    private val _adminMaxUploadMb = MutableStateFlow(50)
    val adminMaxUploadMb: StateFlow<Int> = _adminMaxUploadMb.asStateFlow()

    private val _adminBuildTimeoutMin = MutableStateFlow(45)
    val adminBuildTimeoutMin: StateFlow<Int> = _adminBuildTimeoutMin.asStateFlow()

    private val _adminMaintenanceMode = MutableStateFlow(false)
    val adminMaintenanceMode: StateFlow<Boolean> = _adminMaintenanceMode.asStateFlow()

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun setWizardStep(step: Int) {
        _currentWizardStep.value = step.coerceIn(1, 6)
    }

    fun setRightsAccepted(accepted: Boolean) {
        _rightsAccepted.value = accepted
    }

    fun setPreviewDevice(type: String) {
        _previewDeviceType.value = type
        if (type == "TV") {
            _previewOrientation.value = "LANDSCAPE"
        }
    }

    fun togglePreviewOrientation() {
        _previewOrientation.value = if (_previewOrientation.value == "PORTRAIT") "LANDSCAPE" else "PORTRAIT"
    }

    fun updateSource(type: String, url: String, startHtml: String) {
        _draftProject.value = _draftProject.value.copy(
            sourceType = type,
            sourceUrl = url,
            startHtmlFile = startHtml,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateAppName(name: String) {
        val sanitized = name.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val autoPackage = if (sanitized.isNotBlank()) "com.web2apk.$sanitized" else "com.web2apk.app"
        _draftProject.value = _draftProject.value.copy(
            name = name,
            packageName = autoPackage,
            splashTitle = name,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updatePackageName(pkg: String) {
        _draftProject.value = _draftProject.value.copy(
            packageName = pkg,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateVersionInfo(versionName: String, versionCode: Int, devName: String, desc: String) {
        _draftProject.value = _draftProject.value.copy(
            versionName = versionName,
            versionCode = versionCode,
            developerName = devName,
            description = desc,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateIconUri(uri: String?) {
        _draftProject.value = _draftProject.value.copy(
            iconUri = uri,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateSplashConfig(bgColor: String, duration: Float, title: String, subtitle: String, mode: String) {
        _draftProject.value = _draftProject.value.copy(
            splashBgColor = bgColor,
            splashDuration = duration,
            splashTitle = title,
            splashSubtitle = subtitle,
            splashMode = mode,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updateAppearance(
        themeMode: String,
        orientation: String,
        primary: String,
        accent: String,
        statusBar: String,
        navBar: String,
        isTvMode: Boolean
    ) {
        _draftProject.value = _draftProject.value.copy(
            themeMode = themeMode,
            orientation = orientation,
            primaryColorHex = primary,
            accentColorHex = accent,
            statusBarColorHex = statusBar,
            navBarColorHex = navBar,
            isTvMode = isTvMode,
            updatedAt = System.currentTimeMillis()
        )
        if (isTvMode) {
            _previewDeviceType.value = "TV"
            _previewOrientation.value = "LANDSCAPE"
        }
    }

    fun updateWebViewSettings(
        js: Boolean,
        dom: Boolean,
        zoom: Boolean,
        pullToRefresh: Boolean,
        fileUpload: Boolean,
        fullscreenVideo: Boolean,
        forcedAutoplay: Boolean,
        desktopMode: Boolean,
        backNav: String,
        externalLinks: String
    ) {
        _draftProject.value = _draftProject.value.copy(
            jsEnabled = js,
            domStorageEnabled = dom,
            zoomEnabled = zoom,
            pullToRefreshEnabled = pullToRefresh,
            fileUploadEnabled = fileUpload,
            fullscreenVideoEnabled = fullscreenVideo,
            forcedAutoplay = forcedAutoplay,
            desktopModeEnabled = desktopMode,
            backNavMode = backNav,
            externalLinksMode = externalLinks,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun updatePermissions(camera: Boolean, mic: Boolean, location: Boolean, notifications: Boolean) {
        _draftProject.value = _draftProject.value.copy(
            permCamera = camera,
            permMic = mic,
            permLocation = location,
            permNotifications = notifications,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun isValidPackageName(pkg: String): Boolean {
        val pattern = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$")
        return pattern.matcher(pkg).matches()
    }

    fun startNewDraft() {
        _draftProject.value = ProjectEntity()
        _currentWizardStep.value = 1
        _rightsAccepted.value = false
    }

    fun loadExistingProject(project: ProjectEntity) {
        _draftProject.value = project
        _currentWizardStep.value = 1
    }

    fun saveDraftProject() {
        viewModelScope.launch {
            projectDao.insertProject(_draftProject.value)
            showSnackbar("Project '${_draftProject.value.name}' saved!")
        }
    }

    fun triggerBuild(buildType: String = "DEBUG") {
        viewModelScope.launch {
            // Save project first
            projectDao.insertProject(_draftProject.value)
            // Execute build
            BuildManager.executeBuild(getApplication(), _draftProject.value, buildType)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.deleteProject(project)
            showSnackbar("Project deleted")
        }
    }

    fun deleteBuild(build: BuildEntity) {
        viewModelScope.launch {
            buildDao.deleteBuild(build)
            showSnackbar("Build record removed")
        }
    }

    fun updateAdminConfig(maxUpload: Int, timeout: Int, maintenance: Boolean) {
        _adminMaxUploadMb.value = maxUpload
        _adminBuildTimeoutMin.value = timeout
        _adminMaintenanceMode.value = maintenance
        showSnackbar("Admin configuration updated")
    }
}
