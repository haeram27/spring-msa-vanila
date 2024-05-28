package com.example.batch.config.job;

import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.batch.utils.batch.BatchUtil;
import com.example.batch.utils.quartz.QuartzJobExecutor;
import com.example.batch.utils.quartz.QuartzUtil;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(prefix = "job.enabler", name = "sample", havingValue = "true")
@Slf4j
public class SampleJobConfig {

    private static final String JOB_PREFIX = "sample";
    private static final String BATCH_JOB_NAME = JOB_PREFIX + "BatchJob";
    private static final String QUARTZ_JOB_NAME = JOB_PREFIX + "QuartzJob";
    private static final String TRIGGER_NAME = QUARTZ_JOB_NAME + "Trigger";

    /**
     * quartz job configuration
     */
    @Bean(QUARTZ_JOB_NAME)
    public JobDetail sampleJobDetail() {
        return QuartzUtil.quartzJobBuilder(QUARTZ_JOB_NAME, QuartzJobExecutor.class)
                .usingJobData(BatchUtil.BATCH_JOB_ID_KEY, BATCH_JOB_NAME) // QuartzJobExecutor find batch job with this name
                .storeDurably()
                .build();
    }

    @Bean(TRIGGER_NAME)
    public Trigger sampleJobTrigger(JobDetail sampleJobDetail) {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2) // Schedule interval
                .repeatForever();

        //-- by forJob(), scheduler will find designated JobDetail Bean from JobStore when it regists Trigger 
        return TriggerBuilder.newTrigger()
                .forJob(sampleJobDetail())
                .withIdentity(TRIGGER_NAME)
                .withSchedule(scheduleBuilder)
                .build();
    }

    /**
    * batch job configuration
    */
    @Bean(BATCH_JOB_NAME)
    public Job singleStepJob(JobRepository jobRepository, Step singleStep) {
        return new JobBuilder(BATCH_JOB_NAME, jobRepository).start(singleStep).build();
    }

    @Bean
    public Step singleStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("singleStep", jobRepository)
                .tasklet((StepContribution contribution, ChunkContext chunkContext) -> {
                    try {
                        log.info("singleStep launched");
                        contribution.setExitStatus(ExitStatus.COMPLETED);
                    } catch (Exception e) {
                        contribution.setExitStatus(ExitStatus.FAILED);
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
