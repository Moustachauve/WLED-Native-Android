plugins {
    id("com.android.application")
    id("com.google.protobuf")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("com.google.dagger.hilt.android")
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "ca.cgagnier.wlednativeandroid"
        minSdk = 24
        targetSdk = 34
        versionCode = 33
        versionName = "4.1.1"
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
    val moshiVersion = "1.15.1"
    val retrofit2Version = "2.11.0"
    val okhttp3Version = "5.0.0-alpha.14"
    val roomVersion = "2.6.1"

    //noinspection GradleDependency Version is tied to the kotlin version
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("androidx.datastore:datastore-core:1.1.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.8.3")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.webkit:webkit:1.12.0")

    implementation("com.squareup.moshi:moshi:$moshiVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    implementation("com.squareup.retrofit2:retrofit:$retrofit2Version")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit2Version")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    implementation("com.squareup.okhttp3:okhttp:$okhttp3Version")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.14")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.3")
    // Material Design 3
    implementation("androidx.compose.material3:material3")
    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.compose.material3.adaptive:adaptive-layout")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation")
    implementation("androidx.lifecycle:lifecycle-runtime-compose")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose")

    implementation("com.materialkolor:material-kolor:2.0.0")

    implementation("com.google.protobuf:protobuf-javalite:4.26.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$roomVersion")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // DI - hilt
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("io.noties.markwon:core:4.6.2")
    implementation("org.jsoup:jsoup:1.18.1")
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