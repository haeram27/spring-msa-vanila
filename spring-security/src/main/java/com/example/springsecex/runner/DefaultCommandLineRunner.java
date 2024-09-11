package com.example.springsecex.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class DefaultCommandLineRunner implements CommandLineRunner {
    /*
     * $ gradle bootRun --args='--opt-arg1=value1 --opt-arg2=value2 --non-opt-arg1 0123 non-opt-arg2 -s'
     * $ java -jar executable.jar --opt-arg1=value1 --opt-arg2=value2 --non-opt-arg1 0123 non-opt-arg2 -s
     * $ java -cp non-executable.jar <main-class-name> --opt-arg1=value1 --opt-arg2=value2 --non-opt-arg1 0123 non-opt-arg2 -s
     */
    @Override
    public void run(String... args) throws Exception {
        for (var arg : args) {
            log.info("arg: {}", arg);
        }
    }
}
