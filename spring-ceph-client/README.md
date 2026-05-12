# Spring Ceph Client - S3Presigner Backend MSA

**프로젝트**: Frontend에 Ceph/S3 스토리지 기능을 제공하는 Backend MSA(마이크로서비스 아키텍처)

Frontend 클라이언트가 직접 S3 호환 스토리지(Ceph RadosGW)에 파일을 업로드/다운로드할 수 있도록 **AWS S3Presigner 기반의 서명된 URL 발급** 아키텍처를 제공합니다.

## 🎯 핵심 아키텍처

**URL 발급 중심 설계**: 백엔드 서버는 S3 요청에 필요한 서명을 미리 만들어 URL 형태로 프론트엔드에 전달하고, 프론트엔드는 이 URL을 이용하여 S3에 **직접 접근**합니다.

```
Frontend → Backend(Presigner) → S3Presigner API → [서명된 URL 발급]
                                                       ↓
Frontend → S3 (서버 우회, 직접 데이터 전송)
```

**장점:**
- 서버 부하 최소화 (데이터가 서버를 경유하지 않음)
- 병렬 업로드/다운로드 지원
- 대용량 파일 처리 효율성

## 📋 주요 기능 시나리오

AWS SDK 기준 버전: `software.amazon.awssdk BOM 2.44.4`

### 1. **단일 Object PUT/GET** (`presignPutObject`, `presignGetObject`)
- 일반적인 파일 업로드/다운로드
- 작은 파일에 최적화

### 2. **Multipart Upload** (`presignCreateMultipartUpload`, `presignUploadPart`, `presignCompleteMultipartUpload`)
- 대용량 파일 업로드
- 병렬 파트 전송
- 실패 시 파트별 재시도 가능

### 3. **멀티파트 실패 복구** (`presignAbortMultipartUpload`)
- 업로드 중단 후 미완료 파트 정리
- 스토리지 비용 누수 방지

### 4. **객체 삭제 위임** (`presignDeleteObject`)
- 프론트엔드가 직접 객체 삭제

### 5. **객체 메타 확인** (`presignHeadObject`)
- 객체 존재/크기/ETag 확인 (응답 바디 없음)
- SDK 2.44.4+ 정식 지원

### 6. **버킷 접근 가능 여부 확인** (`presignHeadBucket`)
- 업로드 시작 전 권한 검증
- SDK 2.44.4+ 정식 지원

### 7. **Range 부분 다운로드** (`presignGetObject` + Range 헤더)
- 대용량 파일 부분 다운로드
- 스트리밍/재생 탐색 지원

## 📦 프로젝트 구조

```
src/main/java/com/example/cephclient/
├── SpringApplication.java                 # 메인 진입점
├── component/
│   └── DefaultApplicationRunner.java       # 애플리케이션 초기화
├── config/
│   ├── AwsSdkConfig.java                   # AWS SDK 설정
│   ├── RadosgwAdmin4jConfig.java           # Ceph RadosGW 관리자 설정
│   ├── JdkHttpClientConfig.java            # Java HttpClient 설정
│   ├── JacksonConfig.java                  # JSON 직렬화/역직렬화
│   ├── RestServerConfig.java               # REST 서버 설정
│   └── SpringLifeCycleEventListener.java    # 라이프사이클 이벤트
├── model/
│   └── RestServerConfigDto.java            # 서버 설정 DTO
├── restclient/
│   ├── jdk/RestClient.java                 # Java built-in HttpClient 구현
│   └── spring/RestClientService.java       # Spring RestClient 구현
├── s3/
│   ├── S3ClientFacade.java                 # S3 클라이언트 파사드
│   ├── S3PresignerController.java          # Presigner API 엔드포인트
│   └── S3PresignerFacade.java              # S3Presigner 파사드
├── service/
│   └── FileReadService.java                # 파일 읽기 서비스
└── util/
    └── PathUtil.java                       # 경로 유틸리티
```

## 🔧 주요 구현 클래스

### S3PresignerController
Frontend 클라이언트가 호출하는 REST API 엔드포인트:
- `POST /presign-put` - 업로드용 URL 발급
- `GET /presign-get` - 다운로드용 URL 발급
- `POST /multipart/start` - 멀티파트 시작
- `POST /multipart/part-url` - 파트별 URL 발급
- `POST /multipart/complete` - 멀티파트 완료
- `POST /multipart/abort-url` - 멀티파트 중단
- `POST /presign-delete` - 삭제용 URL 발급
- `GET /presign-head-object` - 객체 메타 확인 URL 발급
- `GET /presign-head-bucket` - 버킷 접근 확인 URL 발급
- `GET /presign-get-range` - Range 다운로드 URL 발급

### S3PresignerFacade
AWS SDK의 `S3Presigner`를 감싸는 파사드 클래스:
- URL 생성 로직 캡슐화
- 헤더 관리 및 서명 처리
- 만료 시간 설정

### S3ClientFacade
직접 S3 호출 시 사용 (Fallback/검증용):
- 객체 존재 여부 확인
- 메타데이터 조회
- 버킷 접근성 검증

## 🌐 HTTP 클라이언트 구성

### Java Built-in HttpClient (권장)
- **이유**: 표준화된 API, 설정 간결함, Spring 버전 업데이트 영향 최소화
- **구현**: `com.example.cephclient.restclient.jdk.RestClient`
- **설정 순서**: `RestClient(JdkClientHttpRequestFactory(HttpClient(SSLContext)))`

### Spring RestClient (선택)
- **특징**: 모든 요청이 동기식(synchronized), 응답 바디가 InputStream 고정
- **구현**: `com.example.cephclient.restclient.spring.RestClientService`

## 📊 요청/응답 데이터 구조

### PUT URL 발급 예시
```json
// 요청
{
  "bucket": "my-bucket",
  "key": "images/2026/05/a.png",
  "contentType": "image/png",
  "contentLength": 1048576,
  "expiresInSeconds": 300
}

// 응답
{
  "uploadUrl": "https://...",
  "method": "PUT",
  "headers": {
    "Content-Type": "image/png"
  },
  "expiresAt": "2026-05-12T04:30:00Z"
}
```

### Multipart 시작 예시
```json
// 요청
{
  "bucket": "my-bucket",
  "key": "videos/2026/demo.mp4",
  "contentType": "video/mp4",
  "expiresInSeconds": 3600
}

// 응답
{
  "startUrl": "https://...",
  "expiresAt": "2026-05-12T05:30:00Z"
}

// S3 직접 호출 결과 (UploadId 획득)
{
  "UploadId": "VXBsb2FkIElE..."
}
```

## ⚙️ 설정 및 실행

### 요구사항
- **Java**: 21 이상 (가상 스레드 사용)
- **빌드**: Gradle
- **AWS SDK**: software.amazon.awssdk BOM 2.44.4+

### 빌드
```bash
./gradlew assemble
```

### 테스트
```bash
./gradlew test --rerun-tasks
```

## 🧪 테스트 커버리지

- **S3ClientFacadeIntegrationTests**: 실제 S3 연결 테스트
- **S3ClientFacadeMockTests**: Mock 기반 단위 테스트
- **S3PresignerFacadeIntegrationTests**: Presigner 통합 테스트
- **S3PresignerFacadeMockTests**: Presigner Mock 테스트
- **RestClientServiceTests**: Spring RestClient 테스트
- **JdkHttpClientTests**: Java HttpClient 테스트

## ⚠️ 주의사항

1. **헤더 정합성**: Presign 시 포함된 헤더는 실제 요청과 동일해야 함
2. **만료 시간**: 짧은 TTL 권장 (기본 300초)
3. **Multipart 중단**: 미완료 파트는 스토리지 비용 누적 → 실패 시 반드시 Abort 호출
4. **Range 헤더**: 서명에 포함된 경우 실제 호출 헤더 값이 일치해야 함

## 📚 상세 시나리오 가이드

자세한 API 시퀀스 다이어그램 및 요청/응답 데이터는 다음 파일을 참조하세요:
- **파일**: `.github/scenario/aws.s3presigner.scenarios.html`
- **내용**: 7가지 시나리오별 Mermaid 시퀀스 다이어그램, 용어 정리, 요청/응답 JSON 예시
