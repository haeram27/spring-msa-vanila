package com.example.empty.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TestEventListener {
    private final ApplicationContext applicationContext;

    @Autowired
    public TestEventListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void logPostConstruct() {
        log.debug("PostConstruct called");
    }

    @PreDestroy
    public void logPreDestroy() {
        log.debug("PreDestroy called");
    }

    @EventListener
    public void onApplicationEvent(ApplicationEvent event) {

        log.debug(applicationContext.getApplicationName()+": onApplicationEvent: " + event.getClass().getSimpleName());
/*
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
}
