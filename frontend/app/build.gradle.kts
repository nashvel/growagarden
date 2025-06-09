plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.glowagarden.stocknotifier"
    compileSdk = 34 // Target Android 14 or higher (for Android 15 compatibility)

    defaultConfig {
        applicationId = "com.glowagarden.stocknotifier"
        minSdk = 24 // Android 7.0 Nougat
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8" // Ensure this is compatible with your Kotlin and Compose versions
                                                // Common versions: 1.5.1 for Compose 1.6.x, 1.5.3 for Compose BOM 2023.10.01+
                                                // Check Android Studio recommendations if sync issues occur.
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0") // or latest 1.13.x
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2") // or latest 1.9.x
    implementation(platform("androidx.compose:compose-bom:2024.02.01")) // Or a more recent BOM like 2024.05.00
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // Or latest 2.11.0
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // Or latest 2.11.0

    // OkHttp Logging Interceptor (for debugging network calls)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Or latest 4.12.0

    // ViewModel for Jetpack Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1") // For Jetpack DataStore
    implementation("androidx.work:work-runtime-ktx:2.9.0") // For background tasks

    // Icons (ensure you have these if StockScreen uses them directly, e.g. Icons.Filled.Refresh)
    implementation("androidx.compose.material:material-icons-core:1.6.7") // Match Compose version if possible
    implementation("androidx.compose.material:material-icons-extended:1.6.7")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.01")) // Match BOM
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}