plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

val googleWalletIssuerId = project.findProperty("GOOGLE_WALLET_ISSUER_ID") as String? ?: ""
val googleWalletIssuerEmail = project.findProperty("GOOGLE_WALLET_ISSUER_EMAIL") as String? ?: ""
val googleWalletClassSuffix = project.findProperty("GOOGLE_WALLET_CLASS_SUFFIX") as String? ?: "business_card"
val googleWalletIssuerName = project.findProperty("GOOGLE_WALLET_ISSUER_NAME") as String? ?: "BusineesCard"

android {
    namespace = "com.sezgin.busineescard"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.sezgin.busineescard"
        minSdk = 24
        targetSdk = 34
        
        // Versiyon kodu kurulum için benzersiz kalmalı
        versionCode = (System.currentTimeMillis() / 1000).toInt()
        
        // Versiyon adı artık istediğiniz formatta
        versionName = "1.0.1"

        buildConfigField("String", "GOOGLE_WALLET_ISSUER_ID", "\"$googleWalletIssuerId\"")
        buildConfigField("String", "GOOGLE_WALLET_ISSUER_EMAIL", "\"$googleWalletIssuerEmail\"")
        buildConfigField("String", "GOOGLE_WALLET_CLASS_SUFFIX", "\"$googleWalletClassSuffix\"")
        buildConfigField("String", "GOOGLE_WALLET_ISSUER_NAME", "\"$googleWalletIssuerName\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.register("renamedDebug") {
    dependsOn("assembleDebug")
    doLast {
        val source = file("${layout.buildDirectory.get()}/outputs/apk/debug/app-debug.apk")
        val target = file("${layout.buildDirectory.get()}/outputs/apk/renamed/BusineesCard.apk")
        
        if (source.exists()) {
            target.parentFile.mkdirs()
            source.copyTo(target, overwrite = true)
            logger.lifecycle("APK GÜNCELLENDİ: ${target.absolutePath}")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // QR Code Generation
    implementation("com.google.zxing:core:3.5.3")

    // Google Wallet
    implementation("com.google.android.gms:play-services-pay:16.5.0")
}
