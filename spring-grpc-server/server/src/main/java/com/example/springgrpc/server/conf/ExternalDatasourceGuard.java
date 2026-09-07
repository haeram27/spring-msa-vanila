package com.example.springgrpc.server.conf;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * stage/product 프로파일에서 외부 DB 접속 정보가 실제로 주입됐는지 확인한다.
 * <p>
 * 이 검사가 필요한 이유 — H2 드라이버는 {@code runtimeOnly} 로 모든 프로파일의 런타임
 * 클래스패스에 올라온다. 그 상태에서 {@code spring.datasource.url} 이 비어 있으면 Spring Boot 는
 * 오류를 내지 않고 <em>임베디드 H2 를 자동으로 띄운다</em>. 운영이 인메모리 DB 위에서 조용히
 * 돌아가고 재시작마다 데이터가 사라지는 사고로 이어진다.
 * <p>
 * {@code ddl-auto: validate} 가 "missing table" 로 걸러주기는 하지만 그건 우연이다. 스키마가
 * 우연히 맞거나 ddl-auto 설정이 바뀌면 그대로 통과한다. 그래서 원인을 직접 막는다.
 * <p>
 * {@link BeanFactoryPostProcessor} 로 등록하는 이유는 순서 때문이다. 평범한 {@code @Configuration}
 * 생성자에서 검사하면 {@code entityManagerFactory} 가 먼저 만들어져, 정작 알아보기 어려운
 * Hibernate 예외가 앞서 터진다. BFPP 는 일반 빈 인스턴스화 이전에 실행된다.
 * <p>
 * {@code ${DB_URL:}} 처럼 빈 기본값을 두는 이유는 별개다. 기본값을 아예 없애면
 * {@code @ConfigurationProperties} 가 해석 못 한 플레이스홀더를 문자열로 통과시킨다.
 */
@Configuration
@Profile({ "stage", "product" })
public class ExternalDatasourceGuard {

    @Bean
    static BeanFactoryPostProcessor requireExternalDatasource() {
        return beanFactory -> {
            Environment environment = beanFactory.getBean(Environment.class);
            String url = environment.getProperty("spring.datasource.url");
            if (url == null || url.isBlank()) {
                throw new IllegalStateException(
                    "spring.datasource.url is not set - stage/product 프로파일은 DB_URL 환경변수를 요구한다"
                        + " (미설정 시 임베디드 H2 로 조용히 폴백되는 것을 막기 위한 검사)");
            }
        };
    }
}
