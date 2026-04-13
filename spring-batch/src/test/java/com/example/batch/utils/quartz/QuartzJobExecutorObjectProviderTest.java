package com.example.batch.utils.quartz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.example.batch.utils.batch.BatchUtil;

/**
 * ObjectProvider 방식 테스트 - 대안 구현
 * 현재 JobRegistry 방식과 비교하기 위한 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuartzJobExecutor 테스트 - ObjectProvider 방식 (대안)")
class QuartzJobExecutorObjectProviderTest {

    @Mock
    private SchedulerFactoryBean schedulerFactoryBean;
    
    @Mock
    private ObjectProvider<Job> jobProvider;
    
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
    
    private QuartzJobExecutorObjectProvider quartzJobExecutor;
    
    @BeforeEach
    void setUp() {
        quartzJobExecutor = new QuartzJobExecutorObjectProvider(schedulerFactoryBean, jobProvider, jobOperator);
    }

    @Test
    @DisplayName("정상적인 Job 실행 - ObjectProvider 방식")
    void testExecuteInternalSuccess() throws JobExecutionException, SchedulerException, org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException {
        // Given
        String jobName = "testBatchJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobProvider.getIfAvailable()).thenReturn(mockBatchJob);
        when(mockBatchJob.getName()).thenReturn(jobName);

        // When
        quartzJobExecutor.executeInternal(jobExecutionContext);

        // Then
        verify(jobProvider).getIfAvailable();
        verify(jobOperator).start(eq(mockBatchJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("Job을 찾을 수 없을 때 - ObjectProvider 방식")
    void testExecuteInternalJobNotFound() throws JobExecutionException, SchedulerException, org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException {
        // Given
        String jobName = "nonexistentJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobProvider.getIfAvailable()).thenReturn(null);  // Job 없음
        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);

        // When
        quartzJobExecutor.executeInternal(jobExecutionContext);

        // Then
        verify(jobProvider).getIfAvailable();
        verify(schedulerFactoryBean).getScheduler();
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    @DisplayName("Job 실행 중 예외 발생 시 - ObjectProvider 방식")
    void testExecuteInternalProviderException() throws JobExecutionException, SchedulerException {
        // Given
        String jobName = "testBatchJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobProvider.getIfAvailable())
            .thenThrow(new RuntimeException("Provider error"));

        // When & Then
        assertThrows(JobExecutionException.class,
            () -> quartzJobExecutor.executeInternal(jobExecutionContext));
    }
}
