package com.hello.grpc.server.service.v1;

import org.springframework.grpc.server.service.GrpcService;

import com.hello.grpc.server.interceptor.GrpcContextKeys;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class HelloGrpcService extends ConsoleS3PresignServiceGrpc.ConsoleS3PresignServiceImplBase {

/* Header key is always lowercase in gRPC Metadata, so use lowercase for the header key when testing with grpcurl or other gRPC clients.
grpcurl -plaintext \
-H 'x-tenant-id: tenant' \
-d '{"message":"hello"}' \
localhost:51003 \
console.s3.v1.ConsoleS3PresignService/Hello
*/
    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        log.info("HelloGrpc#hello called");

        // 인터셉터에서 바인딩한 Context 값을 현재 스레드에서 추출
        String currentUserId = GrpcContextKeys.USER_ID_KEY.get();
        log.info("HelloGrpc#hello - currentUserId from Context: {}", currentUserId);

        validateNotBlank(request.getMessage(), "message");

        execute(responseObserver, () -> {
            HelloResponse response = HelloResponse.newBuilder()
                .setMessage(request.getMessage())
                .setHeaders(currentUserId != null ? "Hello, " + currentUserId : "Hello, Anonymous")
                .build();
            return response;
        });
    }

    private static void validateNotBlank(String value, String fieldName) {
        log.info("HelloGrpc#validateNotBlank called");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private <T> void execute(StreamObserver<T> observer, SupplierWithException<T> supplier) {
        log.info("HelloGrpc#execute called");
        try {
            observer.onNext(supplier.get());
            observer.onCompleted();
        } catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            observer.onError(Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
