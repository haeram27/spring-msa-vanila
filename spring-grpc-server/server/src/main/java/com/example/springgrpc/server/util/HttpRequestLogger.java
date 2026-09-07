package com.example.springgrpc.server.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST 어댑터가 수신한 HTTP 요청을 로깅하는 공용 유틸.
 * <p>
 * gRPC 쪽 {@link GrpcRequestLogger}가 proto 메시지 하나만 찍으면 되는 것과 달리, HTTP 요청은
 * 메서드/경로/쿼리/헤더가 서로 다른 곳에 흩어져 있으므로 {@link HttpServletRequest}에서 모아 남긴다.
 * <p>
 * 자격 증명 계열 헤더는 {@link SensitiveHeaders} 기준으로 값을 가린다.
 * <p>
 * 본문(body)은 찍지 않는다. {@code getInputStream()}은 한 번만 읽을 수 있어 여기서 소비하면
 * 이후 메시지 컨버터가 역직렬화에 실패한다. 본문까지 남기려면 요청을 감싸 캐싱하는
 * {@code ContentCachingRequestWrapper} + 필터가 별도로 필요하다.
 */
public final class HttpRequestLogger {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLogger.class);

    private HttpRequestLogger() {
    }

    /**
     * 요청의 메서드/경로/쿼리스트링/헤더를 로그로 남긴다.
     *
     * @param context 호출 지점 식별자. 로거 이름이 이 유틸로 고정되므로
     *                {@code "HelloHttpController#hello"}처럼 컨트롤러와 메서드를 함께 넘긴다.
     * @param request 로깅할 HTTP 요청
     */
    public static void logRequestMembers(String context, HttpServletRequest request) {
        if (request == null) {
            log.info("{} called with request=<none>", context);
            return;
        }

        // extractHeaders(...)는 로그 전용이다. 운영 프로파일에서 이 로거를 낮추면
        // 헤더 순회 자체가 일어나지 않도록 레벨을 먼저 확인한다.
        if (!log.isInfoEnabled()) {
            return;
        }

        String query = request.getQueryString();
        log.info("{} called with request={} {}{}, headers={}",
            context,
            request.getMethod(),
            request.getRequestURI(),
            query == null ? "" : "?" + query,
            extractHeaders(request));
    }

    /**
     * 헤더를 소문자 키로 모은다. 같은 이름이 여러 번 온 헤더({@code Accept} 등)는 값을 이어 붙인다.
     */
    private static Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> extracted = new LinkedHashMap<>();
        for (String name : Collections.list(request.getHeaderNames())) {
            String key = name.toLowerCase(Locale.ROOT);
            if (SensitiveHeaders.isSensitive(key)) {
                extracted.put(key, SensitiveHeaders.MASK);
                continue;
            }
            List<String> values = Collections.list(request.getHeaders(name));
            extracted.put(key, String.join(", ", values));
        }
        return extracted;
    }
}
