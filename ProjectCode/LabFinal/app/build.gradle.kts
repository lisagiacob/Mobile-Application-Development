plugins {
    //alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)


    id("com.android.application")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.lab2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lab2"
        minSdk = 28
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


        //credo posso commentarli perche io uso la 11 e non la 8 o peggio 1.8 di java gia di base nel progetto
        //for the camera
        //sourceCompatibility = JavaVersion.VERSION_1_8
        //targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.storage)
    implementation(libs.material)
    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.foundation.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation ("androidx.navigation:navigation-compose:2.6.0")



    //cameraS bullshit
    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Activity result APIs
    implementation("androidx.activity:activity-compose:1.8.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")


    //supbase
    implementation(libs.storage.kt)
    //implementation("io.github.jan-tennert.supabase:postgrest-kt:1.3.2")
    implementation(libs.gotrue.kt)
    implementation(libs.ktor.client.okhttp)
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation ("androidx.credentials:credentials:<latest version>")
    implementation ("androidx.credentials:credentials-play-services-auth:<latest version>")
    implementation ("com.google.android.libraries.identity.googleid:googleid:<latest version>")





    //supbase
    implementation("io.github.jan-tennert.supabase:storage-kt:1.3.2")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.3.2")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.3.2")


    // Jetpack Credentials core + Play-Services Auth bridge
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // Google ID helper for Credential Manager
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Firebase (via BoM)
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation ("androidx.credentials:credentials:1.5.0")
    implementation ("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation ("com.google.android.libraries.identity.googleid:googleid:1.1.1")


    implementation ("androidx.credentials:credentials:1.0.0")
    implementation ("androidx.credentials:credentials-play-services-auth:1.0.0")


    implementation ("androidx.credentials:credentials:1.5.0")
    implementation ("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

// Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

}

