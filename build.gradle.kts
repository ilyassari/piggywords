// Top-level build file where you can add configuration options common to all sub-projects/modules.
// build.gradle.kts (Project: Root Directory)

plugins {
    // Android Application and Library plugins
    id("com.android.application") version "8.4.1" apply false
    id("com.android.library") version "8.4.1" apply false

    // Kotlin and Compose plugins. Version 2.0.0 is used for better stability
    // and compatibility with KSP.
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false

    // KSP (Kotlin Symbol Processing) plugin. This is REQUIRED for annotation processors like Room.
    // The version should be compatible with Kotlin 2.0.0.
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}