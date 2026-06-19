package com.example.springgrpc.server;

import com.example.springgrpc.client.HelloGrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/greetings")
public class GreetingHttpController {
    private final HelloGrpcClient helloGrpcClient;

    public GreetingHttpController(HelloGrpcClient helloGrpcClient) {
        this.helloGrpcClient = helloGrpcClient;
    }

    @GetMapping
    public Map<String, String> greet(@RequestParam String name) {
        return Map.of("message", helloGrpcClient.sayHello(name).getMessage());
    }
}
