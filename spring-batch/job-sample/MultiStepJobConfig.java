package com.example.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MultiStepJobConfig {

    @Bean
    public Job multiStepJob(JobRepository jobRepository, Step multiStartStep, Step multiNextStep, Step multiLastStep) {
        return new JobBuilder("multiStepJob", jobRepository).start(multiStartStep).next(multiNextStep)
                .next(multiLastStep).build();
    }

    @Bean
    public Step multiStartStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("multiStartStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("multiStartStep!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    @Bean
    public Step multiNextStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("multiNextStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("multiNextStep!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    @Bean
    public Step multiLastStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("multiLastStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("multiLastStep!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
