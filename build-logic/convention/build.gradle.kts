plugins {
  `kotlin-dsl`
}


group = "cn.winxo.buildlogic"
version = "1.0.0"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  compileOnly(libs.gradle.plugin.android.tools.build)
  compileOnly(libs.gradle.plugin.kotlin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = "convention.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidLibrary") {
      id = "convention.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidHilt") {
      id = "convention.android.hilt"
      implementationClass = "AndroidHiltConventionPlugin"
    }
    register("androidTest") {
      id = "convention.android.test"
      implementationClass = "AndroidTestConventionPlugin"
    }
  }
}