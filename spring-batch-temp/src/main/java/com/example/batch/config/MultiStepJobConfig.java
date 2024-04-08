package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MultiStepJobConfig extends DefaultBatchConfiguration {


    @Bean
    public Job multiStepJob(JobRepository jobRepository) {
        Job exampleJob = JobBuilder("multiStepJob", jobRepository).start(multiStepJobStartStep()).next(nextStep())
                .next(lastStep()).build();
        return exampleJob;
    }

    @Bean
    public Step multiStartStep(JobRepository jobRepository) {
        return StepBuilder("multiStartStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("multiStartStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step multiNextStep(JobRepository jobRepository) {
        return StepBuilder("multiNextStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("multiNextStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step multiLastStep(JobRepository jobRepository) {
        return StepBuilder("multiLastStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("multiLastStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }
}
