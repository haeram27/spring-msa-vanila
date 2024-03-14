package com.example.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class ExampleJobConfig2 {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job MultiStepExampleJob() {
        Job exampleJob =
                jobBuilderFactory.get("exampleJob").start(startStep()).next(nextStep()).next(lastStep()).build();
        return exampleJob;
    }

    @Bean
    public Step startStep() {
        return stepBuilderFactory.get("startStep").tasklet((contribution, chunkContext) -> {
            log.info("Start Step!");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step nextStep() {
        return stepBuilderFactory.get("nextStep").tasklet((contribution, chunkContext) -> {
            log.info("Next Step!");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step lastStep() {
        return stepBuilderFactory.get("lastStep").tasklet((contribution, chunkContext) -> {
            log.info("Last Step!!");
            return RepeatStatus.FINISHED;
        }).build();
    }
}
