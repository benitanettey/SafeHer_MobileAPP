plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.safeher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safeher"
        minSdk = 24
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
}

dependencies {
    // AndroidX + Material libraries
    implementation(libs.appcompat)
    //implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Google Play Services for location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation ("com.google.android.material:material:1.12.0")

    //  Gson library for saving/loading contacts as JSON
    implementation("com.google.code.gson:gson:2.10.1")
    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
