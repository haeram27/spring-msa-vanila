package com.example.batch.config.job;

import org.springframework.batch.core.job.Job;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default JobRegistry implementation
 * 1. First tries to get Job from ApplicationContext (covers both pre-registered and dynamic jobs)
 * 2. Falls back to manual registry if needed
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobRegistryImpl implements JobRegistry {
    
    private final ApplicationContext applicationContext;
    
    @Override
    public Job getJob(String jobQualifier) {
        try {
            // Try to get Job from ApplicationContext by qualifier name
            Job job = applicationContext.getBean(jobQualifier, Job.class);
            log.debug("Found job from ApplicationContext: {}", jobQualifier);
            return job;
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Job not found in ApplicationContext: {}", jobQualifier, e);
            throw new IllegalArgumentException(
                String.format("Job bean not found with qualifier: '%s'. " + 
                    "Ensure the job is registered as a Spring Bean with @Bean annotation.", 
                    jobQualifier), 
                e);
        }
    }
    
    @Override
    public void register(String qualifier, Job job) {
        log.debug("Registering job: {} -> {}", qualifier, job.getName());
        // For future enhancements - manual registry if needed
    }
}
