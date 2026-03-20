package com.example.httpclient;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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

    private static ReentrantLock LOCK_TERM = new ReentrantLock();
    private static Condition COND_TERM = LOCK_TERM.newCondition();

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
                        LOCK_TERM.lock();
                        try {
                            COND_TERM.signalAll();
                        } finally {
                            LOCK_TERM.unlock();
                        }
                    }
                }
            }).web(WebApplicationType.NONE) // Do NOT run as server
            .bannerMode(Banner.Mode.OFF)    // Do NOT display Spring Banner on LOG when app is started
            .run(args);

        LOCK_TERM.lock();
        try {
            COND_TERM.await();
        } finally {
            LOCK_TERM.unlock();
        }
    }
}
