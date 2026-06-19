package com.hello.grpc.server.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

@Component
@GlobalServerInterceptor
public class HeaderLoggingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderLoggingInterceptor.class);

    private static final String USER_ID_HEADER_NAME = "x-user-id";

    // Context에 바인딩할 키 정의
    public static final Context.Key<String> USER_ID_KEY = GrpcContextKeys.USER_ID_KEY;

    // gRPC 메타데이터 키 정의 (헤더명은 소문자와 하이픈만 가능, '-bin' 접미사가 없으면 String)
    private static final Metadata.Key<String> USER_ID_HEADER = Metadata.Key.of(USER_ID_HEADER_NAME, Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // log whole headers for debugging
        log.info("[ServerInterceptor] Method: {} | Headers: {}", call.getMethodDescriptor().getFullMethodName(), headers);

        if (headers != null) {
            for (String key : headers.keys()) {
                if (key.equalsIgnoreCase(USER_ID_HEADER_NAME)) {
                     // 1. 헤더에서 값 추출
                    String userId = headers.get(USER_ID_HEADER);

                    // 헤더가 없을 경우 예외를 던지거나 기본값을 세팅할 수 있습니다.
                    if (userId == null) {
                        userId = "ANONYMOUS";
                    }

                    // 2. 현재 컨텍스트에 새 키-값 쌍을 연결하여 새로운 Context 생성
                    Context context = Context.current().withValue(USER_ID_KEY, userId);

                    // 3. Contexts.interceptCall을 통해 생성한 컨텍스트를 하위 요청 스레드에 바인딩
                    return Contexts.interceptCall(context, call, headers, next);
                }
            }
        }

        return next.startCall(call, headers);
    }
}