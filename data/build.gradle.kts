plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.android.kmp.library)
}

/**
 * The data layer: implementations of the domain's repositories. Memories are stored as files in the app's private
 * directory, with a JSON index.
 */
kotlin {
    jvmToolchain(21)
    android {
        namespace = "me.nathanfallet.snapchatclone.data"
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
            implementation(projects.domain)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.okio)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.okio.fakefilesystem)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
    }
}
