package com.hello.grpc.client.config.deployment

import com.hello.grpc.client.config.GrpcChannelFactory
import com.hello.grpc.client.config.PlatformProperties
import com.hello.grpc.client.config.caller.CallerContextClientInterceptor
import com.hello.grpc.client.config.caller.TenantContextClientInterceptor
import com.com.hello.grpc.client.HelloGrpcClient
import io.grpc.ManagedChannel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(PlatformProperties::class)
class HelloGrpcClientConfig(
    private val properties: PlatformProperties,
    private val channelFactory: GrpcChannelFactory
) {

    private val svc get() = properties.grpc.service.hello

    @Bean
    fun helloGrpcChannel(
        callerContextClientInterceptor: CallerContextClientInterceptor,
        tenantContextClientInterceptor: TenantContextClientInterceptor
    ): ManagedChannel {
        println("### gRPC CONNECT => ${svc.host}:${svc.grpcPort}")
        return createChannel(
            svc.host,
            svc.grpcPort,
            callerContextClientInterceptor,
            tenantContextClientInterceptor
        )
    }

    fun createChannel(host: String, port: Int, vararg interceptors: ClientInterceptor): ManagedChannel {
        val builder = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .defaultLoadBalancingPolicy("round_robin")
            .keepAliveTime(5, TimeUnit.MINUTES)
            .keepAliveTimeout(20, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(false)
        if (interceptors.isNotEmpty()) {
            builder.intercept(interceptors.toList())
        }
        return builder.build()
    }

    @Bean(destroyMethod = "close")
    fun helloGrpcClient(
        @Qualifier("helloGrpcChannel")
        deploymentChannel: ManagedChannel
    ): HelloGrpcClient {
        return HelloGrpcClient.connect(deploymentChannel, 20)
    }
}