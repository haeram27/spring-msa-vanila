plugins {
    `java-library`
}

dependencies {
    api(project(":api"))
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.netty.shaded)
}
