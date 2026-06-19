import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

val jvmVersion = libs.versions.jvm.get()

plugins {
    java
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
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
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion)
        targetCompatibility = JavaVersion.toVersion(jvmVersion)
        toolchain {
            languageVersion = JavaLanguageVersion.of(jvmVersion.toInt())
        }
    }

    configurations.named("compileOnly") {
        extendsFrom(configurations.named("annotationProcessor").get())
    }

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

    plugins.withId("org.springframework.boot") {
        tasks.named<Jar>("jar") {
            enabled = false
        }

        tasks.named<BootRun>("bootRun") {
            environment("spring.output.ansi.console-available", true)
        }
    }
}
