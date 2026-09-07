package com.example.springgrpc.server.grpc.client;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springgrpc.api.HelloResponse;
import com.example.springgrpc.client.HelloGrpcClient;
import com.example.springgrpc.server.controller.dto.HelloRequestBody;
import com.example.springgrpc.server.util.HttpRequestLogger;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST 요청을 gRPC 호출로 중계하는 어댑터.
 * HTTP 요청 헤더를 gRPC Metadata로 변환해 전달하므로,
 * 서버 측 {@code HeaderLoggingInterceptor}가 그대로 헤더를 받아볼 수 있다.
 * <p>
 * {@code HelloHttpController}가 서비스를 직접 호출하는 방식과 비교하기 위한 경로다.
 *
 * <pre>
 * curl -X POST 'localhost:8080/api/grpc/client/hello' -H 'Content-Type: application/json' \
 *   -H 'x-tenant-id: T0001' -d '{"name":"hello"}'
 * </pre>
 */
@RestController
@RequestMapping("/api/grpc/client/hello")
public class HelloGrpcClientController {
    private final HelloGrpcClient helloGrpcClient;

    public HelloGrpcClientController(HelloGrpcClient helloGrpcClient) {
        this.helloGrpcClient = helloGrpcClient;
    }

    @PostMapping
    public Map<String, String> hello(@RequestBody HelloRequestBody body,
                                     @RequestHeader Map<String, String> headers,
                                     HttpServletRequest request) {
        HttpRequestLogger.logRequestMembers("HelloGrpcClientController#hello", request);

        String name = body == null ? null : body.name();
        HelloResponse response = helloGrpcClient.hello(name, ForwardableHeaders.from(headers));
        return Map.of(
            "message", response.getMessage(),
            "headers", response.getHeaders()
        );
    }
}
