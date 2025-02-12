package com.example.myservice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@Slf4j
public class SpringMyServiceApplication {
    private static ReentrantLock LOCK_TERM = new ReentrantLock();
    private static Condition COND_TERM = LOCK_TERM.newCondition();

    public static void main(String[] args) throws Exception {
        log.debug("*** main() ***");
        new SpringApplicationBuilder(SpringMyServiceApplication.class)
                .listeners(new ApplicationListener<ApplicationEvent>() {
                    @SuppressWarnings("null")
                    @Override
                    public void onApplicationEvent(ApplicationEvent event) {
                        log.info(":: springboot :: event :: " + event.toString());

                        if (event instanceof ApplicationContextInitializedEvent) {
                        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
                        } else if (event instanceof ApplicationPreparedEvent) {
                        } else if (event instanceof ApplicationStartedEvent) {
                        } else if (event instanceof ApplicationReadyEvent) {
                            // implementations under HERE after springboot initialization
                            log.debug("spring context is ready. all beans are created.");

                        } else if (event instanceof ApplicationFailedEvent) {
                        } else if (event instanceof ContextRefreshedEvent) {
                        } else if (event instanceof ContextClosedEvent) {
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
                }).run(args);

        // prevent main thread termination
        LOCK_TERM.lock();
        try {
            COND_TERM.await();
        } finally {
            LOCK_TERM.unlock();
        }
    }
}
