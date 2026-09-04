import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.grappim.wayprint.buildlogic.configureKmp
import com.grappim.wayprint.buildlogic.configureLinting
import com.grappim.wayprint.buildlogic.configureTests
import com.grappim.wayprint.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // KMP must be applied first so the android plugin can hook into it
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            // The android DSL lives as a sub-extension of the kotlin extension
            val kotlinExt = extensions.getByType<KotlinMultiplatformExtension>()
            (kotlinExt as ExtensionAware).extensions
                .configure<KotlinMultiplatformAndroidLibraryExtension> {
                    compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
                    minSdk = libs.findVersion("minSdk").get().toString().toInt()
                    namespace = "com.grappim.wayprint" + path.replace(':', '.').replace("-", "")

                    // `com.android.kotlin.multiplatform.library` creates no host-test
                    // compilation unless asked. Without this, `commonTest` belongs to no
                    // Android compilation, the test dependencies from `configureTests()` are
                    // inert for that target, and there is no `testAndroidHostTest` task to run
                    // — `jvm()` (M15.8) gives `commonTest` a `jvmTest` task for free, but that
                    // doesn't cover the Android target.
                    withHostTestBuilder {}.configure {
                        isReturnDefaultValues = true
                        isIncludeAndroidResources = true
                    }
                }

            configureKmp()
            configureTests()
            configureLinting()
        }
    }
}
