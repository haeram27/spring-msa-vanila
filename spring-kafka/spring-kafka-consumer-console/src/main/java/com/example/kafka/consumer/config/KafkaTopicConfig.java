package com.example.kafka.consumer.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    /*
     * KafkaAdmin은 Kafka 토픽 관리를 위한 Spring Kafka의 AdminClient 기반 관리용 Bean이다.
     * 애플리케이션 시작 시 NewTopic Bean을 기준으로 토픽 생성/검증/수정을 수행한다.
     * 토필 관리(생성/검증/수정)를 하지 않는다면 KafkaAdmin은 필수가 아니며 optional이다.
     * (토픽을 사전 생성해 두고 관리 기능을 사용하지 않는 경우)

     *   **KafkaAdmin을 쓰는 목적**
     *   1. 애플리케이션 시작 시 토픽을 코드로 관리하려는 경우
     *   2. NewTopic 기준으로 토픽 생성/검증/파티션 증가/설정 변경을 자동화하려는 경우

     *   **KafkaAdmin이 필요한 경우**
     *   1. 토픽을 앱이 직접 생성/수정/삭제 관리해야 할 때, 단순 생성만이라면 auto.create.topics.enable=true인 경우 KafkaAdmin 없이도 기본 스펙으로 토픽 자동 생성 가능
     *   2. 브로커에서 auto.create.topics.enable=false 이고, 토픽을 앱이 만들 책임이 있을 때
     *   3. 환경별로 토픽 상세 스펙(파티션, 복제계수, 설정값)을 코드로 일관 관리하고 싶을 때 (auto.create.topics.enable=true 이어도 KafkaAdmin이 필요)

     *   **KafkaAdmin이 없어도 되는 경우**
     *   1. 앱이 단순 consume/subscribe/produce만 수행할 때
     *   2. 토픽을 운영 파이프라인/스크립트/인프라(IaC)에서 사전 생성·관리할 때
     *   3. 앱에서 토픽 관리 권한을 의도적으로 제거한 운영 보안 정책일 때

     *   **권한(ACL/Principal) 조건**
     *   1. KafkaAdmin으로 생성/수정/삭제를 하려면 클라이언트 Principal에 해당 권한이 필요
     *   2. auto.create.topics.enable=true 라도 수정/삭제 권한은 별도 필요
     *   3. auto.create.topics.enable=true 는 자동 생성 가능성만 여는 옵션이며, 최종 허용은 보안 정책(ACL)이 결정

     *   **핵심 결론**
     *   1. KafkaAdmin 사용의 본질적 조건은 auto.create.topics.enable 값 자체가 아니라 “토픽 관리 주체가 앱인가”입니다.
     *   2. 토픽 관리 주체가 앱이면 KafkaAdmin + 적절한 Principal 권한이 필요합니다.
     *   3. 토픽 관리 주체가 운영/인프라면 KafkaAdmin은 optional입니다.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        // Kafka Admin client가 연결할 브로커 주소를 지정한다.
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic topic1() {
        // KafkaAdmin이 관리할 토픽 정의: 이름, 파티션 수, 복제 계수
        return new NewTopic("my-topic", 3, (short) 1);
    }
}
