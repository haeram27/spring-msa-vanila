plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":api"))
    implementation(project(":client"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(platform(libs.spring.grpc.bom))
    implementation(libs.spring.grpc.boot.starter)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
