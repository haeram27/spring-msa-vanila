package com.example.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class ServiceApplication {

    private static ReentrantLock LOCK_TERM = new ReentrantLock();
    private static Condition COND_TERM = LOCK_TERM.newCondition();
    private static AtomicInteger ATOM_INT = new AtomicInteger(0);

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

        // wakeup main thread
        LOCK_TERM.lock();
        try {
            COND_TERM.signalAll();
        } finally {
            LOCK_TERM.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(ServiceApplication.class).run(args);

        // wait on main thread
        LOCK_TERM.lock();
        try {
            COND_TERM.await();
        } finally {
            LOCK_TERM.unlock();
        }
    }
}
