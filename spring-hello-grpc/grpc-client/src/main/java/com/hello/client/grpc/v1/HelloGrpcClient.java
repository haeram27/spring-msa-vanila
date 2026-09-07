package com.hello.client.grpc;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.example.api.hello.grpc.v1.HelloGrpcService;
import com.example.api.hello.grpc.v1.HelloRequest;
import com.example.api.hello.grpc.v1.HelloResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloGrpcClient implements AutoCloseable {

    private final ManagedChannel channel;
    private final HelloGrpcService.HelloGrpcServiceBlockingStub baseStub;
    private final long timeoutSeconds;

    /**
     * timeoutSeconds is the per-RPC response wait limit (deadline) for this stub.
     * The timeout configured on this stub is applied to all RPC calls made through this stub.
     * To use a different timeout, create a new stub with a different deadline.
     */
    private HelloGrpcClient(ManagedChannel channel, long timeoutSeconds) {
        log.info("HelloGrpcClient#HelloGrpcClient called");
        this.channel = channel;
        this.baseStub = HelloGrpcService.newBlockingStub(channel);
        this.timeoutSeconds = timeoutSeconds;
    }

    public static HelloGrpcClient connect(String host, int port) {
        log.info("HelloGrpcClient#connect(host, port) called");
        return connect(host, port, 20);
    }

    public static HelloGrpcClient connect(String host, int port, long timeoutSeconds) {
        log.info("HelloGrpcClient#connect(host, port, timeoutSeconds) called");
        Objects.requireNonNull(host, "host must not be null");
        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than 0");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than 0");
        }

        ManagedChannel channel = createChannel(host, port);
        return new HelloGrpcClient(channel, timeoutSeconds);
    }

    public static HelloGrpcClient connect(ManagedChannel channel, long timeoutSeconds) {
        log.info("HelloGrpcClient#connect(channel, timeoutSeconds) called");
        Objects.requireNonNull(channel, "channel must not be null");
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than 0");
        }
        return new HelloGrpcClient(channel, timeoutSeconds);
    }

    private static ManagedChannel createChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
    }

    private ConsoleS3PresignServiceGrpc.ConsoleS3PresignServiceBlockingStub stubWithDeadline() {
        return baseStub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS);
    }

    public HelloResponse hello(HelloRequest request) {
        log.info("HelloGrpcClient#hello called");
        return stubWithDeadline().hello(request);
    }

    @Override
    public void close() {
        log.info("HelloGrpcClient#close called");
        channel.shutdown();
    }
}
