plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.serialization)
    alias(libs.plugins.wayprint.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.io.core)
        }
    }
}
