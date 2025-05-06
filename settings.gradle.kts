pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  plugins {
    id("com.android.application")       version "8.4.0" apply false
    kotlin("android")                   version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51"   apply false
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "lesson-sync"
include(":lesson-sync-fastapi")
//include(":lesson-sync-spring")
include(":android-app")

