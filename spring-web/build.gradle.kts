import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    // https://plugins.gradle.org/plugin/org.springframework.boot
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
val jvmVersion = libs.versions.jvm.get()

// set java source compatibility for java compile task
java {
    // minimum java version to compile source code
    sourceCompatibility = JavaVersion.toVersion(jvmVersion)
    // minimum java version to run the compiled bytecode, same or bigger than sourceCompatibility
    targetCompatibility = JavaVersion.toVersion(jvmVersion)

    // foojay-resolver: Apply a specific Java toolchain to ease working on different environments.
    // foojay downloads specified JDK version if not found in .gradle/toolchains/ so gradlew can automatically setup the JDK toolchain.
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion.toInt())
    }
}

kotlin {
    jvmToolchain(jvmVersion.toInt())
}

configurations {
    all {
        // exclude logback
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    named("compileOnly") {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

dependencies {
    annotationProcessor(libs.lombok)
    compileOnly(libs.lombok)
    runtimeOnly("org.postgresql:postgresql")

    // tools
    implementation("tools.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.google.code.gson:gson")
    implementation(libs.kotlin.reflect)
    implementation(libs.mybatis.spring.boot.starter)

    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework.boot:spring-boot-starter-security")  // enable auth for spring web apis
    // aop > aspectj from spring boot 4.0.x
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    testAnnotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testRuntimeOnly("com.h2database:h2")
    testImplementation("com.google.code.gson:gson")
    testImplementation(libs.kotlin.test.junit5)
    testImplementation("org.springframework.boot:spring-boot-starter-log4j2")
    // aop > aspectj from spring boot 4.0.x
    testImplementation("org.springframework.boot:spring-boot-starter-aspectj")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // ensure autoconfigure test annotations (AutoConfigureMockMvc) available
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.fromTarget(jvmVersion))
    }
}

// do not archive plain-jar(jar \wo dependency)
tasks.named<Jar>("jar") {
    enabled = false
}

/*
 * # clean a specific test result cache before run test
 * gradle test --rerun --tests 'Hello*.hello'
 * # clean all tests result cache before run tests
 * gradle test --rerun-tasks --tests 'Hello*.hello'
 */
tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
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

tasks.named<BootRun>("bootRun") {
    environment("spring.output.ansi.console-available", true)
}

/*
    # tasks = commands list
    $ gradle tasks

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