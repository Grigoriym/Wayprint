plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.library.compose)
    alias(libs.plugins.wayprint.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":uikit"))
        }
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
