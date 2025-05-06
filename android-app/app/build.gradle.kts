plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
}
configurations.all {
    resolutionStrategy {
        // Hilt ?븷?끂?뀒?씠?뀡 泥섎━ ?떆 ?궗?슜?븯?뒗 JavaPoet?쓣 1.13.0?쑝濡? 媛뺤젣
        force("com.squareup:javapoet:1.13.0")
    }
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.dagger:hilt-android:2.51")
    implementation(libs.androidx.animation.core.lint)
    kapt("com.google.dagger:hilt-compiler:2.51") {
        // Hilt가 끌어오는 최신 JavaPoet 모듈을 제외
        exclude(group = "com.squareup", module = "javapoet")
    }
    // 구버전 JavaPoet을 직접 추가
    implementation("com.squareup:javapoet:1.13.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}