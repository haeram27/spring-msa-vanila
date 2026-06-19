package com.example.springgrpc.client

import com.example.springgrpc.api.GreeterGrpc
import com.example.springgrpc.api.HelloRequest
import com.example.springgrpc.api.HelloResponse
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

class HelloGrpcClient private constructor(
    private val channel: ManagedChannel,
    private val timeoutSeconds: Long,
) : AutoCloseable {
    private val stub = GreeterGrpc.newBlockingStub(channel)

    fun sayHello(name: String): HelloResponse {
        require(name.isNotBlank()) { "name must not be blank" }

        return stub
            .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
            .sayHello(
                HelloRequest.newBuilder()
                    .setName(name)
                    .build(),
            )
    }

    override fun close() {
        channel.shutdown()
        channel.awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {
        fun connect(host: String, port: Int, timeoutSeconds: Long = 5): HelloGrpcClient {
            require(host.isNotBlank()) { "host must not be blank" }
            require(port > 0) { "port must be greater than 0" }
            require(timeoutSeconds > 0) { "timeoutSeconds must be greater than 0" }

            val channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build()

            return HelloGrpcClient(channel, timeoutSeconds)
        }
    }
}
