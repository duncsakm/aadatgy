plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.ivi2000.aadatgy"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ivi2000.aadatgy"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_13
        targetCompatibility = JavaVersion.VERSION_13
    }
    kotlinOptions {
        jvmTarget = "13"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.okio)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // ML Kit Barcode Scanning
    //noinspection Aligned16KB
    implementation(libs.barcode.scanning)

    implementation(libs.androidx.core.ktx)

}