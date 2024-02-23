package com.example.springwebex.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class DefaultCommandLineRunner implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        for (var arg : args) {
            log.info("arg: {}", arg);
        }
    }
}
