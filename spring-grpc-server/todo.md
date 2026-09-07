# TODO

이 프로젝트에 남아 있는 일. 각 항목은 **왜 지금 이 상태인지**를 함께 적는다 —
의도한 결정과 아직 못 한 일을 구분하기 위해서다.

작성 기준일: 2026-09-07

---

## 1. 커밋되지 않은 변경 (35개 파일)

이번 작업 전체가 워킹 트리에만 있다. git 저장소 루트는 상위 `spring-msa-vanila`이므로
경로가 `spring-grpc-server/...`로 잡힌다.

한 덩어리로 커밋하기엔 성격이 다른 변경이 섞여 있다. 나눈다면:

1. proto 리네임 (`greeting.proto` → `hello.proto`, `listview.proto` → `list_view_sample.proto`)과 그에 따른 Java 리네임
2. `ListViewSample` 서버/클라이언트/REST 구현
3. REST를 POST + 본문으로 전환 (보안)
4. 헤더 마스킹 (`SensitiveHeaders`)과 오류 응답 정리 (`GrpcStatusExceptionHandler`)
5. 프로파일 분리 (local/stage/product)
6. JPA + QueryDSL + H2 도입
7. `CLAUDE.md`, `todo.md`

---

## 2. 테스트 커버리지

현재 테스트는 `ListViewItemQueryRepositoryTest` 하나(7건)뿐이다. 리포지토리의 QueryDSL
필터/정렬/페이징만 덮는다.

나머지 계층은 **서버를 띄우고 curl로 수동 확인**한 상태다. 회귀를 자동으로 막지 못한다.

없는 것:

- [ ] `ListViewSampleService` — 페이지 크기 기본값(20), `pageNumber` 0/음수 처리
- [ ] `ListViewSampleGrpcServerService` — proto↔도메인 매핑. 특히 enum 매핑,
      `sort_key`/`sort_order` 파싱, `CONDITION_NOT_SET` 처리
- [ ] REST 컨트롤러 2쌍 — 본문 바인딩과 필터 조립
- [ ] `GrpcStatusExceptionHandler` — `Status.Code` → HTTP 매핑, 5xx description 은닉
- [ ] `SensitiveHeaders` / `ForwardableHeaders` — 마스킹과 전달 차단
- [ ] **두 경로 응답 동일성** — 이 샘플의 핵심 불변식인데 지금은 수동 `diff`로만 확인한다.
      `@SpringBootTest`로 자동화할 가치가 가장 큰 항목.

---

## 3. stage / product 는 의도적으로 미완성

`ddl-auto: validate`인데 마이그레이션 도구가 없다. 실제 DB에 붙이면
`SchemaManagementException: missing table [list_view_item]`으로 기동에 실패한다.

**이 샘플의 요구사항이 local/test 동작이므로 Flyway/Liquibase를 넣지 않기로 결정했다.**
버그가 아니다. 실제로 배포할 일이 생기면 그때 다음을 한다:

- [ ] Flyway 또는 Liquibase 추가 + `V1__create_list_view_item.sql`
- [ ] stage/product용 실 DB 드라이버 의존성 (현재는 H2만 있다)

---

## 4. 열어둔 결정

### 4-1. `cookie`를 `ForwardableHeaders` 차단 목록에 넣을지

`authorization`은 넣었다. `cookie`도 같은 자격 증명 계열이지만 지시받은 범위가
`authorization`뿐이라 두었다. 현재 `cookie`는 gRPC Metadata로 전달된다(로그에서는
`SensitiveHeaders`가 값을 가린다).

- [ ] 넣을지 결정

### 4-2. Lombok 도입 여부

버전 카탈로그에 `libs.lombok`이 있고 루트 빌드에 `compileOnly extendsFrom annotationProcessor`
배선도 준비돼 있지만 어느 모듈도 선언하지 않았다. JDK 25에서 `@Slf4j`가 동작하는 것은
확인했다.

보류한 이유: 형제 프로젝트 `spring-grpc-server-kotlin`이 있고 **Lombok은 Kotlin에서 동작하지
않는다**(`@Slf4j`를 붙여도 `log`가 생성되지 않는다). Kotlin 모듈이 생기면 로거 선언 방식이
두 갈래가 된다.

- [ ] Kotlin 사용 계획이 확정되면 결정. 순수 Java로 간다면 `@Slf4j` 도입은 합리적이다.

### 4-3. 프로파일 이름 `product` vs `prod`

Spring 관례는 `prod`다. 배포 스크립트/CI에서 쓸 이름이라 임의로 바꾸지 않고 지시대로
`product`로 두었다.

- [ ] 확정. 바꾼다면 `application-product.yml` 파일명, `CLAUDE.md`, `ExternalDatasourceGuard`의
      `@Profile` 세 곳을 함께 고친다.

---

## 5. 알려진 구멍

### 5-1. `ExternalDatasourceGuard`는 빈 값만 막는다

`DB_URL`이 비어 있을 때 임베디드 H2로 조용히 폴백되는 것은 막는다. 하지만 실수로
`DB_URL=jdbc:h2:mem:...`을 주면 그대로 통과한다. H2 드라이버가 `runtimeOnly`로 모든
프로파일 런타임에 있기 때문이다.

- [ ] product 프로파일에서 `jdbc:h2:` URL 자체를 거부할지 검토

### 5-2. 페이징이 offset 방식이다

`Pagination { page_size, page_number }` + `total_count`. 데이터가 커지면 깊은 offset이
느려지고, 페이지를 넘기는 사이 데이터가 바뀌면 항목이 밀리거나 중복된다.

Google API 설계 가이드(AIP-158)는 `page_token`/`next_page_token` 커서 방식을 권한다.
**바꾸려면 proto 변경이 필요하고 wire 호환이 깨진다.**

- [ ] 목록 규모가 커지기 전에 결정

### 5-3. `run-local.sh`가 래퍼를 쓰지 않는다

시스템 `gradle`(9.4.1)을 호출하는데 프로젝트 래퍼는 9.6.1이다. 버전 차이로 재현되지 않는
문제가 생길 수 있다.

- [ ] `./gradlew`로 바꿀지 결정

---

## 6. 정리해두면 좋은 것

- [ ] `service ListViewSample`의 이름 — 샘플이라 `Sample`이 붙어 있다. 실제 도메인이 정해지면
      바꾼다. **wire 경로에 그대로 쓰이므로 breaking change다.**
- [ ] `ListViewItem`의 필드가 아직 샘플용(`id`, `name`, `status`, `category`, `frequency`)이다.
      실제 도메인 필드는 번호 6번부터 이어 붙이면 호환이 깨지지 않는다.

## 7. haeram todo

- [ ] `ListViewSampleGrpcClientController.java`, `ListViewSampleHttpController.java`에서 `ListViewRequestBody`를 jackson을 이용해 가변 json으로 맵핑할 수 있는 Dto를 생성한다.
- [ ] `ListViewSampleService.java`에서 jpa의 specification을 이용해 search, pagination, sort를 수행하는 로직 구현
