package com.example.quartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

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
                        // System.out.println("heart beat");
                        log.info("heart beat");
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                }
            }
        }.start();
    }
}
