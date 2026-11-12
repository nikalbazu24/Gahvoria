// mkxp-z engine (modern Ruby 3.1 + classic Ruby 1.8), its touch controls and compatibility scripts.
// The app only talks to it through com.hatkid.mkxpz.GameActivity.
plugins {
    id("com.android.library")
}

android {
    namespace = "com.hatkid.mkxpz"
    compileSdk = 34
    ndkVersion = "25.2.9519653"

    defaultConfig {
        minSdk = 21
        ndk {
            // AYN Thor and current phones: 64-bit only
            abiFilters += "arm64-v8a"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    externalNativeBuild {
        ndkBuild {
            path = file("jni/Android.mk")
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // Sizes of the touch controls (gamepad_layout.xml uses _NNsdp)
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    // Translator that works on the phone (free). The player's own DeepL key is the other option.
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:language-id:17.0.6")
}
