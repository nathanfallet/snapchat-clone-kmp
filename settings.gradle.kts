rootProject.name = "snapchat-clone-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // KMP Vision Camera's skia modules use Skiko, whose Android artifacts are only published on JetBrains'
        // repository (see the Installation section of the KMP Vision Camera README).
        maven("https://packages.jetbrains.team/maven/p/cmp/dev") {
            mavenContent {
                includeModuleByRegex("org\\.jetbrains\\.skiko", "skiko-android.*")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Business layer: models, repository interfaces and use cases. Pure Kotlin Multiplatform, no Compose, no camera.
include(":domain")

// Data layer: the repositories' implementations (memories stored as files with a JSON index).
include(":data")

// Presentation layer: Compose Multiplatform UI and ViewModels, the only module using KMP Vision Camera.
include(":presentation")

// The thin Android application (AGP 9 needs the application plugin in its own module).
include(":androidApp")

// The thin iOS entry point, built as the SnapClone framework that the Xcode project in iosApp links.
include(":iosFramework")
