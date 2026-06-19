import com.google.protobuf.gradle.id

plugins {
    `java-library`
    alias(libs.plugins.protobuf)
}

dependencies {
    api(platform(libs.spring.grpc.bom))
    api(libs.protobuf.java)
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}

sourceSets {
    named("main") {
        java.srcDirs(
            "build/generated/sources/proto/main/java",
            "build/generated/sources/proto/main/grpc",
        )
    }
}
