plugins {
    id("java")
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

val buildNumber: String by rootProject.extra

dependencies {
    // lombok
    annotationProcessor("org.projectlombok:lombok")
    compileOnly("org.projectlombok:lombok")

    // springboot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")

    // grpc
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")

    // common
    implementation(project(":api"))
 
    // querydsl
    implementation("io.github.openfeign.querydsl:querydsl-jpa")
    implementation("io.github.openfeign.querydsl:querydsl-apt")

    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
//     systemProperty("spring.profiles.active", project.findProperty("spring.profiles.active")?.toString() ?: "local")
// }
