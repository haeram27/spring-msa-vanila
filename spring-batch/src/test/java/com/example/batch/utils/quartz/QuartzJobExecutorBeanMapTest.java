package com.example.batch.utils.quartz;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.example.batch.utils.batch.BatchUtil;

/**
 * BeanMap 방식 테스트 - 대안 구현
 * 현재 JobRegistry 방식과 비교하기 위한 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QuartzJobExecutor 테스트 - BeanMap 방식 (대안)")
class QuartzJobExecutorBeanMapTest {

    @Mock
    private SchedulerFactoryBean schedulerFactoryBean;
    
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

    private Map<String, Job> jobMap;
    
    private QuartzJobExecutorBeanMap quartzJobExecutor;
    
    @BeforeEach
    void setUp() {
        jobMap = new HashMap<>();
        quartzJobExecutor = new QuartzJobExecutorBeanMap(schedulerFactoryBean, jobMap, jobOperator);
    }

    @Test
    @DisplayName("정상적인 Job 실행 - BeanMap 방식")
    void testExecuteInternalSuccess() throws Exception {
        // Given
        String jobName = "testBatchJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        jobMap.put(jobName, mockBatchJob);

        // When
        quartzJobExecutor.executeInternal(jobExecutionContext);

        // Then
        verify(jobOperator).start(eq(mockBatchJob), any(JobParameters.class));
    }

    @Test
    @DisplayName("Job을 찾을 수 없을 때 - BeanMap 방식")
    void testExecuteInternalJobNotFound() throws Exception {
        // Given
        String jobName = "nonexistentJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);

        // When
        quartzJobExecutor.executeInternal(jobExecutionContext);

        // Then
        verify(schedulerFactoryBean).getScheduler();
        verify(scheduler).deleteJob(jobKey);
    }

    @Test
    @DisplayName("Job 실행 중 예외 발생 시 - BeanMap 방식")
    void testExecuteInternalProviderException() throws Exception {
        // Given
        String jobName = "testBatchJob";
        JobKey jobKey = new JobKey(jobName);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(BatchUtil.BATCH_JOB_ID_KEY, jobName);

        when(jobDetail.getKey()).thenReturn(jobKey);
        when(jobExecutionContext.getJobDetail()).thenReturn(jobDetail);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        jobMap.put(jobName, mockBatchJob);
        when(jobOperator.start(eq(mockBatchJob), any(JobParameters.class)))
            .thenThrow(new RuntimeException("Operator error"));

        // When & Then
        assertThrows(JobExecutionException.class,
            () -> quartzJobExecutor.executeInternal(jobExecutionContext));
    }
}
