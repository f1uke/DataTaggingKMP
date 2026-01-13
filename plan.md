# Plan: Extract DataTaggingManager to Kotlin Multiplatform

## Overview
สร้าง KMP module ใหม่ชื่อ `DataTaggingKMP` เป็น repo แยก พร้อม Ktor networking ใช้ได้ทั้ง iOS และ Android

## Current Architecture (iOS)

```
DataTaggingManager
├── Dependencies:
│   ├── UserManager (Keychain) → userID, session UUID
│   ├── UserDefaultManager (UserDefaults) → brazeID
│   └── CookieManager (Cookies) → finnakie (client ID)
├── Core Logic:
│   ├── Session UUID refresh every 30 minutes
│   ├── Parameter construction for GTM
│   └── Fire-and-forget GET requests
└── Endpoints:
    ├── dev/uat: https://gtm-int.finnomena.com/mpua
    └── prod: https://gtm.finnomena.com/mpua
```

## Target Architecture (KMP)

```
DataTaggingKMP/
├── shared/
│   ├── commonMain/          # Shared code
│   │   ├── DataTaggingManager.kt
│   │   ├── AnalyticsEvent.kt
│   │   ├── DataTaggingConfig.kt
│   │   └── platform/
│   │       ├── Platform.kt          # expect declarations
│   │       ├── Storage.kt           # expect for key-value storage
│   │       └── HttpClient.kt        # Ktor setup
│   ├── iosMain/             # iOS implementations
│   │   └── platform/
│   │       ├── Platform.ios.kt      # actual: device info, user agent
│   │       └── Storage.ios.kt       # actual: Keychain/UserDefaults wrapper
│   └── androidMain/         # Android implementations
│       └── platform/
│           ├── Platform.android.kt  # actual: device info, user agent
│           └── Storage.android.kt   # actual: SharedPreferences/EncryptedPrefs
├── iosApp/                  # iOS sample/test app (optional)
├── androidApp/              # Android sample/test app (optional)
└── build.gradle.kts
```

## Implementation Plan

### Step 1: Create KMP Project Structure
สร้างโครงสร้างโปรเจค KMP ใหม่:
- `settings.gradle.kts`
- `build.gradle.kts` (root)
- `shared/build.gradle.kts` (KMP library)
- Configure iOS XCFramework output

### Step 2: Define Common Interfaces (commonMain)

**2.1 Platform Abstraction**
```kotlin
// Platform.kt
expect fun getPlatformName(): String           // "ios" or "android"
expect fun getUserAgent(): String              // App version, OS version
expect fun generateTimeBasedUUID(): String     // UUID v1 with timestamp
expect fun getUUIDTimestamp(uuid: String): Long?
```

**2.2 Storage Abstraction**
```kotlin
// Storage.kt
interface DataTaggingStorage {
    fun getUserId(): String?
    fun getSessionUUID(): String?
    fun setSessionUUID(uuid: String)
    fun getBrazeId(): String?
    fun getClientId(): String?
    fun setClientId(clientId: String)
}
```

**2.3 Core Data Classes**
```kotlin
// AnalyticsEvent.kt
data class AnalyticsEvent(
    val name: String,
    val location: String,
    val type: String = "click",
    val path: String,
    val params: Map<String, String>? = null
)

// DataTaggingConfig.kt
data class DataTaggingConfig(
    val baseUrl: String,
    val trackingId: String = "UA-FINNO",
    val sessionTimeoutMinutes: Int = 30
)
```

### Step 3: Implement Core Logic (commonMain)

**DataTaggingManager.kt**
```kotlin
class DataTaggingManager(
    private val config: DataTaggingConfig,
    private val storage: DataTaggingStorage,
    private val httpClient: HttpClient
) {
    private var exId: String? = null

    suspend fun logEvent(event: AnalyticsEvent) { ... }
    suspend fun logScreenView(path: String, params: Map<String, String>?) { ... }

    fun setExId(exId: String) { ... }
    fun getExId(): String? = exId

    private fun refreshSessionIfNeeded(): String { ... }
    private fun buildParameters(...): Map<String, String> { ... }
}
```

### Step 4: iOS Platform Implementation (iosMain)

**Platform.ios.kt**
- Use `NSBundle.mainBundle` for app info
- Use `UIDevice` for OS version
- Implement time-based UUID using Darwin C functions

**Storage.ios.kt**
- Wrapper around iOS Keychain (via Security framework)
- UserDefaults for non-sensitive data
- HTTPCookieStorage for client ID cookie

### Step 5: Android Platform Implementation (androidMain)

**Platform.android.kt**
- Use `BuildConfig` / `PackageManager` for app info
- Use `Build.VERSION` for OS version

**Storage.android.kt**
- EncryptedSharedPreferences for secure storage
- SharedPreferences for non-sensitive data

### Step 6: HTTP Client with Ktor (commonMain)

```kotlin
// HttpClient.kt
expect fun createHttpClient(): HttpClient

// Common Ktor configuration
val httpClient = createHttpClient().config {
    install(ContentNegotiation) {
        json()
    }
    install(Logging) {
        level = LogLevel.NONE // Production
    }
}
```

### Step 7: XCFramework Generation
Configure Gradle to generate XCFramework for iOS integration:
```kotlin
ios {
    binaries.framework {
        baseName = "DataTaggingKMP"
        isStatic = true
    }
}
```

## Files to Create

| Path | Description |
|------|-------------|
| `settings.gradle.kts` | Project settings |
| `build.gradle.kts` | Root build config |
| `shared/build.gradle.kts` | KMP library config with Ktor |
| `shared/src/commonMain/kotlin/DataTaggingManager.kt` | Core logic |
| `shared/src/commonMain/kotlin/AnalyticsEvent.kt` | Event data class |
| `shared/src/commonMain/kotlin/DataTaggingConfig.kt` | Config data class |
| `shared/src/commonMain/kotlin/platform/Platform.kt` | Platform expect |
| `shared/src/commonMain/kotlin/platform/Storage.kt` | Storage interface |
| `shared/src/iosMain/kotlin/platform/Platform.ios.kt` | iOS actual |
| `shared/src/iosMain/kotlin/platform/Storage.ios.kt` | iOS storage |
| `shared/src/androidMain/kotlin/platform/Platform.android.kt` | Android actual |
| `shared/src/androidMain/kotlin/platform/Storage.android.kt` | Android storage |

## iOS Integration (nter-ios-app)

หลังจากสร้าง KMP module แล้ว:

1. **Generate XCFramework**
   ```bash
   ./gradlew assembleXCFramework
   ```

2. **Add to iOS Project**
   - Copy XCFramework to project
   - หรือใช้ CocoaPods/SPM integration

3. **Create Swift Wrapper** (optional)
   ```swift
   // DataTaggingBridge.swift
   import DataTaggingKMP

   class DataTaggingBridge {
       private let manager: DataTaggingManager

       init(config: DataTaggingConfig, storage: DataTaggingStorage) {
           self.manager = DataTaggingManager(...)
       }
   }
   ```

4. **Implement iOS Storage**
   ```swift
   class iOSDataTaggingStorage: DataTaggingStorage {
       // Wrap existing UserManager, UserDefaultManager, CookieManager
   }
   ```

## Verification

1. **Build KMP Module**
   ```bash
   ./gradlew build
   ./gradlew assembleXCFramework
   ```

2. **Test iOS Integration**
   - Import XCFramework to test project
   - Verify logEvent and logScreenView work

3. **Verify Analytics**
   - Check GTM receives events with correct parameters
   - Verify session UUID refreshes after 30 minutes

## Dependencies (shared/build.gradle.kts)

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation("io.ktor:ktor-client-core:2.3.7")
        implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
        implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    }
    iosMain.dependencies {
        implementation("io.ktor:ktor-client-darwin:2.3.7")
    }
    androidMain.dependencies {
        implementation("io.ktor:ktor-client-okhttp:2.3.7")
    }
}
```

## Notes

- KMP module จะเป็น repo แยก ไม่อยู่ใน nter-ios-app
- iOS app จะ consume ผ่าน XCFramework
- Android app สามารถใช้เป็น Gradle dependency ได้เลย
- Session UUID logic (30 min refresh) จะอยู่ใน common code
- Storage implementation เป็น platform-specific
