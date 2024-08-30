plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}
android {

    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 14
        versionName = "2.3"
        setProperty("archivesBaseName", "vpnduck-$versionName")
        signingConfig = signingConfigs.getByName("debug")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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


    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    namespace = "com.vpnduck"
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
