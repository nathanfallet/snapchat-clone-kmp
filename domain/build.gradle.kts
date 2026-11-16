plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

/**
 * The domain layer: models, repository interfaces and use cases. Pure Kotlin Multiplatform: no Compose, no camera,
 * no file system.
 */
kotlin {
    jvmToolchain(21)
    android {
        namespace = "me.nathanfallet.snapchatclone.domain"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions {
        optIn.addAll("kotlin.time.ExperimentalTime", "kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
