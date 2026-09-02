import com.grappim.wayprint.buildlogic.configureComposeStabilityConfig
import com.grappim.wayprint.buildlogic.configureComposeStabilityMarker
import com.grappim.wayprint.buildlogic.configureComposeStabilityReports
import org.gradle.api.Plugin
import org.gradle.api.Project

// Applied alongside `wayprint.kmp.library` on `*/domain` modules whose types are consumed as
// Composable parameters elsewhere — see docs/compose/stability-reports.md.
class KmpLibraryStabilityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureComposeStabilityMarker()
            configureComposeStabilityReports()
            configureComposeStabilityConfig()
        }
    }
}
