package com.example.springgrpc.server.service.dto;

/**
 * {@code HelloService#hello}의 처리 결과를 전달하는 Service 계층의 출력 DTO.
 * proto/gRPC 타입에 의존하지 않아 서비스 계층을 gRPC와 독립적으로 테스트할 수 있다.
 */
public record HelloResult(String message, String headers) {
}
