package com.example.batch.config.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class QuartzBatchJob extends QuartzJobBean {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            jobLauncher.run(job,
                    new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
