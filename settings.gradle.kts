pluginManagement {
    repositories {
        google()
        mavenCentral()
        // JetBrains Kotlin dev/eap repositories may host KSP Gradle plugin artifacts for Kotlin 2.x
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
        // Gradle Plugin Portal kept for plugin resolution
        gradlePluginPortal()
    }
    // Map KSP plugin id to Maven module coordinates (some KSP releases are published to Maven Central)
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${requested.version}")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Also include Kotlin dev/eap repos for Kotlin 2.1.x / KSP artifacts
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/eap")
    }
}

rootProject.name = "Lab3"
include(":app")
