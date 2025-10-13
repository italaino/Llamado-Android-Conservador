// app/build.gradle.kts

plugins {
    // Plugins desde el Version Catalog
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Google Services (se registró en el buildscript del root)
    id("com.google.gms.google-services")
}

android {
    namespace = "cl.gpv.llamado_conservador"
    //noinspection GradleDependency
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.gpv.llamado_conservador"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
    @Suppress("UnstableApiUsage")
    composeOptions {
        // Versión del compilador de Compose compatible con AS 2024.3.2
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Core & UI básico
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // BOM de Compose
    implementation(platform(libs.androidx.compose.bom))

    // Bibliotecas de Compose
    implementation(libs.androidx.ui)                  // ui
    implementation(libs.androidx.ui.graphics)         // ui-graphics
    implementation(libs.androidx.ui.tooling.preview) // ui-tooling-preview
    implementation(libs.androidx.activity.compose)   // activity-compose
    implementation(libs.androidx.compose.material3)   // material3

    // Soporte a viewModel() en Composables
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines para Firestore / Android
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Firebase Firestore KTX
    implementation(libs.firebase.firestore.ktx)

    // Jetpack Compose para TV
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
}
