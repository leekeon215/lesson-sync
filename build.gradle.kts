buildscript {
  repositories {
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.4.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
  }
}

subprojects {
  configurations.all {
    resolutionStrategy {
      // JavaPoet 1.13.0 À¸·Î °­Á¦
      force("com.squareup:javapoet:1.13.0")
    }
  }
}
// projects ì°¨ì›?—?„œ ë¦¬í¬ì§??† ë¦? ?„ ?–¸??? settings.gradle.kts ?—?„œ ê´?ë¦¬í•˜ë¯?ë¡? ?‚­? œ?•©?‹ˆ?‹¤.
