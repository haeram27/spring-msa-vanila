plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(platform(libs.spring.grpc.bom))
    api(project(":api"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.netty.shaded)
}
