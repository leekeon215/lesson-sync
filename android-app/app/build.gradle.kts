plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.kapt")
    kotlin("android")
    // KSP 플러그인을 버전 카탈로그 별칭으로 적용
    alias(libs.plugins.kotlin.ksp)
    // Compose 컴파일러 플러그인 추가
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "com.lessonsync.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lessonsync.app"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation (libs.retrofit)
    implementation(libs.converter.scalars)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.media3.common.ktx)

    implementation(libs.okhttp)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.runtime.livedata)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)


    // Core testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.room.testing)
    // Mockito for mocking dependencies in unit tests
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline) // To mock final classes

    // Coroutines testing
    testImplementation(libs.kotlinx.coroutines.test)

    // Special rule for testing LiveData
    testImplementation(libs.androidx.core.testing)

    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
