package com.example.springgrpc.server.controller.dto;

/**
 * {@code /api/hello} 계열의 요청 본문.
 * <p>
 * 값을 URL이 아니라 본문으로 받는다. 쿼리스트링은 액세스 로그·프록시·Referer에 그대로 남는다.
 */
public record HelloRequestBody(String name) {
}
