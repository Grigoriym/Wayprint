import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.wayprint.kmp.library)
    alias(libs.plugins.wayprint.kmp.library.compose)
    alias(libs.plugins.wayprint.kmp.di)
}

compose.desktop {
    application {
        mainClass = "com.grappim.wayprint.composeapp.WayprintDesktopKt"

        nativeDistributions {
            // Only Linux is packaged: this dev machine is the only place M15 can build or
            // verify a real installer, and Windows/macOS icon assets (.ico/.icns) don't exist
            // yet — add them alongside a real target format when one of those platforms is
            // actually verifiable.
            targetFormats(TargetFormat.Deb)
            packageName = libs.versions.app.name.get()
            packageVersion = libs.versions.version.name.get()
            description = libs.versions.app.description.get()
            vendor = libs.versions.app.vendor.get()

            linux {
                iconFile.set(project.file("../art/wayprint_logo.png"))
                debMaintainer = libs.versions.app.vendor.get()
                debPackageVersion = libs.versions.version.name.get()
                appCategory = libs.versions.app.category.get()
                menuGroup = libs.versions.app.menugroup.get()
                shortcut = true
            }
        }
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "WayprintIos"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":uikit"))
            implementation(project(":core:navigation"))
            implementation(project(":core:storage"))
            implementation(project(":feature:wayprint:domain"))
            implementation(project(":feature:wayprint:ui"))
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // `WayprintViewModel`/`RecentsViewModel` use `viewModelScope`
            // (`Dispatchers.Main.immediate`) — without this, JVM has no `Dispatchers.Main`
            // implementation and both crash the instant a ViewModel is created.
            implementation(libs.kotlinx.coroutines.swing)
        }
        // `KoinGraphTest` needs a platform with real actuals for every `expect` module
        // (`PlatformComponentModule`, `PlatformStorageModule`, `PlatformUiModule`) — it can't
        // live in `commonTest`, since Android's own extra-supplied type (`Context`) doesn't
        // exist on the JVM target's classpath. Kept on both platforms (unlike
        // TaigaMobileNova's JVM-only precedent) since Android already had this coverage before
        // `jvm()` existed.
        getByName("androidHostTest").dependencies {
            implementation(libs.koin.test)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
