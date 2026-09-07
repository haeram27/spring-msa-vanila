plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(project(":api"))
    // implementation("com.example.springgrpc:spring-grpc-server-api:1.0.$buildNumber")
    implementation(project(":client"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.spring.grpc.boot.starter)

    // 영속성. 버전은 Spring Boot BOM이 관리한다(querydsl 5.1.0, h2 2.4.240).
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.querydsl:querydsl-jpa::jakarta")
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")

    // local/test 프로파일이 쓰는 인메모리 DB. 운영은 외부 DB 드라이버를 별도로 넣는다.
    runtimeOnly("com.h2database:h2")
    // Boot 4 는 H2 콘솔 자동설정을 별도 모듈로 뺐다. 이게 없으면
    // spring.h2.console.enabled 는 아무 효과 없는 죽은 설정이 된다.
    // developmentOnly 라 bootJar 에는 포함되지 않는다.
    developmentOnly("org.springframework.boot:spring-boot-h2console")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Boot 4 는 테스트 슬라이스를 기술별 모듈로 쪼갰다. @DataJpaTest 는 starter-test 가 아니라
    // 이 스타터에 들어 있다.
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
}

