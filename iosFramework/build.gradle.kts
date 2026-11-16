plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

/**
 * The thin iOS entry point, the counterpart of androidApp: it assembles the layers' Koin modules and exposes
 * `MainViewController` in the `SnapClone` framework linked by the Xcode project in iosApp.
 */
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SnapClone"
            isStatic = true
        }
    }

    sourceSets {
        iosMain.dependencies {
            implementation(projects.domain)
            implementation(projects.data)
            implementation(projects.presentation)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.koin.core)
        }
    }
}
