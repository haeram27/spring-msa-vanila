package com.example.springgrpc.server.util;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 로그나 응답에 값을 노출하면 안 되는 헤더를 판정한다.
 * <p>
 * HTTP 입구({@code HttpRequestLogger})와 gRPC 입구({@code HeaderLoggingInterceptor})가 같은 기준을
 * 써야 한쪽만 가려지는 구멍이 생기지 않으므로 목록을 여기 한곳에 둔다.
 * <p>
 * 전달 차단 목록({@code ForwardableHeaders})과는 목적이 다르다. 이쪽은 "값을 보여주지 않는다",
 * 저쪽은 "하위 호출로 넘기지 않는다"이며, 넘기지 않는 헤더라도 수신 시점에는 로그에 남을 수 있다.
 */
public final class SensitiveHeaders {

    private static final Set<String> MASKED = Set.of(
        "authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key"
    );

    /** 값을 가릴 때 대신 남기는 문자열. 헤더의 존재 여부는 그대로 드러난다. */
    public static final String MASK = "***";

    private SensitiveHeaders() {
    }

    public static boolean isSensitive(String name) {
        return name != null && MASKED.contains(name.toLowerCase(Locale.ROOT));
    }

    /** 자격 증명 계열이면 마스크를, 아니면 원래 값을 돌려준다. */
    public static String maskIfSensitive(String name, String value) {
        return isSensitive(name) ? MASK : value;
    }

    /**
     * 맵 전체에 마스킹을 적용한 사본을 만든다. 키 순서는 유지한다.
     * <p>
     * 서비스 계층으로 넘기기 전에 씌운다. 서비스가 헤더를 응답에 되싣는 경우가 있어,
     * 로그만 가려서는 응답 본문으로 새는 경로가 남는다.
     */
    public static Map<String, String> maskAll(Map<String, String> headers) {
        Map<String, String> masked = new LinkedHashMap<>();
        if (headers == null) {
            return masked;
        }
        headers.forEach((name, value) -> masked.put(name, maskIfSensitive(name, value)));
        return masked;
    }
}
