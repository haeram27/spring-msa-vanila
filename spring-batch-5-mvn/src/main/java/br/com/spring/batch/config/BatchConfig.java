package br.com.spring.batch.config;

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
public class BatchConfig {

    @Bean
    public Job singleStepjob(JobRepository jobRepository, Step singleStep) {
        return new JobBuilder("singleStepjob", jobRepository).start(singleStep).build();
    }

    @Bean
    public Step singleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("singleStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("Hello World!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
