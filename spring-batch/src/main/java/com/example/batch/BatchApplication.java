package com.example.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);

        //-- respirator for service
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // System.out.println("heart beat");
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                }
            }
        }.start();
    }
}
