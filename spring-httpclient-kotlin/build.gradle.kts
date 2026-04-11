import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmVersion = providers.gradleProperty("jvmVersion").get().toInt()
val kotlinLoggingVersion = providers.gradleProperty("kotlinLoggingVersion").get()
val springBootVersion = providers.gradleProperty("springBootVersion").get()

plugins {
    java
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

configurations {
    all {
        // exclude default logging framework (logback) to use log4j2
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    implementation("io.github.oshai:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")
    implementation("org.apache.httpcomponents.client5:httpclient5")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-log4j2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// set java source compatibility for java compile task
java {
    // minimum java version to compile source code
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    // minimum java version to run the compiled bytecode, same or bigger than sourceCompatibility
    targetCompatibility = JavaVersion.toVersion(jvmVersion)

    // foojay-resolver: Apply a specific Java toolchain to ease working on different environments.
    // foojay downloads specified JDK version if not found in .gradle/toolchains/ so gradlew can automatically setup the JDK toolchain.
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion)
    }
}

// set jvm toolchain for kotlin compile task
kotlin {
    jvmToolchain(jvmVersion)
}

// kotlin compiler options
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.fromTarget(jvmVersion.toString()))
    }
}

// do not archive plain-jar(jar \wo dependency)
tasks.jar {
    enabled = false
}

/*
 * # clean a specific test result cache before run test
 * gradle test --rerun --tests 'Hello*.hello'
 * # clean all tests result cache before run tests
 * gradle test --rerun-tasks --tests 'Hello*.hello'
 */
tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showExceptions = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events(
            "passed",
            "skipped",
            "failed",
            "standardOut",
            "standardError"
        )
    }
}

// make spring output colorful in console
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    environment("spring.output.ansi.console-available", "true")
}

/*
    # tasks = commands list
    $ gradle tasks --all

        Application tasks
        -----------------
        bootRun - Runs this project as a Spring Boot application.
        bootTestRun - Runs this project as a Spring Boot application using the test runtime classpath.

        Build tasks
        -----------
        assemble - Assembles the outputs of this project.
        bootBuildImage - Builds an OCI image of the application using the output of the bootJar task
        bootJar - Assembles an executable jar archive containing the main classes and their dependencies.
        build - Assembles and tests this project.
        buildDependents - Assembles and tests this project and all projects that depend on it.
        buildNeeded - Assembles and tests this project and all projects it depends on.
        classes - Assembles main classes.
        clean - Deletes the build directory.
        jar - Assembles a jar archive containing the classes of the 'main' feature.
        resolveMainClassName - Resolves the name of the application's main class.
        resolveTestMainClassName - Resolves the name of the application's test main class.
        testClasses - Assembles test classes.

    # assemble = build - text
    $ gradle assemble --refresh-dependencies
    $ gradle build -x test --refresh-dependencies

    # build = assemble + test
    $ gradle build --refresh-dependencies

    # test = run junit test
    $ gradle test --rerun-tasks --tests "ClassTests.MethodTest"

    # dependency version checked by 'io.spring.dependency-management' plugin serves compatible versions of lib using each BOM
    $ gradle dependencies
    $ gradle dependencyManagement
    $ gradle dependencyInsight --dependency httpclient5
*/
