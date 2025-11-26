plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.coreinnovators.geokids"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.coreinnovators.geokids"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // ✅ Firebase BOM (use this specific stable version)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Authentication + Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Optional Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // ✅ DataStore dependencies (required by Firestore since v26)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore:1.1.1")

    // (Optional but recommended) Kotlin Coroutines if you use async Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}

