plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    `maven-publish`
}

group = "com.github.f1uke"
version = "1.0.0"

kotlin {
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "DataTaggingKMP"
            isStatic = true
        }
    }

    // Android target
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}

android {
    namespace = "com.finnomena.datatagging"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Publishing configuration for JitPack
afterEvaluate {
    publishing {
        publications {
            // KMP plugin auto-generates publications for each target
            // JitPack will use these automatically
        }
    }
}

// XCFramework generation task
// Note: iosX64 is excluded from XCFramework as it conflicts with iosSimulatorArm64
// Apple Silicon Macs can run arm64 simulators natively
tasks.register("assembleXCFramework") {
    dependsOn(
        "linkReleaseFrameworkIosArm64",
        "linkReleaseFrameworkIosSimulatorArm64"
    )

    doLast {
        val outputDir = layout.buildDirectory.dir("XCFrameworks").get().asFile
        val xcframeworkPath = File(outputDir, "DataTaggingKMP.xcframework")

        // Delete existing XCFramework if exists
        if (xcframeworkPath.exists()) {
            xcframeworkPath.deleteRecursively()
        }
        outputDir.mkdirs()

        exec {
            commandLine(
                "xcodebuild",
                "-create-xcframework",
                "-framework", layout.buildDirectory.file("bin/iosArm64/releaseFramework/DataTaggingKMP.framework").get().asFile.absolutePath,
                "-framework", layout.buildDirectory.file("bin/iosSimulatorArm64/releaseFramework/DataTaggingKMP.framework").get().asFile.absolutePath,
                "-output", xcframeworkPath.absolutePath
            )
        }
    }
}
