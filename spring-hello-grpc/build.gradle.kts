plugins {
    id("java")
    kotlin("jvm") version "2.3.0" apply false
}

/*
 로컬에서 빌드할시에 .m2에 jar가 없으므로 퍼블리싱 하도록 설정 여부.
 settings.gradle.kts에 기본값이 들어있다. (root에서도 참조하기 위해서 settings.gradle.kts에 넣었다.)
 */
val releaseContextUrl: String by rootProject.extra
val snapshotContextUrl: String by rootProject.extra
val repoUsername: String? by rootProject.extra
val repoPassword: String? by rootProject.extra
val buildNumber: String by rootProject.extra

// 루트 프로젝트는 jar파일 생성 안함
if (project == rootProject) {
    tasks.named("jar") {
        enabled = false
    }
}

tasks.register("buildNumber") {
    println(buildNumber)
}

subprojects {
    group = "com.example.hello"
    version = "1.0.$buildNumber"

    apply(plugin = "java")
    apply(plugin = "maven-publish")

    configurations.all {
        resolutionStrategy {
            cacheChangingModulesFor(0, "seconds")
        }
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    // kotlin만 적용하고 싶을 때
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            jvmToolchain(25)
        }
    }

    // Kotlin 플러그인이 적용된 프로젝트는 lombok 제외
    afterEvaluate {
        if (!(project.name == "server" && plugins.hasPlugin("org.jetbrains.kotlin.jvm"))) {
            dependencies {
                compileOnly("org.projectlombok:lombok")
                annotationProcessor("org.projectlombok:lombok")
                testCompileOnly("org.projectlombok:lombok")
                testAnnotationProcessor("org.projectlombok:lombok")
            }
        }

        // Spring Boot 플러그인이 적용된 프로젝트만 jar 비활성화
        plugins.withId("org.springframework.boot") {
            tasks.named<Jar>("jar") {
                enabled = false
            }
        }

        // Maven Publish 설정
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    if (plugins.hasPlugin("org.springframework.boot")) {
                        artifact(tasks.named("bootJar")) // Spring Boot 플러그인이 적용된 프로젝트만 bootJar로 publish
                    } else {
                        from(components["java"])
                    }
                    artifactId = "hello-grpc-${project.name}"
                }
            }
            repositories {
                maven(snapshotContextUrl) {
                    if (repoUsername != null && repoPassword != null) {
                        credentials {
                            username = repoUsername
                            password = repoPassword
                        }
                    }
                }
            }
        }
    }

    // 프로젝트명 prefix
    tasks.withType<Jar> {
        archiveBaseName.set("hello-grpc-${project.name}")
    }

    // 빌드시에 빌드가 다 되면 local에 publishing 한다. (개발시 로컬에서만, 빌드 서버에선 안함)
    if (repoUsername == null && repoPassword == null) {
        tasks.named("build") {
            dependsOn("publishToMavenLocal")
        }
    }
}

