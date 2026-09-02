import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.aistudio.jumpvpn.vpytrs"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath =
                System.getenv("KEYSTORE_PATH")
                    ?: "${rootDir}/my-upload-key.jks"

            storeFile = file(keystorePath)
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }

        create("debugConfig") {
            storeFile = file("${rootDir}/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig =
                signingConfigs.getByName("debugConfig")
        }

        release {
            isMinifyEnabled = false
            isCrunchPngs = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            signingConfig =
                signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = true
    }
}

secrets {
    propertiesFileName = ".env"
    defaultPropertiesFileName = ".env.example"

    ignoreList.add(
        "FIREBASE_APPCHECK_DEBUG_TOKEN"
    )
}

googleServices {
    missingGoogleServicesStrategy =
        MissingGoogleServicesStrategy.WARN
}

dependencies {

    // -----------------------------------------------------
    // Android / Compose
    // -----------------------------------------------------

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(
        libs.androidx.activity.compose
    )

    implementation(
        libs.androidx.compose.material.icons.core
    )

    implementation(
        libs.androidx.compose.material.icons.extended
    )

    implementation(
        libs.androidx.compose.material3
    )

    implementation(
        libs.androidx.compose.ui
    )

    implementation(
        libs.androidx.compose.ui.graphics
    )

    implementation(
        libs.androidx.compose.ui.tooling.preview
    )

    implementation(
        libs.androidx.core.ktx
    )

    // -----------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------

    implementation(
        libs.androidx.lifecycle.runtime.compose
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.compose
    )

    // -----------------------------------------------------
    // Room
    // -----------------------------------------------------

    implementation(
        libs.androidx.room.ktx
    )

    implementation(
        libs.androidx.room.runtime
    )

    ksp(
        libs.androidx.room.compiler
    )

    // -----------------------------------------------------
    // Firebase
    // -----------------------------------------------------

    implementation(
        platform(libs.firebase.bom)
    )

    implementation(
        libs.firebase.ai
    )

    implementation(
        libs.firebase.appcheck.recaptcha
    )

    implementation(
        libs.firebase.appcheck.debug
    )

    // -----------------------------------------------------
    // Networking
    // -----------------------------------------------------

    implementation(
        libs.okhttp
    )

    implementation(
        libs.retrofit
    )

    implementation(
        libs.converter.moshi
    )

    implementation(
        libs.logging.interceptor
    )

    // -----------------------------------------------------
    // Moshi
    // -----------------------------------------------------

    implementation(
        libs.moshi.kotlin
    )

    ksp(
        libs.moshi.kotlin.codegen
    )

    // -----------------------------------------------------
    // Kotlin Coroutines
    // -----------------------------------------------------

    implementation(
        libs.kotlinx.coroutines.android
    )

    implementation(
        libs.kotlinx.coroutines.core
    )

    // -----------------------------------------------------
    // sing-box libbox 1.13.14
    //
    // IMPORTANT:
    // File must exist at:
    //
    // app/libs/libbox.aar
    //
    // Do NOT use the JitPack dependency here.
    // -----------------------------------------------------

    implementation(
        files("libs/libbox.aar")
    )

    // -----------------------------------------------------
    // Unit Tests
    // -----------------------------------------------------

    testImplementation(
        libs.junit
    )

    testImplementation(
        libs.androidx.junit
    )

    testImplementation(
        libs.androidx.core
    )

    testImplementation(
        libs.kotlinx.coroutines.test
    )

    testImplementation(
        libs.robolectric
    )

    testImplementation(
        libs.roborazzi
    )

    testImplementation(
        libs.roborazzi.compose
    )

    testImplementation(
        libs.roborazzi.junit.rule
    )

    testImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    // -----------------------------------------------------
    // Android Tests
    // -----------------------------------------------------

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.runner
    )

    // -----------------------------------------------------
    // Debug
    // -----------------------------------------------------

    debugImplementation(
        libs.androidx.compose.ui.tooling
    )

    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
}
