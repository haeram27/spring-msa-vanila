import com.google.protobuf.gradle.id

plugins {
    id("java")
    kotlin("jvm") version "2.3.0"
    id("com.google.protobuf") version "0.9.6"
}

val protocVersion: String by rootProject.extra
val grpcVersion: String by rootProject.extra
val grpcKotlinVersion: String by rootProject.extra
val springGrpcVersion: String by rootProject.extra

dependencies {
    implementation(platform("org.springframework.grpc:spring-grpc-dependencies:$springGrpcVersion"))

    implementation("com.google.protobuf:protobuf-java")
    implementation("io.grpc:grpc-stub")
    implementation("io.grpc:grpc-protobuf")

    implementation("io.grpc:grpc-kotlin-stub")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protocVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
                id("grpckt")
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/sources/proto/main/java")
            srcDirs("build/generated/sources/proto/main/grpc")
        }
        kotlin.srcDirs("build/generated/sources/proto/main/grpckt")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
