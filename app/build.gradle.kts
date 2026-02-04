import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val key: String = gradleLocalProperties(rootDir, providers).getProperty("MAPKIT_API_KEY")

android {
    namespace = "com.example.testmapkit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.testmapkit"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "key", key)
        }
        release {
            buildConfigField("String", "key", key)
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("com.yandex.android:maps.mobile:4.29.0-full")
    implementation("com.yandex.android:maps.mobile:4.29.0-navikit")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.yandex.mapkit.styling:automotivenavigation:4.29.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}