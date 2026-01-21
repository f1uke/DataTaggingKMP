# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DataTaggingKMP is a Kotlin Multiplatform (KMP) library for analytics event tracking, shared between iOS and Android. It sends events to GTM (Google Tag Manager) with session management and platform-specific networking (Ktor Darwin for iOS, OkHttp for Android).

## Build Commands

```bash
# Build all platforms
./gradlew build

# Build iOS frameworks
./gradlew :shared:linkReleaseFrameworkIosArm64 :shared:linkReleaseFrameworkIosSimulatorArm64

# Create XCFramework for iOS distribution
xcodebuild -create-xcframework \
  -framework shared/build/bin/iosArm64/releaseFramework/DataTaggingKMP.framework \
  -framework shared/build/bin/iosSimulatorArm64/releaseFramework/DataTaggingKMP.framework \
  -output shared/build/XCFrameworks/DataTaggingKMP.xcframework

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Architecture

### KMP Structure (expect/actual pattern)

- **commonMain**: Shared business logic and interfaces
  - `DataTaggingManager.kt` - Core manager handling event logging and session management
  - `DataTaggingFactory.kt` - Factory for creating manager instances
  - `model/AnalyticsEvent.kt` - Event data class
  - `model/DataTaggingConfig.kt` - Configuration with environment presets (dev/uat/prod)
  - `platform/*.kt` - `expect` declarations for platform-specific code

- **iosMain**: iOS implementations using `actual` keyword
  - Uses NSUserDefaults for default storage
  - Ktor Darwin engine for HTTP

- **androidMain**: Android implementations using `actual` keyword
  - Uses SharedPreferences for default storage
  - Ktor OkHttp engine for HTTP

### Key Design Patterns

1. **Platform Abstraction**: `DataTaggingStorage` interface must be implemented by consuming apps to provide user IDs, session storage, and client identifiers

2. **Fire-and-Forget**: Events logged via coroutines with errors silently suppressed - the library never throws on logging failures

3. **Session Management**: 30-minute timeout with automatic refresh when session expires or experiment ID (ExId) changes

## Dependencies

Defined in `gradle/libs.versions.toml`:
- Kotlin 2.0.21
- Ktor 2.3.12 (networking)
- Coroutines 1.8.1
- Serialization 1.7.3

## Package Structure

All public API is under `com.finnomena.datatagging`. Platform implementations are in `com.finnomena.datatagging.platform`.
