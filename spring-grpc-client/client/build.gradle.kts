plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.spring.grpc.bom))
    api(project(":api"))
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.netty.shaded)
}
