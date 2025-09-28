plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nerdlegame"
    compileSdk = 36 // ✅ 36 isn’t officially released yet

    defaultConfig {
        applicationId = "com.example.nerdlegame"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ✅ Room components
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.recyclerview) // This is 1.4.0
    annotationProcessor("androidx.room:room-compiler:2.6.1") // if Java
    // kapt("androidx.room:room-compiler:2.6.1") // if using Kotlin

    // Optional: Room with Kotlin coroutines (ignore if Java)
    implementation("androidx.room:room-ktx:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
