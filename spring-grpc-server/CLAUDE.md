# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 성격

남은 작업과 열어둔 결정은 [todo.md](todo.md)에 있다.

`spring-msa-vanila` 모노레포(샘플 프로젝트 ~25개)의 한 디렉토리다. git 저장소 루트는 상위인 `spring-msa-vanila`이므로, `git` 명령의 경로는 저장소 루트 기준으로 해석된다.

Spring Boot 4 + spring-grpc로 **하나의 애플리케이션이 gRPC 서버이자 자기 자신의 gRPC 클라이언트**가 되는 구조를 보여주는 샘플이다. 프로덕션 서비스가 아니라 통신 경로와 계층 분리를 실험하는 용도다.

## 빌드 · 실행

```bash
./gradlew build -x test          # 전체 빌드
./gradlew :server:bootRun        # 서버 기동 (HTTP 8080, gRPC 9090)
./run-local.sh                   # api/client 퍼블리시 + bootRun
./gradlew :api:clean :api:generateProto   # proto만 재생성
./gradlew :server:test        # 테스트 (인메모리 H2)
./gradlew :server:test --tests '*ListViewItemQueryRepositoryTest.appliesNestedAndTree'   # 단일 테스트
```

`run-local.sh`는 래퍼가 아니라 **시스템 `gradle`을 쓴다**(현재 9.4.1 vs 래퍼 9.6.1). 버전 차이로 문제가 생기면 `./gradlew`로 같은 태스크를 직접 돌린다.

`bootRun`에 설정을 덮어쓸 때는 `--args`를 쓴다:
```bash
./gradlew :server:bootRun --args='--sample.grpc.client.port=9999'
./gradlew :server:bootRun --args='--spring.profiles.active=stage'
GRPC_CLIENT_HOST=localhost ./gradlew :server:bootRun --args='--spring.profiles.active=product'
```

### 빌드에서 걸리기 쉬운 것

- **버전 카탈로그가 외부 아티팩트다.** `settings.gradle.kts`가 `com.example.build:gradle-version-catalog:1.0.0-SNAPSHOT`을 mavenLocal에서 가져온다. 해석 실패 시 `../gradle-version-catalog/publish-to-maven-local.sh`를 먼저 실행한다. `libs.*` 별칭(`libs.lombok`, `libs.grpc.stub` 등)의 정의도 그 프로젝트의 `gradle/libs.versions.toml`에 있다.
- **`build`가 `publishToMavenLocal`을 끌고 온다.** 루트 `build.gradle.kts`에서 `repoUsername`/`repoPassword` gradle property가 없으면(로컬 개발 상태) `build`에 `publishToMavenLocal`을 `dependsOn`으로 건다.
- `privateMavenRepositoryUrl`이 없으면 공용 저장소로 폴백한다는 안내가 매 빌드 첫 줄에 찍힌다. 오류가 아니다.
- JVM 타깃은 **25**. 툴체인은 Gradle이 자동 프로비저닝한다.

## 프로파일

`application.yml`이 공통값을, `application-{local,stage,product}.yml`이 환경별 차이만 담는다.
프로파일을 지정하지 않으면 `spring.profiles.default: local`로 **local**이 뜬다.

| | local | stage | product |
|---|---|---|---|
| gRPC reflection | 켬 | 켬 | **끔** |
| 클라이언트 타임아웃 | 30s | 5s | 5s |
| `com.example.springgrpc` 로그 | DEBUG | INFO | INFO |
| 요청별 로거 | INFO | INFO | **WARN(억제)** |
| 접속 대상 host | `localhost` 고정 | 환경변수, 기본 localhost | **환경변수 필수** |

환경별로 값을 바꾼 이유:

- **reflection은 product에서 끈다.** 켜져 있으면 누구나 서비스 목록과 메시지 스키마 전체를 열람할 수 있다. 끄면 `grpcurl ... list`가 `server does not support the reflection API`로 막히고, 호출하려면 `-proto`로 정의를 직접 넘겨야 한다.
- **요청별 로거(`HttpRequestLogger`, `GrpcRequestLogger`, `HeaderLoggingInterceptor`)는 product에서 WARN으로 낮춘다.** 값은 마스킹되지만 요청마다 헤더 전체를 남기는 것은 운영 로그량이 크고, 민감 필드가 늘수록 노출면이 커진다. 이 로거들의 맵 조립은 `log.isInfoEnabled()`로 감싸 두었으므로 레벨을 낮추면 비용도 함께 사라진다.
- **product의 `sample.grpc.client.host`는 기본값이 없다.** 운영에서 조용히 localhost로 붙는 사고를 막기 위해 `${GRPC_CLIENT_HOST:}`(빈 기본값)로 두고 `GrpcClientConfiguration.requireHost(...)`가 기동을 실패시킨다. 여기서 주의할 점 — **`@ConfigurationProperties`는 해석하지 못한 플레이스홀더를 문자열 그대로 통과시킨다.** `${GRPC_CLIENT_HOST}`처럼 기본값 없이 쓰면 "Could not resolve placeholder"가 아니라 리터럴 `${GRPC_CLIENT_HOST}`가 채널 생성까지 내려가 `URISyntaxException`으로 터져 원인을 알 수 없다. 빈 기본값 + 명시적 검사 조합을 유지한다.

## 모듈 구조

| 모듈 | 역할 |
|---|---|
| `api` | `.proto`와 생성 코드. protobuf gradle 플러그인이 `build/generated/sources/proto/` 아래에 만들고 `sourceSets`로 편입한다 |
| `client` | gRPC 스텁 래퍼. Spring 비의존 순수 라이브러리라 다른 프로젝트가 가져다 쓸 수 있다 |
| `server` | Spring Boot 앱. gRPC 서버 + REST + `client` 모듈 사용 |

## 영속성 (JPA + QueryDSL + H2)

`server/repository/`가 영속성 어댑터다. **JPA 엔티티와 QueryDSL 타입은 이 패키지 밖으로 나가지 않는다.** 서비스는 도메인 `ListViewQuery`를 주고 `ListViewResult`를 받는다.

- `ListViewItemEntity`는 도메인 `ListViewEntry`와 필드가 같지만 분리한다. 도메인 레코드는 불변이라 JPA의 기본 생성자/가변 필드 요구와 맞지 않고, 컬럼·인덱스 같은 저장소 사정이 도메인에 스미는 것도 막는다. 변환은 `toDomain()` 한곳.
- enum 컬럼은 **`EnumType.STRING`**. ORDINAL은 상수 순서가 바뀌는 순간 기존 데이터의 의미가 조용히 달라진다.
- 재귀 필터 트리(`ListViewFilter`)는 `BooleanBuilder`로 조립한다. `toPredicate`의 반환 타입은 **`Predicate`**여야 한다 — `BooleanBuilder.getValue()`는 조건이 둘 이상이면 `PredicateOperation`을 돌려주므로 `BooleanExpression`으로 캐스팅하면 `ClassCastException`이 난다.
- 정렬에는 항상 `id` 타이브레이커를 덧붙여 페이지 경계에서 순서가 흔들리지 않게 한다.

### Boot 4에서 달라진 것

- **QueryDSL 버전은 Boot BOM이 관리한다**(5.1.0, Hibernate 7.2.7과 함께). 버전을 직접 쓰지 말고 `com.querydsl:querydsl-jpa::jakarta`처럼 빈 버전 + `jakarta` classifier로 선언한다.
- **`@DataJpaTest`는 `spring-boot-starter-test`에 없다.** Boot 4가 테스트 슬라이스를 기술별 모듈로 쪼갰다. `spring-boot-starter-data-jpa-test`가 필요하고 패키지도 `org.springframework.boot.data.jpa.test.autoconfigure`로 바뀌었다(예전 `...boot.test.autoconfigure.orm.jpa`가 아니다).
- **H2 콘솔 자동설정도 별도 모듈**(`spring-boot-h2console`)이다. 이게 없으면 `spring.h2.console.enabled`는 아무 효과 없는 죽은 설정이다. `developmentOnly`로 넣어 bootJar에서 빠지게 한다.

### 테이블은 누가 만드는가

**리포지토리는 테이블을 만들지 않는다.** 리포지토리를 추가해도, Spring Data JPA의 `JpaRepository`를 쓰더라도 스키마에는 아무 영향이 없다. 테이블을 만드는 것은 `@Entity` 매핑을 재료로 삼는 **Hibernate의 `ddl-auto`**다.

```
@Entity ListViewItemEntity ──(매핑 메타데이터)──► Hibernate
                                                    │ ddl-auto 를 보고
                                                    ▼
                                  create-drop → 기동 시 CREATE TABLE, 종료 시 DROP
                                  validate    → 있는지 검사만, 없으면 기동 실패
                                  none        → 아무것도 하지 않음
```

확인 방법: `--spring.jpa.hibernate.ddl-auto=none`으로 띄우면 리포지토리 빈은 정상 생성되지만 시드 INSERT가 `Table "LIST_VIEW_ITEM" not found (this database is empty)`로 실패한다.

### DB 프로파일

| | local | test | stage / product |
|---|---|---|---|
| DB | 인메모리 H2 | 인메모리 H2(클래스마다 분리) | 외부 DB (`DB_URL` 필수) |
| `ddl-auto` | `create-drop` | `create-drop` | `validate` |
| 테이블 | Hibernate가 생성 | Hibernate가 생성 | **만들지 않음** |
| 시드 | `db/seed/local-test.sql` | 테스트가 직접 삽입 | 없음 |

**stage/product는 의도적으로 미완성이다.** `validate`는 검증만 하므로 누군가 테이블을 미리 만들어줘야 하는데, 이 프로젝트에는 마이그레이션 도구가 없다. 실제 DB에 붙이면 `SchemaManagementException: missing table [list_view_item]`으로 기동에 실패한다. **이 샘플의 요구사항은 local과 test에서만 동작하는 것**이므로 Flyway/Liquibase를 넣지 않기로 했다. 버그가 아니라 결정이니, stage/product를 실제로 띄울 일이 생기면 그때 마이그레이션 도구를 추가한다.

시드는 `spring.sql.init.data-locations`로 **명시한 프로파일에서만** 실행된다. `spring.jpa.defer-datasource-initialization: true`가 함께 필요하다 — 기본값이면 Hibernate가 테이블을 만들기 전에 INSERT가 돌아 실패한다.

**`ExternalDatasourceGuard`를 지운다면 그 이유를 먼저 이해할 것.** H2 드라이버는 `runtimeOnly`라 모든 프로파일의 런타임 클래스패스에 있다. `spring.datasource.url`이 비면 Spring Boot는 오류 없이 **임베디드 H2를 자동으로 띄운다** — 운영이 인메모리 DB 위에서 조용히 돌아간다. 이 가드는 `BeanFactoryPostProcessor`로 등록되어 있는데, 평범한 `@Configuration` 생성자에서 검사하면 `entityManagerFactory`가 먼저 만들어져 알아보기 어려운 Hibernate 예외가 앞서 터지기 때문이다.

## 핵심 아키텍처: 두 경로 대조

같은 로직에 도달하는 두 경로를 나란히 두고 비교하는 것이 이 샘플의 요점이다. **한쪽만 고치면 안 된다.**

```
[REST 직접]   controller/XxxHttpController ──────────────► service/XxxService
[REST→gRPC]  grpc/client/XxxGrpcClientController
                  └─► client/XxxGrpcClient ─(9090 루프백)─► grpc/server/XxxGrpcServerService
                                                                  └─────► service/XxxService
                                                                            └─► repository/ (JPA+QueryDSL)
```

두 경로는 **동일한 JSON을 돌려줘야 한다.** 기능을 추가하면 양쪽을 모두 호출해 응답이 일치하는지 확인한다(아래 "검증" 참고).

### 계층 규칙

- **`service/`는 proto 타입을 몰라야 한다.** `service/dto/`의 도메인 타입만 쓴다. 도메인 enum에는 proto3가 강제하는 `*_UNSPECIFIED`를 두지 않는다 — wire 사정이지 도메인 상태가 아니다.
- **proto ↔ 도메인 매핑은 gRPC 어댑터(`grpc/server/`)에만 둔다.** 어댑터의 역할은 매핑 + 검증이고 로직은 서비스에 맡긴다.
- `grpc/client/`의 컨트롤러는 예외적으로 proto 타입에 직접 의존한다. proto 요청을 조립하는 것이 그 클래스의 역할이라 도메인을 한 번 거치면 enum 매핑만 중복되기 때문이다.

## REST 규칙 (보안)

**모든 REST 엔드포인트는 `@PostMapping` + `@RequestBody`를 쓴다.** 조회성 API도 예외가 아니다.

쿼리스트링은 액세스 로그, 리버스 프록시, 브라우저 히스토리, `Referer` 헤더에 값이 그대로 남는다. `@GetMapping`/`@RequestParam`을 새로 추가하지 않는다 — **메서드만 POST로 바꾸고 `@RequestParam`을 남기면 값이 여전히 URL에 실리므로 의미가 없다.** 값은 반드시 본문으로 받는다.

- 요청 본문 DTO는 record로 만들고, 생략된 필드의 기본값은 compact 생성자에서 채운다. Jackson이 레코드를 역직렬화할 때도 정규 생성자를 거치므로 그 정규화가 그대로 적용된다.
- 계층 규칙을 따라 `controller/dto/`에는 proto 타입을 두지 않는다. proto enum으로 바인딩해야 하는 본문(gRPC 중계 컨트롤러)은 해당 컨트롤러 안에 중첩 record로 둔다.
- 본문 자체가 생략될 수 있는 조회 API는 `@RequestBody(required = false)`로 받고 `empty()` 기본값을 쓴다.

## 코드 컨벤션

- **Lombok을 쓰지 않는다.** 버전 카탈로그에 `libs.lombok`이 있고 루트 빌드에 `compileOnly extendsFrom annotationProcessor` 배선도 있지만, 어느 모듈도 선언하지 않은 상태다. 로거는 `LoggerFactory.getLogger(...)`를 손으로 쓴다. 형제 프로젝트에 `spring-grpc-server-kotlin`이 있고 Lombok은 Kotlin에서 동작하지 않으므로(`@Slf4j`를 붙여도 `log`가 생성되지 않는다) 두 언어가 같은 방식을 쓰도록 유지한다.
- **유틸 클래스는 `final` + `private` 생성자 + static 메서드.** 전부 `server/util/`에 모은다(`GrpcRequestLogger`, `GrpcRequestValidator`, `GrpcCallExecutor`, `HttpRequestLogger`, `SensitiveHeaders`). 이름 앞의 `Grpc`/`Http` 접두사가 어느 스택용인지 구분하고, 접두사가 없는 것은 두 스택 공용이다.
- **enum 변환은 `valueOf(name())` 대신 명시적 switch.** proto에 상수가 추가되면 런타임에 조용히 누락되는 대신 컴파일 에러로 드러나게 한다. `sealed interface`(예: `ListViewFilter`)도 같은 목적 — 이를 소비하는 switch가 exhaustive 검사를 받는다.
- 주석과 javadoc은 한국어로 쓴다. **무엇을 하는지가 아니라 왜 그렇게 했는지**를 적는다.
- import는 알파벳순. 정적 유틸 호출은 static import 하지 않고 `GrpcRequestValidator.validateNotBlank(...)`처럼 클래스명을 붙인다.

## 오류 처리 체인

계층을 넘을 때마다 형태가 바뀌므로 전체를 알고 손대야 한다.

```
service/adapter의 IllegalArgumentException
  └─► GrpcCallExecutor ──► Status.INVALID_ARGUMENT
        └─► 클라이언트에서 StatusRuntimeException
              └─► GrpcStatusExceptionHandler ──► HTTP 400 (ProblemDetail)
```

- `grpc/server/`의 어댑터는 예외를 직접 처리하지 않고 `GrpcCallExecutor.execute(...)`에 감싼다. `StreamObserver`는 `onNext` 후 반드시 `onCompleted`를 불러야 하고 예외를 밖으로 던지면 호출자가 응답 없이 매달린다.
- `controller/GrpcStatusExceptionHandler`가 gRPC `Status.Code` 전체를 HTTP 상태로 매핑한다(exhaustive switch라 `default`가 없다). 5xx의 `description`은 내부 메시지를 담을 수 있어 응답에 싣지 않고 로그로만 남긴다.
- 이 advice의 `IllegalArgumentException` 핸들러는 넓게 잡히므로 주의한다. Spring은 매칭되는 핸들러가 없으면 예외의 cause를 따라 내려가며 다시 찾기 때문에, 더 구체적인 예외(`MethodArgumentTypeMismatchException`)를 먼저 잡지 않으면 내부 클래스명이 응답에 새어 나간다.
- 본문 역직렬화 실패(`HttpMessageNotReadableException`)는 별도 핸들러가 받는다. **Jackson 3(`tools.jackson.*`)** 이므로 `com.fasterxml.jackson.databind.*`가 아니다. 오류 위치는 Jackson의 `getPathReference()`(DTO의 FQCN을 포함한다) 대신 `getPath()`로 직접 조립해 `status[0]` 형태의 필드 경로만 노출한다.
- 오류 응답에는 우리가 정의한 필드명과 enum 상수만 담는다. 클래스명·패키지·스택은 로그로만 남긴다.

## 헤더 취급

두 가지 장치의 목적이 다르다. 헷갈리면 한쪽만 막고 안심하게 된다.

- **`SensitiveHeaders`(값을 보여주지 않는다)**: 마스킹 대상 목록의 유일한 출처. `HttpRequestLogger`(HTTP 입구)와 `HeaderLoggingInterceptor`(gRPC 입구)가 같이 쓴다. 목록을 늘릴 일이 있으면 여기만 고친다.
- **`ForwardableHeaders`(하위 호출로 넘기지 않는다)**: 이 서버가 *내보내는* gRPC 호출에만 관여한다. 전송 계층 헤더를 덮어쓰면 호출이 깨지므로 제외하고, `authorization`은 전달 자체를 막는다. **들어오는 요청에는 아무 효과가 없다** — 외부 클라이언트가 직접 보낸 자격 증명은 인터셉터 쪽에서 가려야 한다.

마스킹은 로그뿐 아니라 **서비스 계층에 넘기기 전에** 적용한다. `HelloService`처럼 헤더를 응답에 되싣는 코드가 있어, 로그만 가리면 응답 본문으로 새는 경로가 남는다. gRPC 경로는 `HeaderLoggingInterceptor`가 Context에 넣기 전에, REST 경로는 컨트롤러가 서비스에 넘기기 전에 `SensitiveHeaders.maskAll(...)`로 씌운다. 새 엔드포인트를 추가할 때 이 순서를 지켜야 두 경로의 응답이 일치한다.

gRPC Metadata 키는 항상 소문자다. grpcurl로 테스트할 때도 소문자로 보낸다.

## proto 규약

- `rpc`와 `service` 이름은 PascalCase. grpc-java는 `<ServiceName>Grpc` 클래스를 만들므로 `service HelloGrpc`는 `HelloGrpcGrpc`가 된다 — 서비스 이름에 `Grpc`를 넣지 않는다.
- wire 경로가 `/<proto package>.<Service>/<Rpc>` 형태로 이름을 그대로 쓴다. **이름 변경은 breaking change다.**
- 응답이 지금 비어 있어도 `google.protobuf.Empty` 대신 전용 response 메시지를 정의한다. `Empty`는 호환을 깨지 않고 필드를 추가할 수 없다.
- `service Xxx`를 추가/변경한 뒤에는 `:api:clean`을 함께 돌린다. 옛 생성 클래스가 `build/`에 남아 혼동을 준다.

## 검증

리포지토리 계층은 `./gradlew :server:test`가 인메모리 H2에 대고 검증한다. 그 위 계층은 서버를 띄우고 두 경로를 대조해 확인한다.

```bash
J='Content-Type: application/json'

# 두 경로가 같은 JSON을 내는지
diff <(curl -s -X POST 'localhost:8080/api/list-view-sample' -H "$J" -d '{"search":"traffic"}') \
     <(curl -s -X POST 'localhost:8080/api/grpc/client/list-view-sample' -H "$J" -d '{"search":"traffic"}')

curl -X POST 'localhost:8080/api/hello' -H "$J" -H 'x-tenant-id: T0001' -d '{"name":"hello"}'
curl -X POST 'localhost:8080/api/grpc/client/hello' -H "$J" -H 'x-tenant-id: T0001' -d '{"name":"hello"}'
```

### grpcurl로 gRPC 직접 호출

reflection이 켜져 있어 `.proto` 파일을 넘기지 않아도 된다.

```bash
grpcurl -plaintext localhost:9090 list                              # 등록된 서비스
grpcurl -plaintext localhost:9090 describe com.example.grpc.v1.Hello

grpcurl -plaintext -H 'x-tenant-id: T0001' -d '{"message":"hello"}' \
  localhost:9090 com.example.grpc.v1.Hello/Hello

# REST 표면으로는 만들 수 없는 중첩 필터 트리
grpcurl -plaintext -d '{
  "filter": {"and": {"targets": [
    {"searchString": {"value": "traffic"}},
    {"and": {"targets": [{"status": {"values": ["CREATED", "DELETED"]}}]}}
  ]}},
  "sort": {"sortKey": "name", "sortOrder": "asc"}
}' localhost:9090 com.example.grpc.v1.ListViewSample/ListAll
```

**REST로는 도달할 수 없는 분기를 여기서 검증한다.** 클라이언트 스텁 래퍼(`client/`)가 호출 전에 막는 값은 REST 경로로 서버까지 가지 않는다. 예를 들어 `-d '{"message":""}'`나 `-d '{}'`(proto3 기본값)로 보내야 서버의 `GrpcRequestValidator`가 실제로 동작하는지 확인할 수 있다 — 둘 다 `InvalidArgument: message must not be blank`가 나와야 한다.

주의할 점:
- **grpcurl 출력은 proto 필드명 그대로다.** 응답이 `total_count`(snake_case)로 나오는 반면 REST 경로는 `totalCount`로 내려간다. 두 출력을 비교할 때 이 차이를 오류로 오해하지 않는다.
- gRPC Metadata 키는 소문자만 허용되므로 `-H`도 소문자로 쓴다.
- 자격 증명 마스킹을 확인할 때는 `-H 'authorization: Bearer TEST'`처럼 넣고 로그와 응답의 `headers`에 `***`가 찍히는지 본다. 두 곳 모두 가려져야 한다.

### 그 밖

- 기동 로그의 `Registered gRPC service: com.example.grpc.v1.Xxx`로 proto 서비스 이름 변경이 반영됐는지 확인한다.
- 하위 gRPC 장애 경로는 `--args='--sample.grpc.client.port=9999'`로 기동해 재현한다(`UNAVAILABLE` → HTTP 503).
