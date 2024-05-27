package com.example.batch.job;

import org.springframework.batch.core.ExitStatus;
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
public class FlowStepJobConfig {

    @Bean
    public Job flowStepJob(JobRepository jobRepository, Step flowStartStep, Step flowFailOverStep, Step flowProcessStep,
            Step flowWriteStep) {
        // @formatter:off
        return new JobBuilder("flowStepJob", jobRepository)
                .start(flowStartStep) // start flowStartStep()
                    .on("FAILED") // if ExitStatus is FAILED
                    .to(flowFailOverStep) // go to failOverStep
                    .on("*") // regardless of ExitStatus of flowProcessStep
                    .to(flowWriteStep) // go to flowWriteStep
                    .on("*") // regardless of ExitStatus of flowWriteStep
                    .end() // end flow

                .from(flowStartStep) // from flowStartStep() is finished 
                    .on("COMPLETED") // if ExitStatus is COMPLETED
                    .to(flowProcessStep) // go to flowProcessStep
                    .on("*") // regardless of ExitStatus of flowProcessStep
                    .to(flowWriteStep) // go to flowWriteStep
                    .on("*") // regardless of ExitStatus of flowWriteStep
                    .end() // end flow

                .from(flowStartStep) // from flowStartStep() ExitStatus
                    .on("*") // regardless of ExitStatus of flowStartStep
                    .to(flowWriteStep) // go to flowWriteStep
                    .on("*") // regardless of ExitStatus of flowWriteStep
                    .end() // end flow
                .end().build();
        // @formatter:on
    }

    @Bean(value = "dfa")
    public Step flowStartStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowStartStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("flowStartStep!");

                    // String result = "COMPLETED";
                    // String result = "FAILED";
                    String result = "UNKNOWN";

                    // on() checks StepContribution.ExitStatus(default COMPLETED) in flow
                    if (result.equals("COMPLETED"))
                        contribution.setExitStatus(ExitStatus.COMPLETED);
                    else if (result.equals("FAILED"))
                        contribution.setExitStatus(ExitStatus.FAILED);
                    else if (result.equals("UNKNOWN"))
                        contribution.setExitStatus(ExitStatus.UNKNOWN);

                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    @Bean
    public Step flowFailOverStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowFailOverStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("flowFailOverStep!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    @Bean
    public Step flowProcessStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowProcessStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("flowProcessStep!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    @Bean
    public Step flowWriteStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("flowWriteStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    System.out.println("flowWriteStep!");
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

}
