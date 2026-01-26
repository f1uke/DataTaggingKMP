# DataTaggingKMP

A Kotlin Multiplatform module for Analytics Event Tracking, shared between iOS and Android platforms.

## Overview

DataTaggingKMP is a shared module that handles sending analytics events to GTM (Google Tag Manager). It supports both iOS and Android using Ktor as the HTTP client.

### Features

- Event tracking (click, page view, custom events)
- Session management (auto-refresh every 30 minutes)
- Platform-agnostic core logic
- Ktor networking (Darwin engine for iOS, OkHttp for Android)
- Type-safe API with Kotlin data classes

## Project Structure

```
DataTaggingKMP/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml          # Version catalog
├── shared/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/             # Shared code
│       │   └── kotlin/com/finnomena/datatagging/
│       │       ├── DataTaggingManager.kt
│       │       ├── DataTaggingFactory.kt
│       │       ├── model/
│       │       │   ├── AnalyticsEvent.kt
│       │       │   └── DataTaggingConfig.kt
│       │       └── platform/
│       │           ├── Platform.kt
│       │           ├── Storage.kt
│       │           └── HttpClientFactory.kt
│       ├── iosMain/                # iOS implementations
│       │   └── kotlin/.../platform/
│       │       ├── Platform.ios.kt
│       │       ├── Storage.ios.kt
│       │       └── HttpClientFactory.ios.kt
│       └── androidMain/            # Android implementations
│           └── kotlin/.../platform/
│               ├── Platform.android.kt
│               ├── Storage.android.kt
│               └── HttpClientFactory.android.kt
```

## Installation

### iOS (XCFramework)

#### 1. Build XCFramework

```bash
cd /path/to/DataTaggingKMP

# Build iOS frameworks
./gradlew :shared:linkReleaseFrameworkIosArm64 :shared:linkReleaseFrameworkIosSimulatorArm64

# Create XCFramework
xcodebuild -create-xcframework \
  -framework shared/build/bin/iosArm64/releaseFramework/DataTaggingKMP.framework \
  -framework shared/build/bin/iosSimulatorArm64/releaseFramework/DataTaggingKMP.framework \
  -output shared/build/XCFrameworks/DataTaggingKMP.xcframework
```

#### 2. Add to Xcode Project

**Option A: Manual Integration**
1. Copy `DataTaggingKMP.xcframework` to your project directory
2. In Xcode, go to your target's "General" tab
3. Under "Frameworks, Libraries, and Embedded Content", click "+"
4. Select "Add Other..." > "Add Files..."
5. Choose `DataTaggingKMP.xcframework`
6. Set "Embed" to "Embed & Sign"

**Option B: XcodeGen (project.yml)**
```yaml
targets:
  YourApp:
    dependencies:
      - framework: path/to/DataTaggingKMP.xcframework
        embed: true
```

#### 3. Implement Storage Protocol

```swift
import DataTaggingKMP

class IosDataTaggingStorage: DataTaggingStorage {

    func getUserId() -> String? {
        // Return user ID from your auth system
        return UserManager.getUserID()
    }

    func getSessionUUID() -> String? {
        // Return stored session UUID
        return UserManager.getUUID()
    }

    func setSessionUUID(uuid: String) {
        // Store session UUID
        UserManager.setUUID(uuid)
    }

    func getBrazeId() -> String? {
        // Return Braze ID if using Braze
        return UserDefaultManager().getBrazeID()
    }

    func getClientId() -> String? {
        // Return persistent client identifier
        return CookieManager.checkAnalyticsCookie().value
    }

    func setClientId(clientId: String) {
        // Store client ID (optional if managed elsewhere)
    }
}
```

#### 4. Initialize and Use

```swift
import DataTaggingKMP

class DataTaggingManager {

    static let shared = DataTaggingManager()

    private let kmpManager: DataTaggingKMP.DataTaggingManager

    private init() {
        let storage = IosDataTaggingStorage()
        let userAgent = "YourApp/1.0 (iOS)"

        // Choose environment
        #if DEBUG
        self.kmpManager = DataTaggingFactory.shared.createDevelopment(
            storage: storage,
            userAgent: userAgent
        )
        #else
        self.kmpManager = DataTaggingFactory.shared.createProduction(
            storage: storage,
            userAgent: userAgent
        )
        #endif
    }

    func logEvent(name: String, location: String, path: String, params: [String: String]?) {
        let event = AnalyticsEvent(
            name: name,
            location: location,
            type: "click",
            path: path,
            params: params
        )
        kmpManager.logEvent(event: event)
    }

    func logScreenView(path: String, params: [String: String]?) {
        kmpManager.logScreenView(path: path, params: params)
    }
}
```

### Android (Gradle Dependency)

#### 1. Add Repository

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        // Add your repository or use local maven
        mavenLocal()
    }
}
```

#### 2. Add Dependency

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.finnomena:datatagging:1.0.0")
}
```

#### 3. Implement Storage

```kotlin
import android.content.Context
import com.finnomena.datatagging.platform.DataTaggingStorage

class AndroidDataTaggingStorage(context: Context) : DataTaggingStorage {

    private val prefs = context.getSharedPreferences("analytics", Context.MODE_PRIVATE)

    override fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    override fun getSessionUUID(): String? {
        return prefs.getString("session_uuid", null)
    }

    override fun setSessionUUID(uuid: String) {
        prefs.edit().putString("session_uuid", uuid).apply()
    }

    override fun getBrazeId(): String? {
        return prefs.getString("braze_id", null)
    }

    override fun getClientId(): String? {
        return prefs.getString("client_id", null)
    }

    override fun setClientId(clientId: String) {
        prefs.edit().putString("client_id", clientId).apply()
    }
}
```

#### 4. Initialize and Use

```kotlin
import com.finnomena.datatagging.DataTaggingFactory
import com.finnomena.datatagging.DataTaggingManager
import com.finnomena.datatagging.model.AnalyticsEvent
import com.finnomena.datatagging.model.DataTaggingConfig

class AnalyticsManager(context: Context) {

    private val manager: DataTaggingManager

    init {
        val storage = AndroidDataTaggingStorage(context)
        val userAgent = "YourApp/1.0 (Android)"

        manager = if (BuildConfig.DEBUG) {
            DataTaggingFactory.createDevelopment(storage, userAgent)
        } else {
            DataTaggingFactory.createProduction(storage, userAgent)
        }
    }

    fun logEvent(name: String, location: String, path: String, params: Map<String, String>?) {
        val event = AnalyticsEvent(
            name = name,
            location = location,
            type = "click",
            path = path,
            params = params
        )
        manager.logEvent(event)
    }

    fun logScreenView(path: String, params: Map<String, String>?) {
        manager.logScreenView(path, params)
    }
}
```

## API Reference

### DataTaggingConfig

Configuration for analytics endpoints.

```kotlin
data class DataTaggingConfig(
    val baseUrl: String,
    val userAgent: String,
    val trackingId: String = "UA-FINNO",
    val sessionTimeoutMinutes: Int = 30
)

// Pre-configured environment URLs (use with DataTaggingFactory convenience methods)
DataTaggingFactory.createDevelopment(storage, userAgent)  // https://gtm-int.finnomena.com/mpua
DataTaggingFactory.createUAT(storage, userAgent)          // https://gtm-uat.finnomena.com/mpua
DataTaggingFactory.createProduction(storage, userAgent)   // https://gtm.finnomena.com/mpua

// Or create custom config
val config = DataTaggingConfig(
    baseUrl = "https://gtm.finnomena.com/mpua",
    userAgent = "YourApp/1.0 (iOS)"
)
```

### AnalyticsEvent

Event data model.

```kotlin
data class AnalyticsEvent(
    val name: String,           // Event name, e.g., "click_button"
    val location: String,       // Event location, e.g., "home_screen"
    val type: String = "click", // Event type: "click", "page", etc.
    val path: String,           // Screen path, e.g., "home"
    val params: Map<String, String>? = null  // Additional parameters
)
```

### DataTaggingManager

Main manager class.

```kotlin
class DataTaggingManager {
    fun logEvent(event: AnalyticsEvent)
    fun logScreenView(path: String, params: Map<String, String>?)
    fun setExId(exId: String)   // Set experiment ID for A/B testing
    fun getExId(): String?      // Get current experiment ID
}
```

### DataTaggingStorage

Protocol/Interface for platform-specific storage implementation.

```kotlin
interface DataTaggingStorage {
    fun getUserId(): String?
    fun getSessionUUID(): String?
    fun setSessionUUID(uuid: String)
    fun getBrazeId(): String?
    fun getClientId(): String?
    fun setClientId(clientId: String)
}
```

## Event Parameters

Events are sent to GTM with the following parameters:

| Parameter | Description |
|-----------|-------------|
| `v` | Version ("1") |
| `t` | Type ("event") |
| `tid` | Tracking ID ("UA-FINNO") |
| `cid` | Client ID (persistent device identifier) |
| `ea` | Event action (event name) |
| `l` | Location |
| `u.fss` | Session UUID (refreshed every 30 min) |
| `u.f` | Client ID |
| `u.e` | Braze ID |
| `u.i` | User ID |
| `p` | JSON-encoded custom parameters |
| `ph` | Path |
| `et` | Event type ("click", "page") |
| `d` | Device ("ios" or "android") |
| `user_agent` | User agent string provided in configuration |

## Development

### Requirements

- JDK 17+
- Kotlin 2.0+
- Xcode 15+ (for iOS)
- Android SDK 24+ (for Android)

### Build Commands

```bash
# Build all
./gradlew build

# Build iOS frameworks only
./gradlew :shared:linkReleaseFrameworkIosArm64
./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64

# Run tests
./gradlew test

# Clean
./gradlew clean
```

### Updating Dependencies

Version catalog is located at `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.21"
ktor = "2.3.12"
coroutines = "1.8.1"
serialization = "1.7.3"
```

## Troubleshooting

### iOS: "No such module 'DataTaggingKMP'"

1. Clean build folder (Cmd + Shift + K)
2. Verify XCFramework is properly embedded
3. Check "Framework Search Paths" in Build Settings

### Android: SDK not found

Create `local.properties` in project root:
```properties
sdk.dir=/path/to/Android/sdk
```

### Session not refreshing

Session UUID refreshes automatically after 30 minutes of inactivity. Verify your `DataTaggingStorage` implementation correctly stores and retrieves the session UUID.

## License

Copyright (c) Finnomena. All rights reserved.
