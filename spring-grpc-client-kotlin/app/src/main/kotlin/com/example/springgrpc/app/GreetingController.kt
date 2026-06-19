package com.example.springgrpc.app

import com.example.springgrpc.client.HelloGrpcClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/greetings")
class GreetingController(
    private val helloGrpcClient: HelloGrpcClient,
) {
    @GetMapping
    fun greet(@RequestParam name: String): Map<String, String> =
        mapOf("message" to helloGrpcClient.sayHello(name).message)
}
