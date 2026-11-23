plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.larkplayer.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.larkplayer.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = false
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,LICENSE,NOTICE}"
        }
        jniLibs {
            pickFirsts += listOf("**/libc++_shared.so")
        }
    }
}

dependencies {
    // Core Android
    val coreKtx = "1.12.0"
    val appcompat = "1.6.1"
    val material = "1.11.0"
    val constraintLayout = "2.1.4"
    val activity = "1.8.2"
    val fragment = "1.6.2"
    val lifecycle = "2.7.0"
    val room = "2.6.1"
    val navigation = "2.7.6"
    val workManager = "2.9.0"
    val datastore = "1.0.0"
    val paging = "3.2.1"

    // Media
    val exoplayer = "2.19.1"
    val media3 = "1.3.1"

    // DI
    val hilt = "2.51.1"

    // Coroutines & Flow
    val coroutines = "1.7.3"
    val flow = "1.7.3"

    // Serialization
    val serialization = "1.6.1"

    // Image loading
    val coil = "2.5.0"

    // UI
    val lottie = "6.1.0"

    // Testing
    val junit = "4.13.2"
    val espresso = "3.5.1"
    val truth = "1.1.3"
    val robolectric = "4.11"
    val mockk = "1.13.13"

    implementation("androidx.core:core-ktx:$coreKtx")
    implementation("androidx.appcompat:appcompat:$appcompat")
    implementation("com.google.android.material:material:$material")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayout")
    implementation("androidx.activity:activity-ktx:$activity")
    implementation("androidx.fragment:fragment-ktx:$fragment")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle")

    // Room Database
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navigation")
    implementation("androidx.navigation:navigation-ui-ktx:$navigation")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workManager")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:$datastore")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:$paging")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // ExoPlayer / Media3
    implementation("com.google.android.exoplayer:exoplayer:$exoplayer")
    implementation("com.google.android.exoplayer:exoplayer-ui:$exoplayer")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-ui:$media3")
    implementation("androidx.media3:media3-database:$media3")
    implementation("androidx.media3:media3-datasource-okhttp:$media3")

    // Hilt
    implementation("com.google.dagger:hilt-android:$hilt")
    ksp("com.google.dagger:hilt-compiler:$hilt")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization")

    // Image Loading - Coil
    implementation("io.coil-kt:coil:$coil")
    implementation("io.coil-kt:coil-compose:$coil")

    // Image Loading - Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Google Drive backup
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.http-client:google-http-client-gson:1.44.1")
    implementation("com.google.apis:google-api-services-drive:v3-rev20231113-2.0.0")

    // Lottie for animations
    implementation("com.airbnb.android:lottie:$lottie")

    // Testing
    testImplementation("junit:junit:$junit")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("io.mockk:mockk:$mockk")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso")
    androidTestImplementation("com.google.truth:truth:$truth")
    androidTestImplementation("org.robolectric:robolectric:$robolectric")
}