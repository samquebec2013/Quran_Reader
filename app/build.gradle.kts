plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace "com.education.quran8"
    compileSdk 33  // Changed from 33 to 34

    defaultConfig {
        applicationId "com.education.quran8"
        minSdk 21
        targetSdk 33
        versionCode 10009
        versionName "2.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ViewPager2 (if needed)
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Unit Testing Dependencies
    testImplementation("junit:junit:4.13.2")  // JUnit 4 for local unit tests

    // Android Instrumented Testing Dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // JUnit 4 for instrumentation tests
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // UI testing
}
