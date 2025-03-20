import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("dagger.hilt.android.plugin")
      }

      dependencies {
        add("implementation", libs.findLibrary("google-hilt-android").get())
        add("kapt", libs.findLibrary("google-hilt-compiler").get())
      }
    }
  }
}