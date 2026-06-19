plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":api"))
    implementation(project(":client"))
    implementation(libs.kotlin.reflect)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(platform(libs.spring.grpc.bom))
    implementation(libs.spring.grpc.boot.starter)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
