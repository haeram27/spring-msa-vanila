// plugin repositories
pluginManagement {
    repositories {
        val privateMavenRepositoryUrl = settings.providers
            .gradleProperty("privateMavenRepositoryUrl")
            .orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (privateMavenRepositoryUrl != null) {
            maven {
                url = uri(privateMavenRepositoryUrl)

                // if url is NOT https
                // isAllowInsecureProtocol = true

                // if authentication is required
                // credentials {
                //     username = "user"
                //     password = "password"
                // }
            }
        } else {
            println("pluginManagement: No private maven repository url provided, fallback to public maven repositories.")
        }
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

// dependency repositories
dependencyResolutionManagement {
    repositories {
        val privateMavenRepositoryUrl = settings.providers
            .gradleProperty("privateMavenRepositoryUrl")
            .orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        if (privateMavenRepositoryUrl != null) {
            maven {
                url = uri(privateMavenRepositoryUrl)

                // if url is NOT https
                // isAllowInsecureProtocol = true

                // if authentication is required
                // credentials {
                //     username = "user"
                //     password = "password"
                // }
            }
        } else {
            println("dependencyResolutionManagement: No private maven repository url provided, fallback to public maven repositories.")
        }
        mavenCentral()
    }
}

startParameter.isOffline = false
if (startParameter.isOffline) {
    println("======================")
    println("gradle in offline mode")
    println("======================")
}

rootProject.name = "gradle-version-catalog"
