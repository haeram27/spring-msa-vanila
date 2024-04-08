package com.example.batch.config;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FlowStepJobConfig extends DefaultBatchConfiguration {

    @Bean
    public Job flowStepJobConfig(JobRepository jobRepository) {

        Job exampleJob = JobBuilder("flowStepJob", jobRepository).start(flowStartStep()).on("FAILED") //startStep의 ExitStatus가 FAILED일 경우
                .to(flowFailOverStep()) //failOver Step을 실행 시킨다.
                .on("*") //failOver Step의 결과와 상관없이
                .to(flowWriteStep()) //write Step을 실행 시킨다.
                .on("*") //write Step의 결과와 상관없 이
                .end() //Flow를 종료시킨다.

                .from(flowStartStep()) //startStep이 FAILED가 아니고
                .on("COMPLETED") //COMPLETED일 경우
                .to(flowProcessStep()) //process Step을 실행 시킨다
                .on("*") //process Step의 결과와 상관없이
                .to(flowWriteStep()) // write Step을 실행 시킨다.
                .on("*") //wrtie Step의 결과와 상관없이
                .end() //Flow를 종료 시킨다.

                .from(flowStartStep()) //startStep의 결과가 FAILED, COMPLETED가 아닌
                .on("*") //모든 경우
                .to(flowWriteStep()) //write Step을 실행시킨다.
                .on("*") //write Step의 결과와 상관없이
                .end() //Flow를 종료시킨다.
                .end().build();

        return exampleJob;
    }

    @Bean
    public Step flowStartStep(JobRepository jobRepository) {
        return StepBuilder("flowStartStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("Start Step!");

            String result = "COMPLETED";
            //String result = "FAIL";
            //String result = "UNKNOWN";

            //Flow에서 on은 RepeatStatus가 아닌 ExitStatus를 바라본다.
            if (result.equals("COMPLETED"))
                contribution.setExitStatus(ExitStatus.COMPLETED);
            else if (result.equals("FAIL"))
                contribution.setExitStatus(ExitStatus.FAILED);
            else if (result.equals("UNKNOWN"))
                contribution.setExitStatus(ExitStatus.UNKNOWN);

            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step flowFailOverStep(JobRepository jobRepository) {
        return StepBuilder("flowFailOverStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("flowFailOverStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step flowProcessStep(JobRepository jobRepository) {
        return StepBuilder("flowProcessStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("flowProcessStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }


    @Bean
    public Step flowWriteStep(JobRepository jobRepository) {
        return StepBuilder("flowWriteStep", jobRepository).tasklet((contribution, chunkContext) -> {
            log.info("flowWriteStep!");
            return RepeatStatus.FINISHED;
        }).build();
    }
}
