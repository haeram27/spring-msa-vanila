package com.example.springgrpc.app;

import com.example.springgrpc.client.HelloGrpcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/greetings")
public class GreetingController {
    private final HelloGrpcClient helloGrpcClient;

    public GreetingController(HelloGrpcClient helloGrpcClient) {
        this.helloGrpcClient = helloGrpcClient;
    }

    @GetMapping
    public Map<String, String> greet(@RequestParam String name) {
        return Map.of("message", helloGrpcClient.sayHello(name).getMessage());
    }
}
