plugins {
    alias(libs.plugins.android.application)
}

android {
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
        buildConfigField("String", "BASE_URL", "\"http://192.168.1.37:5000/api/\"")
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
            buildConfigField("String", "BASE_URL", "\"http://192.168.1.37:5000/api/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true   // ✅ Activa ViewBinding
        buildConfig = true   // ✅ Habilita BuildConfig
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

    // 🔹 Lifecycle (ViewModel + LiveData)
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.3")

    // 🔹 Biometría (Huella digital / FaceID)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // 🔹 Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
