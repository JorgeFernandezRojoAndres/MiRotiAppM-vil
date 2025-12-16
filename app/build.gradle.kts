import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    val localProperties = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.reader()?.use { load(it) }
    }
    val backendHost = localProperties.getProperty("backendHost") ?: "192.168.1.36"
    val localBaseUrl = "\"http://$backendHost:8080/api/\""

    namespace = "com.jorge.mirotimobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jorge.mirotimobile"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ✅ Habilitar soporte de VectorDrawable en versiones antiguas
        vectorDrawables.useSupportLibrary = true

        // ✅ URL base editable sin tocar código
        buildConfigField("String", "BASE_URL", localBaseUrl)
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // 🔹 URL distinta para producción
            buildConfigField("String", "BASE_URL", "\"https://api.miroti.com/api/\"")
        }

        debug {
            // 🔹 URL para entorno local
            buildConfigField("String", "BASE_URL", localBaseUrl)
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true   // ✅ Activa ViewBinding
        buildConfig = true   // ✅ Habilita BuildConfig
        dataBinding = true   // ✅ Necesario para layouts `<layout>`
    }
}

dependencies {
    // 🔹 Dependencias base de Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 🔹 Retrofit + Gson converter (versiones estables)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 🔹 OkHttp3 + Logging interceptor
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 🔹 Imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 🔹 Lifecycle (ViewModel + LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.3")

    // 🔹 Biometría (Huella digital / FaceID)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // 🔹 Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
