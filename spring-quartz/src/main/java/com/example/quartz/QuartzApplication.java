package com.example.quartz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QuartzApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzApplication.class, args);

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // System.out.println("heart beat");
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                }
            }
        }.start();
    }
}
