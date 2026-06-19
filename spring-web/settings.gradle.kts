// plugin repositories
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

// dependency repositories
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from("com.example.build:gradle-version-catalog:1.0.0-SNAPSHOT")
        }
    }
}

startParameter.isOffline = false
if (startParameter.isOffline) {
    println("======================")
    println("gradle in offline mode")
    println("======================")
}

rootProject.name = "springwebex"