package com.example.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ServiceApplication {

    private static AtomicInteger ATOM_INT = new AtomicInteger(0);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent e) {
        // applicaiton started
        log.info("@@@ ApplicationReadyEvent");
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        log.info("message ID:" + String.valueOf(ATOM_INT.incrementAndGet()));
                        Thread.sleep(200);
                    }
                } catch (Exception e) {
                    log.error("Exception: ", e);
                }
            }
        }.start();
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent e) {
        // applicaiton terminated
        log.info("@@@ ContextClosedEvent");

        // release main thread
        shutdownLatch.countDown();
    }

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(ServiceApplication.class).run(args);

        // hold main thread
        shutdownLatch.await();
    }
}
