plugins {
    alias(libs.plugins.android.library)
    // 1. Removed the kotlin.compose plugin since a network engine doesn't need compiler UI transformations
    `maven-publish`
}

android {
    namespace = "com.example.androidlibrary"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 2. Removed the buildFeatures { compose = true } block entirely

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    // 3. Kept only the essential core library dependencies for network operations and asynchronous handling
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Core underlying networking engines remain intact
    api("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.VijayAndroidTest"
                artifactId = "VijayRetrofit"
                version = "1.0.0"
            }
        }
    }
}