---
applyTo: "**/*.java,**/*.gradle,**/src/main/resources/**/*.yml,**/src/test/**/*.java"
description: "Java Spring project conventions: spaces, system Gradle, test naming, logging and exception handling"
---

# Java Project Rules (Mini)

## 코드 스타일
- 들여쓰기는 스페이스만 사용한다.
- 외부 연동 코드는 stateless로 유지한다.
- 설정값은 ConfigurationProperties로 주입한다.

## 빌드 및 실행
- Gradle 실행은 gradlew 대신 시스템 Gradle을 사용한다.

## 테스트
- 테스트 메서드명은 대상메서드_조건_기대결과 형식을 사용한다.
- 단위 테스트는 외부 I/O 없이 작성한다.
- 정상, 예외, null, 경계값 케이스를 분리한다.
- 코드 변경 후 관련 테스트를 최소 1회 실행한다.

## 입력 유효성 검사
- **Public 메소드 (Controller, Facade)**: 모든 인자 검사 필수
  - 외부 입력은 신뢰할 수 없으므로 반드시 검증한다.
  - 예: `requireNonNull()`, `requireNotBlank()` 등 사용
- **Private/Internal 메소드**: 검사 선택적
  - 호출 메소드에서 이미 검사한 값만 수신하므로 스킵 가능
  - 성능 최적화가 필요한 경우 생략 고려
- **Record 클래스**: Compact constructor에서 필드 검증
  - 생성 시점에 유효성을 보장한다.

## 로깅
- 로그는 동작, 대상, 결과가 보이게 작성한다.
- 민감정보(키, 토큰, 비밀번호)는 로그에 남기지 않는다.

## 예외 처리
- 예외는 무시하지 않고 로그를 남긴다.
- 정책에 맞는 반환값 또는 도메인 예외로 처리한다.

## 실패 보고
- 빌드 또는 테스트 실패 시 원인과 재현 방법을 짧게 남긴다.
