plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt)
}

allprojects {
    group = "me.nathanfallet.snapchatclone"
    version = "1.0.0"
}

// detekt on every module, with the same rules as KMP Vision Camera (see detekt.yml).
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom("${rootProject.projectDir}/detekt.yml")
        source.setFrom(files("src"))
    }
}

// The iOS code of KMP Vision Camera uses Objective-C cinterop libraries, which need Xcode: on other hosts (e.g. the
// Linux Android CI), Kotlin skips the iOS compilations, so the shared iOS metadata compilations are skipped too.
if (!System.getProperty("os.name").startsWith("Mac")) {
    val iosMetadataCompilation = Regex("compile(Ios|Apple|Native)MainKotlinMetadata")
    subprojects {
        tasks.configureEach {
            if (iosMetadataCompilation.matches(name)) enabled = false
        }
    }
}
