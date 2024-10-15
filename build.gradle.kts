buildscript {
    val kotlin_version = "1.9.21"

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.0")
        classpath(kotlin("gradle-plugin", version = kotlin_version))
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.1")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.1")
        classpath(kotlin("gradle-plugin", version = kotlin_version))
    }
}

plugins {
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

tasks.register("clean",Delete::class){
    delete(layout.buildDirectory)
}