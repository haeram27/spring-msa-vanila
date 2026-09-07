package com.example.springgrpc.server.service;

import org.springframework.stereotype.Service;

import com.example.springgrpc.server.service.dto.HelloResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * Grpc 통신 테스트용 서비스
 * gRPC 어댑터({@code HelloGrpcServerService})는 파라미터 유효성 검증과 proto 매핑만 담당하고,
 * 실제 로직은 이 클래스에서 처리한다.
 */
@Service
public class HelloService {
    private static final Logger log = LoggerFactory.getLogger(HelloService.class);

    public HelloResult hello(String message, Map<String, String> headerMap) {
        log.info("HelloService#hello called");
        String headerString = headerMap.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
        return new HelloResult(message, headerString);
    }
}
