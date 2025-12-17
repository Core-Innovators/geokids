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
    // AndroidX Core
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Authentication + Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // Optional Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // DataStore dependencies (required by Firestore)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore:1.1.1")

    // Kotlin Coroutines (optional but recommended for async Firebase)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // 🔥 NEW DEPENDENCIES FOR DRIVER FORM

    // OkHttp for Supabase API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Glide for image loading and display
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")

}