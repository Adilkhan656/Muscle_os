import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
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

        // Load secrets from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { properties.load(it) }
        }

        resValue("string", "facebook_app_id", properties.getProperty("facebook_app_id") ?: "")
        resValue("string", "facebook_client_token", properties.getProperty("facebook_client_token") ?: "")
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
        resValues = true
    }
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
    //fragment navigation
    val nav_version = "2.9.8" // Use the latest stable version
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")
    //viewpager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation(libs.firebase.firestore)
//lottie
    implementation("com.airbnb.android:lottie:6.6.7")
    implementation(libs.androidx.credentials)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation(libs.androidx.credentials.play.services.auth)
    //Facebook
    implementation("com.facebook.android:facebook-login:18.1.3")
    implementation(libs.googleid)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

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
