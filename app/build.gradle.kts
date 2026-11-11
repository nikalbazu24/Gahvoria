plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Build number from GitHub Actions: every build has a higher versionCode, so the updater knows which is newer.
val buildNumber = (System.getenv("GAHVORIA_BUILD_NUMBER") ?: "0").toInt()

// Fixed signature: the keystore and its password come from the repository secrets.
val keystorePath: String? = System.getenv("GAHVORIA_KEYSTORE_FILE")
val keystorePassword: String? = System.getenv("GAHVORIA_KEYSTORE_PASSWORD")
val hasReleaseKey = !keystorePath.isNullOrEmpty() && !keystorePassword.isNullOrEmpty() && file(keystorePath!!).exists()

android {
    namespace = "io.github.nikalbazu24.gahvoria"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.nikalbazu24.gahvoria"
        minSdk = 21
        targetSdk = 33
        versionCode = maxOf(1, buildNumber)
        versionName = "0.1.$buildNumber"
        buildConfigField("String", "UPDATE_BASE", "\"https://github.com/nikalbazu24/Gahvoria/releases/download/dev/\"")
        ndk { abiFilters += "arm64-v8a" }
    }

    signingConfigs {
        create("gahvoria") {
            if (hasReleaseKey) {
                storeFile = file(keystorePath!!)
                storePassword = keystorePassword
                keyAlias = "gahvoria"
                keyPassword = keystorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Without the key (a fork, a pull request) the APK uses the debug signature.
            signingConfig = if (hasReleaseKey) signingConfigs.getByName("gahvoria") else signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    packaging {
        jniLibs { useLegacyPackaging = true }
    }

    testOptions { unitTests.isReturnDefaultValues = true }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(project(":engine"))
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    testImplementation("junit:junit:4.13.2")
}
