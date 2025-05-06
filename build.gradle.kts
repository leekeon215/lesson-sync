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
      // JavaPoet 1.13.0 ���� ����
      force("com.squareup:javapoet:1.13.0")
    }
  }
}
// projects 차원?��?�� 리포�??���? ?��?��??? settings.gradle.kts ?��?�� �?리하�?�? ?��?��?��?��?��.
