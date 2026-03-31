spring-httpclient
=================

**프로젝트**: 간단한 Java HTTP 클라이언트 유틸리티(동기/비동기)와 관련 테스트를 제공하는 예제 프로젝트입니다.
POJO로 구현된 RESTAPI Client 구현체인 `JdkRestClient`를 통해 `HEAD`, `GET`, `POST` 요청을 보낼수 있습니다.

**요구사항**:
- Java 21 이상 (가상 스레드 사용)
- Gradle

**빌드 / 테스트**:
- Junit 테스트 실행:

```bash
./gtest.sh "Tests.test"
```
