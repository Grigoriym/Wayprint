plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.library.compose)
    alias(libs.plugins.wayprint.kmp.di)
    // `WayprintEditRoute` is a `@Serializable` `NavKey` — the shell serializes it into the back
    // stack (M9).
    alias(libs.plugins.wayprint.kmp.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":feature:wayprint:domain"))
            implementation(project(":core:storage"))
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
    }
}
