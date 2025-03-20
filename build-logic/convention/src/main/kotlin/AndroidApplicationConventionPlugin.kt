import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.application")
        apply("org.jetbrains.kotlin.android")
        apply("org.jetbrains.kotlin.kapt")
      }


      extensions.configure<BaseAppModuleExtension> {
        configureKotlinAndroid(commonExtension = this)
        defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
      }

      dependencies {
        add("implementation", libs.findLibrary("androidx-core-ktx").get())
      }

      val kaptExtension = extensions.getByType<KaptExtension>()
      kaptExtension.apply {
        correctErrorTypes = true
      }
    }
  }
}