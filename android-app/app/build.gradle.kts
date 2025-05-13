plugins {
    id("com.android.application")
    kotlin("android")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    // Compose BOM 사용
    implementation(platform(libs.androidx.compose.bom.v20240300))

    // AndroidX 코어/라이프사이클/Activity
    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)

    // (선택) 네비게이션 컴포즈
    implementation(libs.androidx.navigation.compose)

    implementation(libs.dagger)
    implementation(libs.androidx.hilt.navigation.compose)

    debugImplementation(libs.ui.tooling)
}
