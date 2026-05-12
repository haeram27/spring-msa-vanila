package com.example.cephclient;

import java.util.concurrent.CountDownLatch;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class SpringApplication {

    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        log.trace("*** main() ***");

        // add event listener before SpringApplication.run() to get initializing app events previous ApplicationStartedEvent
        new SpringApplicationBuilder(SpringApplication.class)
            .listeners(new ApplicationListener<ApplicationEvent>() {
                @Override
                public void onApplicationEvent(@SuppressWarnings("null") ApplicationEvent event) {
                    log.info(":: springboot :: event :: " + event.toString());

                    if (event instanceof ContextClosedEvent) {
                        // implementations under HERE before applcation closed

                        // release main thread
                        shutdownLatch.countDown();
                    }
                }
            }).web(WebApplicationType.NONE) // Do NOT run as server
            .bannerMode(Banner.Mode.OFF)    // Do NOT display Spring Banner on LOG when app is started
            .run(args);

        // hold main thread
        shutdownLatch.await();
    }
}
