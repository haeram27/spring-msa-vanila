package com.example.springwebex.controller

import java.util.concurrent.atomic.AtomicLong
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/get")
class GetController {
    private val template = "Hello, %s!"
    private val counter = AtomicLong()

    // curl -i http://localhost:8181/api/get/greeting"
    // curl -i http://localhost:8181/api/get/greeting?name=Json"
    @GetMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): Greeting {
        return Greeting(counter.incrementAndGet(), String.format(template, name))
    }

    // curl -i "http://localhost:8181/get/string?name=Jason"
    @GetMapping("/string")
    fun string(@RequestParam(value = "name", defaultValue = "World") name: String): String {
        return String.format(template, name)
    }
}
