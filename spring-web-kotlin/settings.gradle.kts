pluginManagement {
    val privateMavenRepositoryUrl = providers.gradleProperty("privateMavenRepositoryUrl").orNull
    if (privateMavenRepositoryUrl.isNullOrBlank()) {
        println("[WARN] gradle property 'privateMavenRepositoryUrl' is missing.")
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
}

dependencyResolutionManagement {
    val privateMavenRepositoryUrl = providers.gradleProperty("privateMavenRepositoryUrl").orNull
    if (privateMavenRepositoryUrl.isNullOrBlank()) {
        println("[WARN] gradle property 'privateMavenRepositoryUrl' is missing.")
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
