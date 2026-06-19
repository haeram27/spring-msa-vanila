rootProject.name = "hello-grpc-server"

include("api", "client", "server")

val buildNumber = "0"

// 하위 프로젝트들 참조하기 위함
gradle.beforeProject {
    // 공용 설정
    extra["repoUsername"] = repoUsername
    extra["repoPassword"] = repoPassword
    extra["artifactoryContextUrl"] = artifactoryContextUrl
    extra["releaseContextUrl"] = releaseContextUrl
    extra["snapshotContextUrl"] = snapshotContextUrl
    extra["buildNumber"] = buildNumber

    // 버전 관리
    extra["springGrpcVersion"] = "1.0.2"
    extra["protocVersion"] = "4.33.4"
    extra["grpcVersion"] = "1.77.1"
    extra["grpcKotlinVersion"] = "1.5.0"
}

pluginManagement {
    repositories {
        // maven(artifactoryContextUrl)
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(artifactoryContextUrl)
        if (repoUsername != null && repoPassword != null) {
            maven(releaseContextUrl)
            maven(snapshotContextUrl)
            mavenLocal()
        } else {
            mavenLocal()
            maven(releaseContextUrl)
            maven(snapshotContextUrl)
        }
    }
}