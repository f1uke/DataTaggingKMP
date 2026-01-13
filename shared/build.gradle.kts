plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

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

// XCFramework generation task
tasks.register("assembleXCFramework") {
    dependsOn(
        "linkDebugFrameworkIosArm64",
        "linkDebugFrameworkIosX64",
        "linkDebugFrameworkIosSimulatorArm64",
        "linkReleaseFrameworkIosArm64",
        "linkReleaseFrameworkIosX64",
        "linkReleaseFrameworkIosSimulatorArm64"
    )

    doLast {
        val outputDir = layout.buildDirectory.dir("XCFrameworks").get().asFile
        outputDir.mkdirs()

        exec {
            commandLine(
                "xcodebuild",
                "-create-xcframework",
                "-framework", layout.buildDirectory.file("bin/iosArm64/releaseFramework/DataTaggingKMP.framework").get().asFile.absolutePath,
                "-framework", layout.buildDirectory.file("bin/iosSimulatorArm64/releaseFramework/DataTaggingKMP.framework").get().asFile.absolutePath,
                "-framework", layout.buildDirectory.file("bin/iosX64/releaseFramework/DataTaggingKMP.framework").get().asFile.absolutePath,
                "-output", File(outputDir, "DataTaggingKMP.xcframework").absolutePath
            )
        }
    }
}
