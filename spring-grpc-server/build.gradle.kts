import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

val jvmVersion = libs.versions.jvm.get()
val springGrpcBom = libs.spring.grpc.bom
val repoUsername = providers.gradleProperty("repoUsername").orNull
val repoPassword = providers.gradleProperty("repoPassword").orNull

plugins {
    java
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.protobuf) apply false
}

group = "com.example.springgrpc"
version = "0.0.1-SNAPSHOT"

tasks.named<Jar>("jar") {
    enabled = false
}

subprojects {
    // Inherit a single group/version from the root project for consistent coordinates.
    group = rootProject.group
    version = rootProject.version

    // Apply Java conventions and Maven publication support to every submodule.
    pluginManager.apply("java")
    pluginManager.apply("maven-publish")

    // Always re-check changing modules to avoid stale snapshot-like dependencies.
    configurations.configureEach {
        resolutionStrategy {
            cacheChangingModulesFor(0, "seconds")
        }
    }

    // Standardize Java source/target compatibility and toolchain JDK.
    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion)
        targetCompatibility = JavaVersion.toVersion(jvmVersion)
        toolchain {
            languageVersion = JavaLanguageVersion.of(jvmVersion.toInt())
        }
    }

    // Let compileOnly inherit annotationProcessor dependencies (e.g., Lombok).
    configurations.named("compileOnly") {
        extendsFrom(configurations.named("annotationProcessor").get())
    }

    dependencies {
        // Mark internal com.example artifacts as changing to always fetch fresh metadata/artifacts.
        components.all {
            if (id.group.startsWith("com.example")) {
                isChanging = true
            }
        }

        // Root script does not expose type-safe accessors like implementation(...).
        // Apply the shared BOM to all submodules through the implementation configuration.
        add("implementation", platform(springGrpcBom))
    }

    // Create a default Maven publication for each submodule.
    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                // Publish the standard Java component output (jar + metadata).
                from(components["java"])
                // Use predictable artifact IDs per module.
                artifactId = "${rootProject.name}-${project.name}"
            }
        }
    }

    // Unify test platform and verbose logging across submodules.
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
            showCauses = true
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR,
            )
        }
    }

    // Apply Kotlin-specific JVM/toolchain/compiler settings only when Kotlin JVM plugin is present.
    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(jvmVersion.toInt())
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                freeCompilerArgs.add("-Xjsr305=strict")
                jvmTarget.set(JvmTarget.fromTarget(jvmVersion))
            }
        }
    }

    // Spring Boot modules publish bootJar instead of the plain jar.
    plugins.withId("org.springframework.boot") {
        // Disable plain jar when Spring Boot packaging is used.
        tasks.named<Jar>("jar") {
            enabled = false
        }

        extensions.configure<PublishingExtension> {
            publications.named<MavenPublication>("maven") {
                // Override publication artifact to bootJar for executable Spring Boot artifact publishing.
                setArtifacts(listOf(tasks.named("bootJar").get()))
            }
        }

        // Keep console ANSI output available in local bootRun sessions.
        tasks.named<BootRun>("bootRun") {
            environment("spring.output.ansi.console-available", true)
        }
    }

    // In local/dev mode (no repo credentials), include Maven Local publishing in the build graph.
    // dependsOn means publishToMavenLocal runs before build completes (not after build).
    if (repoUsername == null && repoPassword == null) {
        tasks.named("build") {
            dependsOn("publishToMavenLocal")
        }
    }
}
