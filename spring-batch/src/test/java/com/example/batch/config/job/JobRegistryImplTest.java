package com.example.batch.config.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobRegistry 테스트 - 현재 구현 방식")
class JobRegistryImplTest {

    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private Job mockJob;

    @InjectMocks
    private JobRegistryImpl jobRegistry;

    @Test
    @DisplayName("Job이 존재할 때 정상 반환")
    void testGetJobSuccess() {
        // Given
        String jobName = "testBatchJob";
        when(applicationContext.getBean(jobName, Job.class)).thenReturn(mockJob);
        when(mockJob.getName()).thenReturn(jobName);

        // When
        Job result = jobRegistry.getJob(jobName);

        // Then
        assertNotNull(result);
        assertEquals(mockJob, result);
        verify(applicationContext).getBean(jobName, Job.class);
    }

    @Test
    @DisplayName("Job이 존재하지 않을 때 IllegalArgumentException 발생")
    void testGetJobNotFound() {
        // Given
        String jobName = "nonexistentJob";
        when(applicationContext.getBean(jobName, Job.class))
            .thenThrow(new NoSuchBeanDefinitionException(jobName));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> jobRegistry.getJob(jobName),
            "Job을 찾을 수 없을 때 IllegalArgumentException이 발생해야 함"
        );

        assertTrue(exception.getMessage().contains("nonexistentJob"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("동적으로 추가된 Job도 조회 가능")
    void testGetDynamicJob() {
        // Given - 런타임 중 새로 등록된 Job
        String dynamicJobName = "dynamicNewJob";
        Job dynamicJob = mockJob;
        
        when(applicationContext.getBean(dynamicJobName, Job.class)).thenReturn(dynamicJob);

        // When
        Job result = jobRegistry.getJob(dynamicJobName);

        // Then
        assertNotNull(result);
        assertEquals(dynamicJob, result);
    }
}
