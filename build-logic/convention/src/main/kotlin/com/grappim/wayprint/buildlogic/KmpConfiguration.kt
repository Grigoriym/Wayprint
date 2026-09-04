package com.grappim.wayprint.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val JDK_VERSION = 21

/**
 * The single edit point for platform targets.
 *
 * The Android target itself is declared by `com.android.kotlin.multiplatform.library` in
 * `KmpLibraryConventionPlugin`. `jvm()` (Desktop, M15) is declared here; `iosArm64()`/
 * `iosSimulatorArm64()` (M16) go here too, and nowhere else, when that target arrives.
 */
fun Project.configureKmp() {
    // Android + JVM both have real host-test coverage now (`testAndroidHostTest`, `jvmTest`) —
    // unlike TaigaMobileNova, which disables instrumentation for the Android unit test tasks
    // because it measures coverage on `jvmTest` alone.
    pluginManager.apply("org.jetbrains.kotlinx.kover")

    extensions.configure<KotlinMultiplatformExtension> {
        jvmToolchain(JDK_VERSION)
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        jvm()

        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                implementation(libs.findLibrary("kotlinx.collections").get())
                implementation(libs.findLibrary("kotlinx.date.time").get())
            }
        }
    }
}
