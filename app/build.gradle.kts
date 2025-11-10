import java.util.Properties
import java.io.FileInputStream

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(FileInputStream(localPropsFile))
}
val apiKey: String = localProps.getProperty("GOOGLE_API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nerdlegame"
    compileSdk = 36 // ✅ 36 isn’t officially released yet

    defaultConfig {
        applicationId = "com.example.nerdlegame"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // חשיפת המפתח לקוד Java כ‑BuildConfig.GOOGLE_API_KEY
        buildConfigField("String", "GOOGLE_API_KEY", "\"$apiKey\"")
        // חלופה (ללא BuildConfig):
        // resValue("string", "google_api_key", apiKey)

        packaging {
            resources {
                excludes += "/META-INF/INDEX.LIST"
                excludes += "/META-INF/DEPENDENCIES"
                // מומלץ גם להחריג קבצי מטא נפוצים נוספים (למקרה שיופיעו):
                excludes += "/META-INF/AL2.0"
                excludes += "/META-INF/LGPL2.1"
                excludes += "/META-INF/NOTICE*"
                excludes += "/META-INF/LICENSE*"
            }
        }
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
    // ⚠️ נדרש אם משתמשים ב‑buildConfigField
    buildFeatures {
        buildConfig = true
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


    implementation("com.google.genai:google-genai:1.24.0") // SDK רשמי ל‑Gemini

}
