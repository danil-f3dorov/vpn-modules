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
        versionCode = 22
        versionName = "3.0.2"
        setProperty("archivesBaseName", "vpnduck-$versionName")
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
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isShrinkResources = false
            isMinifyEnabled = false
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    namespace = "com.vpnduck"}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))

    implementation(libs.koin.android)
    implementation("androidx.compose.material:material:1.7.5")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    debugImplementation(libs.androidx.ui.tooling)
}
