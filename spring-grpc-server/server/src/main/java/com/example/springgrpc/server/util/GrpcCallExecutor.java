package com.example.springgrpc.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * gRPC 어댑터의 응답 전송과 예외 → {@link Status} 매핑을 한곳에 모은 유틸.
 * <p>
 * {@code StreamObserver}는 {@code onNext} 후 반드시 {@code onCompleted}를 불러야 하고
 * 예외는 절대 밖으로 던지면 안 된다(던지면 호출자가 응답 없이 매달린다). 어댑터마다 이 규약을
 * 되풀이하는 대신 여기서 처리한다.
 */
public final class GrpcCallExecutor {

    private static final Logger log = LoggerFactory.getLogger(GrpcCallExecutor.class);

    private GrpcCallExecutor() {
    }

    /**
     * 공급자를 실행해 결과를 응답으로 보낸다. {@link IllegalArgumentException}은 클라이언트 잘못으로
     * 보고 {@code INVALID_ARGUMENT}로, 나머지는 {@code INTERNAL}로 매핑한다.
     *
     * @param context 호출 지점 식별자. 로거 이름이 이 유틸로 고정되므로 어댑터와 메서드를 함께 넘긴다
     */
    public static <T> void execute(String context, StreamObserver<T> observer, SupplierWithException<T> supplier) {
        log.info("{} executing", context);
        try {
            observer.onNext(supplier.get());
            observer.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("{} rejected: {}", context, e.getMessage());
            observer.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        } catch (Exception e) {
            log.error("{} failed", context, e);
            observer.onError(Status.INTERNAL.withDescription(e.getMessage()).withCause(e).asRuntimeException());
        }
    }

    @FunctionalInterface
    public interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
