plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Reads optional Supabase credentials from local.properties (never committed).
val localPropsMap = mutableMapOf<String, String>()
rootProject.file("local.properties").takeIf { it.exists() }?.forEachLine { line ->
    val t = line.trim()
    if (t.isNotEmpty() && !t.startsWith("#") && "=" in t) {
        val p = t.split("=", limit = 2)
        localPropsMap[p[0].trim()] = p[1].trim()
    }
}
val supabaseUrl: String = localPropsMap["SUPABASE_URL"] ?: ""
val supabaseKey: String = localPropsMap["SUPABASE_KEY"] ?: ""

android {
    namespace = "com.oaa.livingresume"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.oaa.livingresume"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose (Material 3) + navigation (bottom bar, 5 tabs)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Coroutines + StateFlow
    implementation(libs.kotlinx.coroutines.android)

    // QR codes for the recruiter resume (ZXing)
    implementation(libs.zxing.core)

    // Supabase (Postgrest + Auth + Storage). The app compiles & runs on mock data
    // without any Supabase configuration - see README.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// supabase auth-kt transitively pulls androidx.browser:1.9.0, which demands
// compileSdk 36 + AGP 8.9.1. Pin the last version compatible with this toolchain.
configurations.all {
    resolutionStrategy {
        force("androidx.browser:browser:1.8.0")
    }
}
