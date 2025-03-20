import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidTestConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
      }

      dependencies {
        add("testImplementation", kotlin("test"))
        add("testImplementation", libs.findLibrary("junit-mockito-core").get())
        add("testImplementation", libs.findLibrary("junit-mockito-kotlin").get())
      }
    }
  }
}