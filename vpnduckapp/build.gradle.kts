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
        versionCode = 15
        versionName = "2.4"
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
    implementation(project(":domain"))

    implementation("io.insert-koin:koin-android:4.0.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
}
