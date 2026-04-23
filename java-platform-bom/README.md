# java-platform-bom

Spring MSA 서비스 전체에서 공통으로 사용하는 의존성 버전을 중앙 관리하는 **Gradle Java Platform BOM** 프로젝트입니다.

---

## 프로젝트 구조

```
java-platform-bom/
├── build.gradle              # java-platform + maven-publish 설정
├── settings.gradle
├── gradle.properties         # 성능 옵션, 자격증명 키 템플릿
└── gradle/
    └── libs.versions.toml    # 버전 카탈로그 (중앙 버전 관리)
```

---

## 핵심 설계 포인트

### `java-platform` 플러그인

`dependencyManagement`(BOM) 전용 Gradle 플러그인으로, 소스 코드 없이 의존성 버전 제약만 담은 Maven BOM(`.pom`)을 생성·배포합니다.

### `javaPlatform { allowDependencies() }`

다른 BOM을 `api platform(...)` 으로 import 하기 위해 필요한 설정입니다. 기본 상태에서는 platform import가 허용되지 않습니다.

### BOM re-export (`api platform(...)`)

```groovy
api platform(libs.spring.boot.dependencies)
api platform(libs.spring.cloud.dependencies)
api platform(libs.jackson.bom)
api platform(libs.testcontainers.bom)
```

`api` 스코프로 선언하면 이 BOM을 사용하는 소비자 프로젝트에도 해당 버전 정책이 **전이(transitive)** 됩니다.

### `constraints { api ... }` — 개별 버전 고정

Spring BOM에 포함되지 않은 라이브러리는 `constraints` 블록에서 버전을 직접 지정합니다.

| 카테고리 | 라이브러리 |
|----------|-----------|
| Persistence / Data | QueryDSL, MapStruct, P6Spy |
| Utility | Lombok, Guava, Commons Lang3 / IO / Collections4 |
| Testing | Mockito, ArchUnit |
| Observability | Micrometer Prometheus Registry |

### 버전 카탈로그 (`libs.versions.toml`)

모든 버전 숫자는 `gradle/libs.versions.toml` 한 곳에서 관리합니다.  
`build.gradle`에서는 `libs.*` 별칭만 참조하므로 버전 변경 시 `.toml` 파일만 수정하면 됩니다.

### `components.javaPlatform` — BOM 게시

`MavenPublication`에서 `from components.javaPlatform`으로 선언하면 Gradle이 `<dependencyManagement>` 섹션을 포함한 표준 Maven BOM `.pom` 파일을 자동 생성합니다.

---

## 배포 명령

### 로컬 파일 시스템 저장소 (`build/repo/`) — 개발·검증용

```bash
gradle publishMavenBomPublicationToLocalRepository
```

### 시스템 Maven 로컬 저장소 (`~/.m2/repository/`) — 개발·검증용

```bash
gradle publishToMavenLocal
```

- 다른 로컬 프로젝트에서 `mavenLocal()` 추가만으로 바로 사용 가능

### Nexus / Artifactory 배포

`build.gradle`의 `nexus` 저장소 블록 주석을 해제하고 자격증명을 설정합니다.

```properties
# ~/.gradle/gradle.properties
nexusUser=<username>
nexusPassword=<password>
```

```bash
gradle publishMavenBomPublicationToNexusRepository
```

### GitHub Packages 배포

`build.gradle`의 `githubPackages` 저장소 블록 주석을 해제하고 자격증명을 설정합니다.

```properties
# ~/.gradle/gradle.properties
gpr.user=<github-username>
gpr.key=<personal-access-token>
```

```bash
gradle publishMavenBomPublicationToGithubPackagesRepository
```

---

## 소비자 프로젝트에서 사용 방법

### Gradle

```groovy
// settings.gradle — 로컬 저장소를 사용하는 경우
dependencyResolutionManagement {
    repositories {
        maven { url = uri('/path/to/java-platform-bom/build/repo') }
        mavenCentral()
    }
}
```

```groovy
// build.gradle
dependencies {
    implementation platform('com.example.platform:java-platform-bom:1.0.0-SNAPSHOT')

    // 버전 생략 가능 — BOM이 관리
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
    implementation 'com.google.guava:guava'
    implementation 'org.mapstruct:mapstruct'
    compileOnly     'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.example.platform</groupId>
            <artifactId>java-platform-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 버전 업그레이드 방법

`gradle/libs.versions.toml` 에서 버전 숫자만 변경한 뒤 재배포합니다.

```toml
[versions]
spring-boot = "3.5.0"   # 여기만 수정
```

```bash
gradle publishMavenBomPublicationToLocalRepository
```
