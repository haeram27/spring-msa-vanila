package com.example.kafka.consumer.config;

// Importing required classes
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaConsumerListenerConfig {

    // @KafkaListener(topics = "haeram27", groupId = "group_id")
    // public void consume(String msg) {
    // log.info("message = " + msg);
    // }


    @KafkaListener(topics = "haeram27", groupId = "group_id")
    public void consume(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) String partition,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) String ts, @Payload String msg) {
        log.info(String.format("key:%s, partition:%s, topic:%s, ts:%s, message:%s", key, partition,
                topic, ts, msg));
    }
}
