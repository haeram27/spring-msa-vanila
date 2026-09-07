package com.example.springgrpc.server.grpc.interceptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.stereotype.Component;

import com.example.springgrpc.server.util.SensitiveHeaders;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/**
 * 수신 gRPC 요청의 Metadata를 로깅하고, 텍스트 헤더를 {@link GrpcRequestHeaderContext}에 실어
 * 서비스 계층에서 꺼내 쓸 수 있게 한다.
 * <p>
 * Metadata를 {@code toString()}으로 통째로 찍으면 외부 클라이언트가 보낸 {@code authorization} 값이
 * 평문으로 남는다. {@code ForwardableHeaders}는 이 서버가 <em>내보내는</em> 호출만 거르므로
 * 들어오는 요청에는 효과가 없다. 그래서 여기서 {@link SensitiveHeaders} 기준으로 직접 가린다.
 */
@Component
@GlobalServerInterceptor
public class HeaderLoggingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HeaderLoggingInterceptor.class);

    /** 바이너리 헤더는 값을 남기지 않고 존재만 표시한다. */
    private static final String BINARY_PLACEHOLDER = "<binary>";

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        Map<String, String> textHeaders = extractTextHeaders(headers);

        // describe(...)는 로그 전용이다. 운영 프로파일은 이 로거를 WARN 으로 낮추므로
        // 레벨을 먼저 확인해 맵 조립 자체를 건너뛴다.
        if (log.isInfoEnabled()) {
            log.info("[ServerInterceptor] Method: {} | Headers: {}",
                call.getMethodDescriptor().getFullMethodName(), describe(headers, textHeaders));
        }

        Context contextWithHeaders = GrpcRequestHeaderContext.withHeaders(Context.current(), textHeaders);
        return Contexts.interceptCall(contextWithHeaders, call, headers, next);
    }

    /**
     * 텍스트 헤더만 골라 마스킹해 담는다. 이 맵은 로그뿐 아니라 Context를 거쳐 서비스 계층으로
     * 흘러가 응답 본문에까지 실리므로, 여기서 가려야 응답으로 새는 경로도 함께 막힌다.
     */
    private static Map<String, String> extractTextHeaders(Metadata headers) {
        Map<String, String> extracted = new LinkedHashMap<>();
        for (String key : new TreeSet<>(headers.keys())) {
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                continue;
            }
            String value = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER));
            if (value != null) {
                extracted.put(key, SensitiveHeaders.maskIfSensitive(key, value));
            }
        }
        return extracted;
    }

    /** 로그용 표현. 마스킹된 텍스트 헤더에 바이너리 헤더의 존재를 덧붙인다. */
    private static Map<String, String> describe(Metadata headers, Map<String, String> textHeaders) {
        Map<String, String> described = new LinkedHashMap<>(textHeaders);
        for (String key : new TreeSet<>(headers.keys())) {
            if (key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                described.put(key, BINARY_PLACEHOLDER);
            }
        }
        return described;
    }
}
