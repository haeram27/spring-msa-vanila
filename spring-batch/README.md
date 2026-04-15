
<h1> 
  <strong>Spring Batch Version 6 Example</strong>
</h1>
<br>

## About the project

Spring Batch 6 + Quartz Scheduler 통합 예제 프로젝트입니다.  
Quartz에서 Spring Batch Job을 타입 안전하게 조회·실행하는 구조를 구현합니다.

## DOCS

- [Spring Batch Version 6](https://docs.spring.io/spring-batch/reference/whatsnew.html)
- [Quartz Scheduler](https://www.quartz-scheduler.org/documentation/)

## Technologies

- [Java](https://www.java.com/pt-BR/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Batch](https://spring.io/projects/spring-batch)
- [Quartz Scheduler](https://www.quartz-scheduler.org/)

---

## Architecture

Spring Batch + Quartz 연동

- Quartz Job이 Trigger의 설정 시간에 fire 된다
- Quartz Job 내부에서 Spring Batch의 JobLauncher를 이용해 Batch Job이 실행된다

Quartz는 스케줄/트리거 담당이고, 실제 배치 처리 로직은 Spring Batch가 담당

주의할 점:

- Quartz Job 코드에서 Batch Job 실행 호출을 직접 넣어야 합니다.
- 같은 배치 파라미터로 재실행하면 이미 완료된 Job 인스턴스로 간주되어 실행이 안 될 수 있습니다. 보통 실행 시점 같은 고유 파라미터를 넣습니다.
- 중복 실행 방지(동시 실행 제어) 설정이 필요할 수 있습니다.

중요: `Trigger(QuartzJob(BatchJob))`의 형태로 작업이 정의되고, `Scheduler`가 `Trigger`에 정의 된 타이밍에 `QuartJob`을 실행시켜서 최종적으로 `BatchJob`이 실행된다. `BatchJob`은 `BatchStep`에서 사에서 작업을 수행한다.

### 주요 class

#### Quartz

- `org.quartz.Job`
  - Quartz용 Job 인터페이스
- `org.quartz.JobDetail`
  - Quartz의 Job Context, Quartz Job과 Trigger를 담는 컨테이너
- `org.quartz.Trigger`
  - sheduler에서 Quartz Job이 실행될 시간 정보를 담는 객체
- `org.quartz.Scheduler`
  - Trigger를 기반으로 지정된 시간이 되면 Quartz Job을 실행 시킴
- `org.springframework.scheduling.quartz.QuartzJobBean`
  - `org.quartz.Job`를 구현한 추상 클래스, spring 에서 `org.quartz.Job`을 사용하기 편하게 Wrap 처리 함

#### Batch

- `org.springframework.batch.core.job.Job`
  - Batch의 Job, Step을 담을수 있으며, Step으로 상세 단계를 구현
- `org.springframework.batch.core.repository.JobRepository`
  - 실행 메타데이터(실행 여부, 실행 시간, 성공 여부 등), 기본적으로 메모리에 저장되지만 DB에 저장 가능

#### Application

- SampleJobConfig : Quartz Job, Trigger, Batch Jog의 Bean 생성
- QuartzJobExecutorBeanMap: QuartzJobBean 구현체, Quartz scheduler를 통해 Trigger가 fire되면 `executeInternal()`이 실행됨, `executeInternal()` 에서 Batch Job Bean을 찾아서 `org.springframework.batch.core.launch.JobOperator`을 이용하여 실행

## Job 등록 및 사용 방법

### 1. Batch Job 등록

`@Bean` 어노테이션에 **Job 이름(qualifier)** 을 명시하여 등록합니다.

```java
@Configuration
@ConditionalOnProperty(prefix = "job.enabler", name = "myJob", havingValue = "true")
public class MyJobConfig {

    private static final String BATCH_JOB_NAME  = "myBatchJob";
    private static final String QUARTZ_JOB_NAME = "myQuartzJob";
    private static final String TRIGGER_NAME    = QUARTZ_JOB_NAME + "Trigger";

    // ① Quartz JobDetail 등록 - BATCH_JOB_NAME을 JobDataMap에 저장
    @Bean(QUARTZ_JOB_NAME)
    public JobDetail myJobDetail() {
        return QuartzUtil.quartzJobBuilder(QUARTZ_JOB_NAME, QuartzJobExecutor.class)
                .usingJobData(BatchUtil.BATCH_JOB_ID_KEY, BATCH_JOB_NAME) // ← Job 식별자
                .storeDurably()
                .build();
    }

    // ② Quartz Trigger 등록
    @Bean(TRIGGER_NAME)
    public Trigger myJobTrigger(JobDetail myJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(myJobDetail)
                .withIdentity(TRIGGER_NAME)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();
    }

    // ③ Spring Batch Job 등록 - Bean 이름이 BATCH_JOB_NAME과 일치해야 함
    @Bean(BATCH_JOB_NAME)
    public Job myBatchJob(JobRepository jobRepository, Step myStep) {
        return new JobBuilder(BATCH_JOB_NAME, jobRepository)
                .start(myStep)
                .build();
    }
}
```

### 2. Job 조회 흐름

Quartz가 트리거를 발동하면 `QuartzJobExecutor.executeInternal()`이 호출됩니다.

```
Quartz Trigger 발동
    → QuartzJobExecutor.executeInternal()
        → BatchUtil.getJobId(jobDataMap)       // JobDataMap에서 BATCH_JOB_NAME 추출
        → JobRegistry.getJob(jobQualifier)     // ApplicationContext에서 Job Bean 조회
        → JobOperator.start(job, parameters)   // Spring Batch Job 실행
```

### 3. application.yml Job 활성화 설정

각 Job은 `@ConditionalOnProperty`로 독립적으로 활성/비활성화할 수 있습니다.

```yaml
job:
  enabler:
    sample: true    # SampleJobConfig 활성화
    myJob: false    # MyJobConfig 비활성화
```

### 4. 동적으로 Job 추가하기

새로운 Job을 추가할 때 `JobRegistry`나 `QuartzJobExecutor`를 수정할 필요가 없습니다.  
`@Bean(BATCH_JOB_NAME)` 으로 등록만 하면 자동으로 조회됩니다.

```java
// 신규 Job 추가 - 기존 코드 변경 없음
@Bean("newFeatureJob")
public Job newFeatureJob(JobRepository jobRepository, Step newStep) {
    return new JobBuilder("newFeatureJob", jobRepository)
            .start(newStep)
            .build();
}
```

---

## 주요 컴포넌트

### `JobRegistry` / `JobRegistryImpl`
- `JobRegistry.getJob(String qualifier)` - qualifier(Bean 이름)로 Job을 타입 안전하게 조회
- 내부적으로 `ApplicationContext.getBean(qualifier, Job.class)` 위임
- Job 미존재 시 `IllegalArgumentException` (원인 포함) 발생

### `QuartzJobExecutor`
- Quartz Job 실행 진입점
- `JobRegistry`를 통해 Spring Batch Job 조회 및 실행
- Job 미존재 시 해당 Quartz Job 자동 삭제 (잘못된 스케줄 방지)
- `@DisallowConcurrentExecution` / `@PersistJobDataAfterExecution`으로 동시 실행 방지

### `QuartzConfig`
- Quartz `SchedulerFactoryBean` 설정
- `VitualVirtualThreadTaskExecutor` 사용으로 IO 작업에 최적화
- Global Listener 등록 (`GlobalJobListener`, `GlobalTriggerListener`, `GlobalSchedulerListener`)

### `BatchUtil`
- `BATCH_JOB_ID_KEY` - Quartz JobDataMap에서 Batch Job 이름을 저장하는 키 상수
- `getJobId(JobDataMap)` - JobDataMap에서 Batch Job 이름 추출

---

## 프로젝트 구조

```
src/main/java/com/example/batch/
├── BatchApplication.java
├── config/
│   ├── job/
│   │   ├── JobRegistry.java          # Job 조회 인터페이스
│   │   ├── JobRegistryImpl.java      # ApplicationContext 기반 구현
│   │   └── SampleJobConfig.java      # 샘플 Job 설정
│   └── quartz/
│       └── QuartzConfig.java         # Quartz 스케줄러 설정
└── utils/
    ├── batch/
    │   └── BatchUtil.java
    └── quartz/
        ├── QuartzJobExecutor.java    # Quartz → Batch Job 실행 브릿지
        ├── QuartzUtil.java
        ├── GlobalJobListener.java
        ├── GlobalTriggerListener.java
        └── GlobalSchedulerListener.java
```
<br><br>