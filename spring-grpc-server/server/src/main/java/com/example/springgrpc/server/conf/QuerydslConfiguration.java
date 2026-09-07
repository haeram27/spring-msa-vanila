package com.example.springgrpc.server.conf;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 진입점. {@link JPAQueryFactory}는 스스로 빈이 되지 않으므로 여기서 등록한다.
 * <p>
 * 주입받는 {@link EntityManager}는 Spring이 넣어주는 프록시라, 실제 호출 시점의 트랜잭션에
 * 묶인 EntityManager로 위임된다. 따라서 이 팩터리를 싱글턴으로 공유해도 안전하다.
 */
@Configuration
public class QuerydslConfiguration {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
