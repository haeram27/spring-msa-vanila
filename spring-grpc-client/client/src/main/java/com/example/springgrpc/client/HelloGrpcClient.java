package com.example.springgrpc.client;

import com.example.springgrpc.api.GreeterGrpc;
import com.example.springgrpc.api.HelloRequest;
import com.example.springgrpc.api.HelloResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public final class HelloGrpcClient implements AutoCloseable {
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub stub;
    private final long timeoutSeconds;

    private HelloGrpcClient(ManagedChannel channel, long timeoutSeconds) {
        this.channel = channel;
        this.stub = GreeterGrpc.newBlockingStub(channel);
        this.timeoutSeconds = timeoutSeconds;
    }

    public static HelloGrpcClient connect(String host, int port) {
        return connect(host, port, 5);
    }

    public static HelloGrpcClient connect(String host, int port, long timeoutSeconds) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than 0");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than 0");
        }

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();

        return new HelloGrpcClient(channel, timeoutSeconds);
    }

    public HelloResponse sayHello(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        return stub
            .withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS)
            .sayHello(
                HelloRequest.newBuilder()
                    .setName(name)
                    .build()
            );
    }

    @Override
    public void close() throws InterruptedException {
        channel.shutdown();
        channel.awaitTermination(5, TimeUnit.SECONDS);
    }
}
