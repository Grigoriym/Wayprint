package com.grappim.wayprint.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Lets the Compose Compiler embed a stability marker on this module's classes, without pulling in
// any Compose UI toolkit (Foundation/Material3/Navigation/...). For `*/domain` modules whose types
// are consumed as Composable parameters elsewhere: without this, the Compose compiler in a
// downstream UI module has no marker to trust and defaults every such class to Unstable, even a
// fully `val`, ImmutableList-using data class — see docs/compose/stability-reports.md.
//
// compose-runtime is `compileOnly` on JVM-based targets (Android, Desktop): it's needed only for
// the compiler to reference the `@StabilityInferred` annotation type at compile time, not at
// runtime, and consuming UI modules already carry compose-runtime themselves. Kotlin/Native
// doesn't support `compileOnly` resolution the same way — a `compileOnly` dependency declared in
// `commonMain` prints a "Unsupported compileOnly Dependencies ... Kotlin/Native" warning on every
// build once iOS targets exist — so iOS gets `api` instead; harmless, since a Native consumer
// links compose-runtime directly anyway.
fun Project.configureComposeStabilityMarker() {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    extensions.configure<KotlinMultiplatformExtension> {
        val composeRuntime = libs.findLibrary("jetbrains.compose.runtime").get()
        sourceSets.apply {
            androidMain.dependencies {
                compileOnly(composeRuntime)
            }
            jvmMain.dependencies {
                compileOnly(composeRuntime)
            }
            iosMain.dependencies {
                api(composeRuntime)
            }
        }
    }
}
