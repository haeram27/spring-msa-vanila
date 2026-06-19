package com.example.springgrpc.server;

import com.example.springgrpc.api.GreeterGrpc;
import com.example.springgrpc.api.HelloRequest;
import com.example.springgrpc.api.HelloResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class GreetingGrpcService extends GreeterGrpc.GreeterImplBase {
    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName().trim();
        if (name.isBlank()) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("name must not be blank")
                    .asRuntimeException()
            );
            return;
        }

        responseObserver.onNext(
            HelloResponse.newBuilder()
                .setMessage("Hello, " + name + "!")
                .build()
        );
        responseObserver.onCompleted();
    }
}
