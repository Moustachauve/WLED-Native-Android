plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "ca.cgagnier.wlednativeandroid"
        minSdk = 24
        targetSdk = 35
        versionCode = 33
        versionName = "5.0.0"
        resourceConfigurations += listOf("en", "fr")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                )
            }
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildFeatures {
        dataBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
    }
    namespace = "ca.cgagnier.wlednativeandroid"
}

kapt {
    correctErrorTypes = true
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(composeBom)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.adaptive.layout)
    implementation(libs.androidx.adaptive.navigation)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.navigation.ui)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.webkit)
    implementation(libs.converter.moshi)
    implementation(libs.core)
    implementation(libs.core.ktx)
    implementation(libs.datastore)
    implementation(libs.datastore.core)
    implementation(libs.datastore.preferences)
    implementation(libs.hilt.android)
    implementation(libs.jsoup)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.logging.interceptor)
    implementation(libs.material.kolor)
    implementation(libs.moshi)
    implementation(libs.okhttp)
    implementation(libs.protobuf.javalite)
    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlin.coroutines.adapter)
    implementation(libs.semver4j)
    kapt(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)
    testImplementation(libs.junit)
}

protobuf {
    // Configures the Protobuf compilation and the protoc executable
    protoc {
        // Downloads from the repositories
        artifact = "com.google.protobuf:protoc:3.14.0"
    }

    // Generates the java Protobuf-lite code for the Protobufs in this project
    generateProtoTasks {
        all().forEach {
            it.builtins {
                // Configures the task output type
                create("java") {
                    // Java Lite has smaller code size and is recommended for Android
                    option("lite")
                }
            }
        }
    }
}