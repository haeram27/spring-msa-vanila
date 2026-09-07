package com.example.springgrpc.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageOrBuilder;

/**
 * gRPC 어댑터가 수신한 proto 요청을 로깅하는 공용 유틸.
 * <p>
 * protobuf 메시지의 {@code toString()}은 필드명과 값을 함께 출력하므로 별도 매핑 없이
 * 요청 전체를 남길 수 있다. 다만 자동 개행이 들어가고 unset 필드는 생략된다는 점에 유의한다.
 */
public final class GrpcRequestLogger {

    private static final Logger log = LoggerFactory.getLogger(GrpcRequestLogger.class);

    private GrpcRequestLogger() {
    }

    /**
     * 요청 메시지의 필드를 로그로 남긴다.
     *
     * @param context 호출 지점 식별자. 로거 이름이 이 유틸로 고정되므로
     *                {@code "HelloGrpcServerService#hello"}처럼 어댑터와 메서드를 함께 넘긴다.
     * @param message 로깅할 proto 요청 메시지
     */
    public static void logRequestMembers(String context, MessageOrBuilder message) {
        log.info("{} called with request={}", context, message);
    }
}
