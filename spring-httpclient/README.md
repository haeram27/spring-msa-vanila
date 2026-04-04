spring-httpclient
=================

**프로젝트**: 간단한 Java HTTP 클라이언트 유틸리티(동기/비동기)와 관련 테스트를 제공하는 예제 프로젝트입니다.

HTTPClient 선택 기준:

- jdk : 가장 표준화된 client를 제공하며, 특별히 부족한 것이 없고 효율적인 방식으로 요청을 생성할 수 있다. 대부분의 경우 가장 추천 한다.
- apache httpclient : 좀더 상세한 timeout 설정이 가능하지만 대부분의 경우 필요하지 않습니다. jdk에 비해 설정이 복잡하고 버전마다 설정 변화가 커서 유지보수에 비용이 크다. spring framework 사용시 spring과 apache 버전을 변경 할 때마다 구현이 방법이 달라지면서 기존 구현 코드에서 오류가 발생할 가능성이 매우 높다. 꼭 필요하지 않다면 추천하지 않는다.
- Spring Restclient wrapper : 나름의 편리한 구조를 제공한다고 하지만 순수 Java API 만으로 대부분의 경우에 대처가 가능하므로 필수로 사용할 필요는 없다. 오히려 모든 request가 Synchronized 방식이며 반환 HttpResonse의 body가 InputStream으로 고정 되어 있어 이를 반드시 close 처리 해줘야 하기 때문에 구현 복잡도가 높아질 수 있다. 사용 방법에 대한 깊은 이해가 필요한 부분이 많으므로 java 표준 HttpClient 사용 대비 큰 장점이 있다고 하기 어렵다.
  - 특징:
    - 모든 request 는 synchronized
    - Http reponse body는 InputStream 타입, 단, ClientHttpResponse 자체를 Closeable 인터페이스 구현체로 제공

**주요 구현**

- `RestClient.java`
  - Java's built-in HttpClient를 이용하여 POJO로 구현된 RESTAPI Client 싱글턴 구현체
  - Restapi에 대해 요청을 보낼수 있는 유틸리티 클래스
  - Java HttpClient의 상세 사용 방법을 참조 가능
- `RestClientService.java`
  - spring-web의 RestClient 사용법을 참조 가능

**요구사항**:

- Java 21 이상 (가상 스레드 사용)
- Gradle

**빌드 / 테스트**:

- Junit 테스트 실행:

```bash
./gtest.sh "Tests.test"
```
