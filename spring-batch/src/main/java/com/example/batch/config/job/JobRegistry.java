package com.example.batch.config.job;

import org.springframework.batch.core.job.Job;

/**
 * Registry for managing Spring Batch Jobs
 * Provides type-safe job lookup with fallback to ApplicationContext for dynamic jobs
 */
public interface JobRegistry {
    /**
     * Get a job by qualifier name
     * 
     * @param jobQualifier job bean name/qualifier
     * @return Job instance
     * @throws IllegalArgumentException if job not found
     */
    Job getJob(String jobQualifier);
    
    /**
     * Register a job (optional, for explicit registration)
     */
    void register(String qualifier, Job job);
}
