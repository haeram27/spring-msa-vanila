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
public class SingleStepJobConfig extends DefaultBatchConfiguration {

    @Bean
    public Job SingleStepJob(JobRepository jobRepository) {
        Job exampleJob = JobBuilder("SingleStepJob", jobRepository).start(Step()).build();
        return exampleJob;
    }

    @Bean
    public Step SingleStep(JobRepository jobRepository) {
        return StepBuilder("SingleStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("SingleStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }
}
