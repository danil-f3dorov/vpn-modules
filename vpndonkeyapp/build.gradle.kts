plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}
android {

    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        setProperty("archivesBaseName", "vpndonkey-$versionName")
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
        viewBinding = true
        buildConfig = true
    }

    namespace = "com.donkeyvpn"

    defaultConfig {
        applicationId = "vpn.donkeyapp"
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    dependencies {
        implementation(project(":common"))

        implementation(libs.jackson.core)
        implementation(libs.jackson.databind)
        implementation(libs.brotli.dec)

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity.ktx)
        implementation(libs.constraintlayout)

        implementation(libs.retrofit)
        implementation(libs.converter.gson)
        implementation(libs.logging.interceptor)

        implementation(libs.room.ktx)
        ksp(libs.room.compiler)

        implementation(libs.lottie)

        implementation(libs.installreferrer)
        implementation(libs.work.runtime.ktx)

        implementation(libs.preference.ktx)
    }
}