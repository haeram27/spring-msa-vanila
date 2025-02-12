package com.example.empty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;


import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class SpringEmptyApplication {

    @EventListener
    public void onMainApplicationEvent(ApplicationEvent event) {
/*
        log.debug(": onMainApplicationEvent: " + event.getClass().getSimpleName());

        if (event instanceof ApplicationContextInitializedEvent) {
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
        } else if (event instanceof ApplicationPreparedEvent) {
        } else if (event instanceof ApplicationStartedEvent) {
        } else if (event instanceof ApplicationReadyEvent) {
        } else if (event instanceof ApplicationFailedEvent) {
        } else if (event instanceof ContextRefreshedEvent) {
        } else if (event instanceof ContextClosedEvent) {
            // implementations under HERE before applcation closed
        }
*/
    }

    public static void main(String[] args) throws Exception {
        log.debug("*** main() ***");
        new SpringApplication(SpringEmptyApplication.class).run(args);
    }
}
