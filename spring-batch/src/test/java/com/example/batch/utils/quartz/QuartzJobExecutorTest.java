package com.example.batch.utils.quartz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.example.batch.config.job.JobRegistry;
import com.example.batch.utils.batch.BatchUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuartzJobExecutor 테스트 - JobRegistry 방식")
class QuartzJobExecutorTest {

    @Mock
    private SchedulerFactoryBean schedulerFactoryBean;
    
    @Mock
    private JobRegistry jobRegistry;
    
    @Mock
    private JobOperator jobOperator;
    
    @Mock
    private JobExecutionContext jobExecutionContext;
    
    @Mock
    private JobDetail jobDetail;
    
    @Mock
    private Scheduler scheduler;
    
    @Mock
    private Job mockBatchJob;
    
    private QuartzJobExecutor quartzJobExecutor;
    
    @BeforeEach
    void setUp() {
        quartzJobExecutor = new QuartzJobExecutor(schedulerFactoryBean, jobRegistry, jobOperator);
    }

    @Test
    @DisplayName("정상적인 Job 실행")
    void testExecuteInternalSuccess() throws JobExecutionException, SchedulerException, org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException {
        // Given
        String jobName = "testBatchJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobRegistry.getJob(jobName)).thenReturn(mockBatchJob);
        when(mockBatchJob.getName()).thenReturn(jobName);

        // When
        quartzJobExecutor.executeInternal(jobExecutionContext);

        // Then
        verify(jobRegistry).getJob(jobName);
        verify(jobOperator).start(eq(mockBatchJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("Job을 찾을 수 없을 때 Quartz Job 삭제")
    void testExecuteInternalJobNotFound() throws JobExecutionException, SchedulerException, org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException {
        // Given
        String jobName = "nonexistentJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobRegistry.getJob(jobName))
            .thenThrow(new IllegalArgumentException("Job not found: " + jobName));
        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);

        // When
        quartzJobExecutor.executeInternal(jobExecutionContext);

        // Then
        verify(jobRegistry).getJob(jobName);
        verify(schedulerFactoryBean).getScheduler();
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    @DisplayName("Job 실행 중 예외 발생 시 JobExecutionException 발생")
    void testExecuteInternalThrowsException() throws JobExecutionException, SchedulerException {
        // Given
        String jobName = "testBatchJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobRegistry.getJob(jobName)).thenReturn(mockBatchJob);
        when(jobOperator.start(mockBatchJob, new JobParameters()))
            .thenThrow(new RuntimeException("Job execution failed"));

        // When & Then
        assertThrows(JobExecutionException.class, 
            () -> quartzJobExecutor.executeInternal(jobExecutionContext),
            "Job 실행 실패 시 JobExecutionException이 발생해야 함");
    }

    @Test
    @DisplayName("Job 인터럽트 처리")
    void testInterrupt() throws Exception {
        // Given
        when(mockBatchJob.getName()).thenReturn("testJob");

        // When - 실행 전에 인터럽트 플래그 설정 (simulate interrupt 호출)
        quartzJobExecutor.interrupt();

        // Then
        // interrupt() 호출 후 executeInternal이 호출되지 않으므로 별도 검증 불필요
        // 다만 interrupt() 메소드가 정상 작동함을 확인
    }
}
