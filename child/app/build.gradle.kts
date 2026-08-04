plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
	
   
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}
android {
    namespace = "com.anro.child"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anro.child"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // OkHttp + WebSocket
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // hillt
    implementation("com.google.dagger:hilt-android:2.57.1")
    kapt("com.google.dagger:hilt-compiler:2.57.1")
    implementation("com.google.android.material:material:1.12.0")
    //webrtc
    implementation("io.github.webrtc-sdk:android:144.7559.05")    
}

kapt {
    correctErrorTypes = true
}
