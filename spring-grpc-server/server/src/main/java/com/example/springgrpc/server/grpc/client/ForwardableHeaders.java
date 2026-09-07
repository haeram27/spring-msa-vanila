package com.example.springgrpc.server.grpc.client;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * HTTP 요청 헤더 중 gRPC 호출로 전달해도 되는 것만 골라낸다.
 * <p>
 * HTTP 헤더를 그대로 Metadata에 실으면 gRPC 전송 계층이 직접 관리하는 헤더
 * ({@code content-type: application/grpc}, {@code te: trailers} 등)를 덮어써서
 * 호출이 깨질 수 있으므로, 전송 계층 헤더는 제외하고 애플리케이션 헤더만 넘긴다.
 * <p>
 * 자격 증명({@code authorization})도 제외한다. 하위 호출로 넘기면 수신 측
 * {@code HeaderLoggingInterceptor}가 Metadata 전체를 찍으면서 토큰이 로그에 평문으로 남는다.
 */
public final class ForwardableHeaders {

    /**
     * 전달하지 않는 헤더. 전송 계층이 소유해 덮어쓰면 호출이 깨지는 것과,
     * 넘기면 안 되는 자격 증명이 함께 들어 있다.
     */
    private static final Set<String> EXCLUDED = Set.of(
        // gRPC 전송 계층이 직접 설정한다
        "content-type", "te", "user-agent", "accept-encoding", "grpc-accept-encoding",
        // HTTP/1.1 hop-by-hop 헤더 — 커넥션 단위라 전달 대상이 아니다
        "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
        "trailer", "transfer-encoding", "upgrade",
        // 요청 본문/라우팅 메타데이터 — 새 호출에는 유효하지 않다
        "host", "content-length", "accept",
        // 자격 증명 — 전달하면 하위 호출과 서버 로그로 그대로 흘러간다
        "authorization"
    );

    /** gRPC가 예약한 접두사. 클라이언트가 직접 설정하면 안 된다. */
    private static final String GRPC_RESERVED_PREFIX = "grpc-";

    private ForwardableHeaders() {
    }

    public static Map<String, String> from(Map<String, String> httpHeaders) {
        Map<String, String> forwardable = new LinkedHashMap<>();
        if (httpHeaders == null) {
            return forwardable;
        }

        httpHeaders.forEach((name, value) -> {
            if (name == null || value == null) {
                return;
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (EXCLUDED.contains(key) || key.startsWith(GRPC_RESERVED_PREFIX) || key.startsWith(":")) {
                return;
            }
            forwardable.put(key, value);
        });
        return forwardable;
    }
}
