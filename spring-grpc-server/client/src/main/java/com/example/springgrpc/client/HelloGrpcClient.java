package com.example.springgrpc.client;

import com.example.springgrpc.api.HelloGrpc;
import com.example.springgrpc.api.HelloRequest;
import com.example.springgrpc.api.HelloResponse;

import io.grpc.ManagedChannel;

import java.util.Map;

/** {@code com.example.grpc.v1.Hello} 서비스 호출용 클라이언트. */
public final class HelloGrpcClient extends AbstractGrpcClient {

    private final HelloGrpc.HelloBlockingStub stub;

    private HelloGrpcClient(ManagedChannel channel, long timeoutSeconds) {
        super(channel, timeoutSeconds);
        this.stub = HelloGrpc.newBlockingStub(channel);
    }

    public static HelloGrpcClient connect(String host, int port) {
        return connect(host, port, 5);
    }

    public static HelloGrpcClient connect(String host, int port, long timeoutSeconds) {
        return new HelloGrpcClient(openChannel(host, port, timeoutSeconds), timeoutSeconds);
    }

    public HelloResponse hello(String message) {
        return hello(message, Map.of());
    }

    public HelloResponse hello(String message, Map<String, String> headers) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }

        return callStub(stub, headers).hello(
            HelloRequest.newBuilder()
                .setMessage(message)
                .build()
        );
    }
}
