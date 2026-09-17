plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"

}

android {

    namespace = "com.example.mcptestapp"
    compileSdk {
        version = release(37) {

        }
    }

    defaultConfig {
        applicationId = "com.example.mcptestapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            force("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    val ktorVersion = "2.3.7"
    androidTestImplementation("io.ktor:ktor-server-core:$ktorVersion")
    androidTestImplementation("io.ktor:ktor-server-cio:$ktorVersion")
    androidTestImplementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    androidTestImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    val coroutinesVersion = "1.8.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    val roomVersion = "2.8.4" // Or match whatever version is in your version catalog
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
