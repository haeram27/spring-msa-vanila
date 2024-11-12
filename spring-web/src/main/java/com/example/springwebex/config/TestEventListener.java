package com.example.springwebex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TestEventListener {
    private final ApplicationContext applicationContext;

    @Autowired
    public TestEventListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug(applicationContext.getApplicationName()+":Event: ApplicationReadyEvent");
/*
                while (true) {
                    try {
                        var now = OffsetDateTime.ofInstant(Instant.now(),
                            ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        log.debug("test_event_log: "+now);
                        Thread.sleep(300);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        log.error("## Error: ", e);
                    }
                }
*/
            }
        }).start();
    }
}