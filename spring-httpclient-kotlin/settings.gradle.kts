pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = providers.gradleProperty("kotlinVersion").get()
    val springBootVersion = providers.gradleProperty("springBootVersion").get()
    val springDependencyManagementVersion = providers.gradleProperty("springDependencyManagementVersion").get()
    val fooJayVersion = providers.gradleProperty("fooJayVersion").get()
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        // jdk toolchain resolver plugin for jdk auto download and setup
        id("org.gradle.toolchains.foojay-resolver-convention") version fooJayVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

gradle.startParameter.isOffline = false
if (gradle.startParameter.isOffline) {
    println("======================")
    println("gradle in offline mode")
    println("======================")
}

rootProject.name = "spring-httpclient-kotlin"
