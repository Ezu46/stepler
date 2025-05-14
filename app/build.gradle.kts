plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.stepler"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stepler"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

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
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.firebase:firebase-bom:33.9.0")
    implementation(platform("com.google.firebase:firebase-bom:32.8.1")) // BOM для управления версиями
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-functions:20.4.0")
    implementation ("androidx.activity:activity:1.10.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.yandex.android:maps.mobile:4.4.0-full")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.animation.core.android)  // Совместимая версия для SDK 34
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}