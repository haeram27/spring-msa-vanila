package com.example.springgrpc.server.controller;

import com.example.springgrpc.server.controller.dto.HelloRequestBody;
import com.example.springgrpc.server.service.HelloService;
import com.example.springgrpc.server.service.dto.HelloResult;
import com.example.springgrpc.server.util.HttpRequestLogger;
import com.example.springgrpc.server.util.SensitiveHeaders;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * REST 어댑터: 같은 프로세스의 {@link HelloService}를 직접 호출한다.
 * gRPC 어댑터({@code HelloGrpcServerService})와 동일한 서비스를 공유하므로
 * 직렬화나 네트워크 왕복 없이 로직을 재사용한다.
 *
 * <pre>
 * curl -X POST 'localhost:8080/api/hello' -H 'Content-Type: application/json' \
 *   -H 'x-tenant-id: T0001' -d '{"name":"hello"}'
 * </pre>
 */
@RestController
@RequestMapping("/api/hello")
public class HelloHttpController {
    private final HelloService helloService;

    public HelloHttpController(HelloService helloService) {
        this.helloService = helloService;
    }

    @PostMapping
    public Map<String, String> hello(@RequestBody HelloRequestBody body,
                                     @RequestHeader Map<String, String> headers,
                                     HttpServletRequest request) {
        HttpRequestLogger.logRequestMembers("HelloHttpController#hello", request);

        if (body == null || body.name() == null || body.name().isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        // gRPC 경로는 HeaderLoggingInterceptor가 마스킹한 헤더를 서비스에 넘긴다.
        // 이 경로도 같은 기준을 적용해야 두 경로의 응답이 일치한다.
        HelloResult result = helloService.hello(body.name(), SensitiveHeaders.maskAll(headers));
        return Map.of(
            "message", result.message(),
            "headers", result.headers()
        );
    }
}
