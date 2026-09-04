plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.library.stability)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:gpx"))
            implementation(libs.kotlinx.io.core)
        }
    }
}
