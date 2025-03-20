include(":sample")
include(":matisse")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    maven {
      setUrl("https://maven.aliyun.com/repository/public")
    }
    maven {
      setUrl("https://maven.aliyun.com/repository/central")
    }
    maven {
      setUrl("https://maven.aliyun.com/repository/gradle-plugin")
    }
    gradlePluginPortal()
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
  }
}

dependencyResolutionManagement {
  repositories {
    maven {
      setUrl("https://maven.aliyun.com/repository/public")
    }
    maven {
      setUrl("https://maven.aliyun.com/repository/central")
    }
    google()
    mavenCentral()
    maven {
      setUrl("https://maven.google.com")
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("androidx.*")
        includeGroupByRegex("com\\.google\\.android.*")
        includeGroup("android.arch.lifecycle")
        includeGroup("android.arch.core")
      }
    }
    maven {
      setUrl("https://repo.maven.apache.org/maven2/")
      content {
        includeGroupByRegex("org\\.jetbrains.*")
        includeGroupByRegex("com\\.jakewharton.*")
        includeGroupByRegex("io\\.reactivex.*")
        includeGroupByRegex("org\\.reactivestreams.*")
      }
    }
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://developer.huawei.com/repo/") }
  }
}