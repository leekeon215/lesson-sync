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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Compose BOM 사용
    implementation(platform("androidx.compose:compose-bom:2024.03.00"))

    // AndroidX 코어/라이프사이클/Activity
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.1")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // (선택) 네비게이션 컴포즈
    implementation("androidx.navigation:navigation-compose:2.7.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
