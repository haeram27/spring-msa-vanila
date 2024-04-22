package com.example.kafka.consumer.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    // @Value(value = "${spring.kafka.consumer.group-id}")
    // private String groupId;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        log.info(String.valueOf(System.nanoTime()));
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        // configs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 32768);
        // configs.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // configs.put(ProducerConfig.ACKS_CONFIG, "all");
        // configs.put(ProducerConfig.RETRIES_CONFIG, 0);
        // configs.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 20 * 1000);
        // configs.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 60 * 1000);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConcurrency(3);
        factory.setConsumerFactory(consumerFactory());
        // factory.setBatchListener(true);
        // factory.setAutoStartup(true);
        return factory;
    }

}
