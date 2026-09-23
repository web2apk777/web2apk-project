# Web2APK Builder

> **Turn your website or HTML project into your own Android APK.**  
> A complete, production-ready Android and web platform that converts any website URL or offline HTML5/CSS/JavaScript ZIP bundle into a standalone, installable Android APK without requiring Android Studio.

---

## 🌟 Key Architecture & Capabilities

Web2APK Builder is built with clean architectural separation across six distinct tiers:

```
┌─────────────────────────────────────────────────────────────┐
│                    WEB2APK ARCHITECTURE                     │
├─────────────────┬───────────────────────────────────────────┤
│ CLIENT          │ Android App (Kotlin + Jetpack Compose M3) │
│                 │ Web UI Dashboard (Single-page dashboard)  │
├─────────────────┼───────────────────────────────────────────┤
│ BACKEND         │ Node.js / Express REST API                │
│                 │ Rate limiting, validation & auth guards   │
├─────────────────┼───────────────────────────────────────────┤
│ BUILD WORKER    │ Isolated asynchronous build queue         │
│                 │ Path traversal guards & zip-bomb filters  │
├─────────────────┼───────────────────────────────────────────┤
│ TEMPLATE ENGINE │ Automated Kotlin & Gradle DSL generator   │
│                 │ Android 12+ SplashScreen & Adaptive Icons │
├─────────────────┼───────────────────────────────────────────┤
│ DATABASE        │ Local Room DB (Android client)            │
│                 │ Projects & Builds persistence             │
├─────────────────┼───────────────────────────────────────────┤
│ CI/CD           │ GitHub Actions (`build-apk.yml`)          │
│                 │ Automated Debug & Release APK signing     │
└─────────────────┴───────────────────────────────────────────┘
```

---

## 📱 Android Client Features

- **Jetpack Compose & Material 3**: Glassmorphism cards, glowing status pills, smooth step transitions, dark & light themes.
- **6-Step Creation Wizard**:
  1. **Source Selection**: Live Website URL (with HTTPS verification and cleartext HTTP warnings) or Offline HTML/ZIP package.
  2. **App Information**: App Name, automatic package name synthesizer (e.g. `com.web2apk.myportfolio`), version code & name, developer metadata.
  3. **Branding & Adaptive Icons**: Icon previews in squircle, circle, and square shapes; adaptive mipmap generator (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`); Android 12+ `SplashScreen` styling with duration control.
  4. **Appearance & Device Modes**: Light/Dark/System theme, brand palette picker, and **Android TV / Fire TV Mode** with remote D-pad focus traversal and Leanback banner.
  5. **WebView Settings & Permissions**: Toggles for JavaScript, DOM storage, zoom controls, pull-to-refresh, file uploads, HTML5 fullscreen video, camera, microphone, and geolocation.
  6. **Review & Build**: Interactive device preview simulator, compilation mode selector (Debug vs Release), and **mandatory legal rights acceptance**.
- **10-Stage Visual Build Pipeline**:
  - Live progression with spinners and checkmarks.
  - Streaming terminal console logs with timestamped diagnostics.
  - APK Ready screen with Download, Share, and Install buttons.
  - Sideloading / Unknown Sources tutorial popup modal.
- **Local Room Database**: Persists saved projects and past build artifacts with SHA-256 integrity fingerprints.
- **Interactive Simulator**: Built-in sandbox allowing users to preview and test their live web apps in an actual Android WebView before compiling.
- **Admin Dashboard & Help Center**: 7 illustrated guides and complete CI/CD secrets configuration instructions.

---

## 🔒 Security & Safe Packaging

1. **Path Traversal Protection**: File extractors validate every entry path against canonical root directories to prevent file escape attacks (`../`).
2. **Zip Bomb Defense**: Maximum uncompressed limits (250MB) and maximum file thresholds (2,000 files) enforced during extraction.
3. **Least-Privilege Android Permissions**: Only user-selected permissions are declared in the synthesized `AndroidManifest.xml`.
4. **Mandatory Rights Verification**: Users must explicitly accept legal responsibility before any build job is initiated:
   > *"I confirm that I have the necessary rights or permission to package and distribute this content."*

---

## 🚀 Getting Started

### 1. Running the Android Application
The Android app compiles via Gradle Kotlin DSL.
```bash
# Build debug APK
gradle assembleDebug

# Run unit and local JVM tests
gradle :app:testDebugUnitTest
```

### 2. Running the Backend Server
```bash
cd server
npm install
npm start
# Server starts on http://localhost:3000
```

### 3. Setting Up GitHub Actions Secrets
To enable automated cloud builds in GitHub:
Navigate to your repository: **Settings → Secrets and variables → Actions**, and add:
- `KEYSTORE_BASE64`: Base64 encoded `.jks` file (`base64 -w 0 upload-key.jks`)
- `KEYSTORE_PASSWORD`: Master password for the keystore
- `KEY_ALIAS`: Signing key alias (e.g. `upload`)
- `KEY_PASSWORD`: Key password

---

## 📄 License
Released under the MIT License. Users are strictly responsible for the content and websites they package.
