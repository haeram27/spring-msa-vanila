package com.hello.grpc.server;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class SpringApplication {

    public static void main(String[] args) {
        log.trace("*** main() ***");

        // add event listener before SpringApplication.run() to get initializing app events previous ApplicationStartedEvent
        new SpringApplicationBuilder(SpringApplication.class)
            .listeners((ApplicationEvent event) -> log.info(":: springboot :: event :: " + event.toString()))
            .bannerMode(Banner.Mode.OFF)
            .run(args);
    }
}
