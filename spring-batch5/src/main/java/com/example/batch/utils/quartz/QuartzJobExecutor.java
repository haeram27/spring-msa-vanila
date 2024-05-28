package com.example.batch.utils.quartz;

import java.time.LocalDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import lombok.extern.slf4j.Slf4j;

@DisallowConcurrentExecution // prevent concurrency execution in a quartz server instances
@PersistJobDataAfterExecution // prevent concurrency execution across multiple quartz server instances
@Slf4j
public class QuartzJobExecutor extends QuartzJobBean {

    protected Job batchJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            if (batchJob != null) {
                jobLauncher.run(
                        batchJob,
                        new JobParametersBuilder().addLocalDateTime("time", LocalDateTime.now()).toJobParameters());
            } else {
                log.info("batch job is empty");
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
