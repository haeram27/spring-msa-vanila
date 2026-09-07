package com.example.springgrpc.server.grpc.server;

import org.springframework.grpc.server.service.GrpcService;

import com.example.springgrpc.api.HelloGrpc;
import com.example.springgrpc.api.HelloRequest;
import com.example.springgrpc.api.HelloResponse;
import com.example.springgrpc.server.grpc.interceptor.GrpcRequestHeaderContext;
import com.example.springgrpc.server.service.HelloService;
import com.example.springgrpc.server.service.dto.HelloResult;
import com.example.springgrpc.server.util.GrpcCallExecutor;
import com.example.springgrpc.server.util.GrpcRequestLogger;
import com.example.springgrpc.server.util.GrpcRequestValidator;

import io.grpc.stub.StreamObserver;

@GrpcService
public class HelloGrpcServerService extends HelloGrpc.HelloImplBase {

    private final HelloService helloService;

    public HelloGrpcServerService(HelloService helloService) {
        this.helloService = helloService;
    }

/* Header key is always lowercase in gRPC Metadata, so use lowercase for the header key when testing with grpcurl or other gRPC clients.
grpcurl -plaintext \
-H 'x-tenant-id: T0001' \
-d '{"message":"hello"}' \
localhost:9090 \
com.example.grpc.v1.Hello/Hello
*/
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        GrpcRequestLogger.logRequestMembers("HelloGrpcServerService#hello", request);
        GrpcCallExecutor.execute("HelloGrpcServerService#hello", responseObserver, () -> {
            GrpcRequestValidator.validateNotBlank(request.getMessage(), "message");
            HelloResult result = helloService.hello(request.getMessage(), GrpcRequestHeaderContext.currentHeaders());
            return HelloResponse.newBuilder()
                .setMessage(result.message())
                .setHeaders(result.headers())
                .build();
        });
    }
}
