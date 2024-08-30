plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
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
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    namespace = "common"

    kotlinOptions {
        jvmTarget = "17"
    }



    dependencies {
        implementation(project(":vpn"))
        implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

        implementation(libs.dagger.hilt)
        ksp(libs.dagger.hilt.compiler)

        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.navigation.compose)

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

        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        androidTestImplementation(libs.androidx.ui.test.junit4)
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)

    }
}