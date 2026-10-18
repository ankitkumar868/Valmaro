plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    buildFeatures{
        viewBinding=true
    }
    namespace = "com.example.valmaro"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.valmaro"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.cardview:cardview:1.0.0")
    // Gemini AI
    implementation("com.google.firebase:firebase-ai:17.17.0")

// Firebase App Check Debug
    implementation("com.google.firebase:firebase-appcheck-debug:19.4.1")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")

}