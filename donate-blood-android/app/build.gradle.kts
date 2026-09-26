import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    kotlin("kapt")
    id("com.google.gms.google-services")

}

// Sırlar koda ve git'e girmez: local.properties (git-ignored) ya da ortam değişkeninden okunur.
// Şablon: local.properties.example
val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
fun secret(name: String): String = localProperties.getProperty(name) ?: System.getenv(name) ?: ""

android {
    namespace = "com.ribuufing.bloodapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ribuufing.bloodapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = secret("MAPS_API_KEY")
        buildConfigField("String", "CONTENTFUL_SPACE_ID", "\"${secret("CONTENTFUL_SPACE_ID")}\"")
        buildConfigField("String", "CONTENTFUL_ACCESS_TOKEN", "\"${secret("CONTENTFUL_ACCESS_TOKEN")}\"")

    }
    
    flavorDimensions += "environment"
    
    // İmza bilgileri yalnızca local.properties'te; yoksa release imzasız derlenir (katkıcılar debug derleyebilir).
    val hasReleaseSigning = secret("RELEASE_STORE_FILE").isNotBlank()
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(secret("RELEASE_STORE_FILE"))
                storePassword = secret("RELEASE_STORE_PASSWORD")
                keyAlias = secret("RELEASE_KEY_ALIAS")
                keyPassword = secret("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.bloodapp.com/\"")
            buildConfigField("String", "PHYSICAL_IP", "\"\"")
            isMinifyEnabled = false
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.bloodapp.com/\"")
            buildConfigField("String", "PHYSICAL_IP", "\"\"")
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "API_BASE_URL", "\"https://dev-api.bloodapp.com/\"")
        }
        create("prod") {
            dimension = "environment"
            applicationIdSuffix = ".prod"
            manifestPlaceholders["android.profileable"] = "false"
            versionNameSuffix = "-prod"
            buildConfigField("String", "API_BASE_URL", "\"https://api.bloodapp.com/\"")
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dagger - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose)

    // Coil
    implementation(libs.coil.compose)

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Responsive Design
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
    implementation("com.google.firebase:firebase-analytics")

    // Google Play Services Maps SDK
    implementation (libs.play.services.maps)
    
    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Maps Compose Library
    implementation (libs.maps.compose)

    implementation("androidx.compose.material:material:1.6.3")

    // Microsoft Authentication Library
    implementation("com.microsoft.identity.client:msal:5.+") {
        exclude(group = "com.microsoft.device.display")
        exclude(group = "io.opentelemetry", module = "opentelemetry-bom")
    }

    implementation("com.android.volley:volley:1.2.1")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.3.6")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")

    // ML Kit for Barcode scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    implementation("com.contentful.java:java-sdk:9.0.1")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")
}