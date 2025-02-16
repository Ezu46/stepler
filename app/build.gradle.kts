plugins {
    alias(libs.plugins.android.application)
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

    // Если используете AndroidX Activity:
    implementation ("androidx.activity:activity:1.10.0")  // Совместимая версия для SDK 34
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}