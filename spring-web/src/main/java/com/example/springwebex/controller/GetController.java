package com.example.springwebex.controller;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/get")
public class GetController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    // curl -i http://localhost:8181/api/get/greeting"
    // curl -i http://localhost:8181/api/get/greeting?name=Json"
    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    // curl -i "http://localhost:8181/get/string?name=Jason"
    @GetMapping("/string")
    public String string(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format(template, name);
    }
}
