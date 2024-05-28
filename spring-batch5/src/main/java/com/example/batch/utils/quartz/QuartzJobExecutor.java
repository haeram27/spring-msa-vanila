package com.example.batch.utils.quartz;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.example.batch.utils.batch.BatchUtil;

import lombok.extern.slf4j.Slf4j;

@DisallowConcurrentExecution // prevent concurrency execution in a quartz server instances
@PersistJobDataAfterExecution // prevent concurrency execution across multiple quartz server instances
@Slf4j
public class QuartzJobExecutor extends QuartzJobBean implements InterruptableJob {

    private boolean isJobInterrupted = false;

    protected Job batchJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            if (batchJob != null) {
                JobParameters jobParameters = BatchUtil.getJobParameters(context);
                if (isJobInterrupted) {
                    log.warn("job is interrupted. [name: {}, parameters: {}]",
                            batchJob.getName(),
                            jobParameters);
                    return;
                }

                jobLauncher.run(batchJob, jobParameters);
            } else {
                log.info("batch job is empty");
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        if (batchJob != null) {
            log.info("interrupting job: {}", batchJob.getName());
        }
        isJobInterrupted = true;
    }
}
