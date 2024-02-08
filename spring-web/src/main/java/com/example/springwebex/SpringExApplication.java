package com.example.springwebex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class SpringExApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringExApplication.class, args);
        log.info("***** main() *****");
    }
}
