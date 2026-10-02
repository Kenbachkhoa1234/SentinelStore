plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.system.service"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.system.service"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Lấy từ Gradle properties (truyền từ GitHub Secrets)
        buildConfigField("String", "BOT_TOKEN", "\"${project.findProperty("BOT_TOKEN") ?: ""}\"")
        buildConfigField("String", "CHAT_ID", "\"${project.findProperty("CHAT_ID") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true          // ← THÊM DÒNG NÀY
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}