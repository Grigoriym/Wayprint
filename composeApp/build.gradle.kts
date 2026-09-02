plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.library.compose)
    alias(libs.plugins.wayprint.kmp.di)
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
