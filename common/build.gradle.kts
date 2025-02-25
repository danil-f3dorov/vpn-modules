plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.android)
}
android {

    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    buildTypes {
        release {
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }

    namespace = "com.common"

    kotlinOptions {
        jvmTarget = "17"
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }


    dependencies {
        implementation(project(":vpn"))
        implementation(project(":dunta_sdk"))
        implementation(project(":domain"))
        implementation(project(":data"))

        implementation("com.google.dagger:dagger:2.49")
        ksp("com.google.dagger:dagger-compiler:2.49")

        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.foundation.android)

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

        implementation(libs.installreferrer)
        implementation(libs.work.runtime.ktx)

        implementation(libs.preference.ktx)
    }
}

