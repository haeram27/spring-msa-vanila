pluginManagement {
    val privateMavenRepositoryUrl = providers.gradleProperty("privateMavenRepositoryUrl").orNull
    if (privateMavenRepositoryUrl.isNullOrBlank()) {
        println("No private maven repository url provided, fallback to pulic maven repositories.")
    }
    repositories {
        if (!privateMavenRepositoryUrl.isNullOrBlank()) {
            maven {
                url = uri(privateMavenRepositoryUrl)
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val springBootVersion = providers.gradleProperty("springBootVersion").get()
    val fooJayVersion = providers.gradleProperty("fooJayVersion").get()
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        // jdk toolchain resolver plugin for jdk auto download and setup
        id("org.gradle.toolchains.foojay-resolver-convention") version fooJayVersion
    }
}

dependencyResolutionManagement {
    val privateMavenRepositoryUrl = providers.gradleProperty("privateMavenRepositoryUrl").orNull
    if (privateMavenRepositoryUrl.isNullOrBlank()) {
        println("No private maven repository url provided, fallback to pulic maven repositories.")
    }
    repositories {
        if (!privateMavenRepositoryUrl.isNullOrBlank()) {
            maven {
                url = uri(privateMavenRepositoryUrl)
            }
        }
        mavenCentral()
    }
}

gradle.startParameter.isOffline = false
if (gradle.startParameter.isOffline) {
    println("======================")
    println("gradle in offline mode")
    println("======================")
}

rootProject.name = "springwebex-kotlin"
