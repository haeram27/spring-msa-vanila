plugins {
    id("java")
    id("com.google.protobuf") version "0.9.6"
}

val springGrpcVersion: String by rootProject.extra
val buildNumber: String by rootProject.extra

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation(platform("org.springframework.grpc:spring-grpc-dependencies:$springGrpcVersion"))
    implementation(project(":api"))

    implementation("io.grpc:grpc-stub")
    implementation("io.grpc:grpc-protobuf")
    compileOnly("io.grpc:grpc-netty-shaded")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
