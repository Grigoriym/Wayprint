plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.library.compose)
    alias(libs.plugins.wayprint.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":uikit"))
            implementation(project(":feature:wayprint:domain"))
            implementation(project(":feature:wayprint:ui"))
        }
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
