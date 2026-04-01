spring-httpclient
=================

**프로젝트**: 간단한 Java HTTP 클라이언트 유틸리티(동기/비동기)와 관련 테스트를 제공하는 예제 프로젝트입니다.

HTTPClient 선택 기준:
- jdk : 가장 표준화된 client를 제공하며, 특별히 부족한 것이 없고 효율적인 방식으로 요청을 생성할 수 없습니다. 대부분의 경우 가장 추천 합니다.
- apache httpclient : 좀더 상세한 timeout 설정이 가능하지만 대부분의 경우 필요하지 않습니다. jdk에 비해 설정이 복잡하고 버전마다 설정 변화가 커서 유지보수에 비용이 듭니다. spring framework 사용시 spring과 apache 버전을 변경 할 때마다 구현이 방법이 달라지면서 기존 구현 코드에서 오류가 발생할 가능성이 매우 높습니다. 꼭 필요하지 않다면 추천하지 않습니다.
- spring restclient wrapper : 나름의 편리한 구조를 제공한다고 하지만 순수 Java API 만으로 대부분의 경우에 대처가 가능하므로 필수로 사용할 필요가 없습니다. 오히려 반환 HttpResonse(InputStream)를 반드시 close 해줘야 하면서 구현 복잡도가 높아지고, 사용 방법에 대한 깊은 이해가 필요한 부분이 많아, 코딩 실수 및 메모리 누수가 발생할 수 있습니다.

** 주요 구현 **
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
