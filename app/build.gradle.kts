import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
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
        viewBinding.enable = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "key", key) // Добавлены кавычки
        }
        release {
            buildConfigField("String", "key", key) // Добавлены кавычки
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true // Перенесено из defaultConfig
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
}

dependencies {
    // Navigation - исправлены дублирования
    val nav_version = "2.8.9" // Обновлено с 1.0.0

    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // Yandex Maps
    implementation("com.yandex.android:maps.mobile:4.29.0-full")
    implementation("com.yandex.android:maps.mobile:4.29.0-navikit")
    implementation("com.yandex.mapkit.styling:automotivenavigation:4.29.0")

    // Google Play Services
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Обновлено

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Обновлено
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7") // Обновлено
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // Room
    val room_version = "2.6.1" // Обновлено с 2.5.2
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}