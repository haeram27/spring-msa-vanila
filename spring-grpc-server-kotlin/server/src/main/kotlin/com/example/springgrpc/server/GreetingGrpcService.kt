package com.example.springgrpc.server

import com.example.springgrpc.api.GreeterGrpc
import com.example.springgrpc.api.HelloRequest
import com.example.springgrpc.api.HelloResponse
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.springframework.grpc.server.service.GrpcService

@GrpcService
class GreetingGrpcService : GreeterGrpc.GreeterImplBase() {
    override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
        val name = request.name.trim()
        if (name.isBlank()) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("name must not be blank")
                    .asRuntimeException(),
            )
            return
        }

        responseObserver.onNext(
            HelloResponse.newBuilder()
                .setMessage("Hello, $name!")
                .build(),
        )
        responseObserver.onCompleted()
    }
}
