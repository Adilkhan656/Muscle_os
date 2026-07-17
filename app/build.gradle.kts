plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
//    alias(libs.plugins.kotlin.android)
//    kotlin("kapt")
}

android {
    namespace = "com.musclesOS.adil"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.musclesOS.adil"
        minSdk = 24
        targetSdk = 36
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
    buildFeatures{
        viewBinding = true
    }
//    kapt {
//        correctErrorTypes = true
//    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.ui)
    implementation(libs.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.material)
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    // Used by existing authentication screens; the liquid splash itself has no
    // Lottie, video, bitmap animation, OpenGL, or third-party renderer.
    implementation("com.airbnb.android:lottie:6.6.7")
    implementation(libs.androidx.credentials)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation(libs.androidx.credentials.play.services.auth)
    implementation("com.facebook.android:facebook-login:18.1.3")
    implementation(libs.googleid)

    implementation(libs.firebase.auth)
    implementation(libs.hilt.android)
//    kapt(libs.hilt.compiler)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
