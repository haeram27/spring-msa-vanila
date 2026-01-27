package com.example.httpclient;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class SpringLifecycleTestApplication {

    @EventListener
    public void onMainApplicationEvent(ApplicationEvent event) {
        // @EventListener registered after this class bean is created so init application events can NOT be received
        log.debug(": onMainApplicationEvent: " + event.getClass().getSimpleName());
        /*
            if (event instanceof ApplicationStartedEvent) {
                log.debug("Application Started");
            } else if (event instanceof ApplicationReadyEvent) {
                log.debug("Application Ready");
            } else if (event instanceof ApplicationFailedEvent) {
                log.debug("Application Failed");
            } else if (event instanceof ContextRefreshedEvent) {
                log.debug("Context Refreshed");
            } else if (event instanceof ContextClosedEvent) {
                log.debug("Context Refreshed");
                // implementations under HERE before application closed
            }
        */
    }

    public static void main(String[] args) throws Exception {
        log.debug("*** main() ***");

        // add event listener before SpringApplication.run() to get initializing app events previous ApplicationStartedEvent
        new SpringApplicationBuilder(SpringLifecycleTestApplication.class)
        .listeners(new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(@SuppressWarnings("null") ApplicationEvent event) {
                log.debug("InitializingApplicationEvent: " + event.getClass().getSimpleName());
                /*
                    if (event instanceof ApplicationContextInitializedEvent) {
                        log.debug("Context Initialized");
                    } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
                        log.debug("Environment Prepared");
                    } else if (event instanceof ApplicationPreparedEvent) {
                        log.debug("Application Prepared");
                    } else if (event instanceof ApplicationStartedEvent) {
                        log.debug("Application Started");
                    } else if (event instanceof ApplicationReadyEvent) {
                        log.debug("Application Ready");
                    } else if (event instanceof ApplicationFailedEvent) {
                        log.debug("Application Failed");
                    } else if (event instanceof ContextRefreshedEvent) {
                        log.debug("Context Refreshed");
                    } else if (event instanceof ContextClosedEvent) {
                        log.debug("Context Refreshed");
                        // implementations under HERE before application closed
                    }
                */
            }
        }).web(WebApplicationType.NONE) // Do NOT run as server
        .bannerMode(Banner.Mode.OFF)    // Do NOT display Spring Banner on LOG when app is started
        .run(args);
    }
}
