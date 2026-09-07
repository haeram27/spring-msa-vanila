package com.example.quartz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class QuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzApplication.class, args);

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(2000);
                        // log.trace("heart beat");
                    }
                } catch (Exception e) {
                    log.error("Exception: " + e);
                }
            }
        }.start();
    }
}
