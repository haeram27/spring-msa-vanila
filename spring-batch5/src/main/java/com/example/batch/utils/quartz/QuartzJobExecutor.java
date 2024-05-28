package com.example.batch.utils.quartz;

import java.time.LocalDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.example.batch.utils.batch.BatchUtil;

import lombok.extern.slf4j.Slf4j;

@DisallowConcurrentExecution // prevent concurrency execution in a quartz server instances
@PersistJobDataAfterExecution // prevent concurrency execution across multiple quartz server instances
@Slf4j
public class QuartzJobExecutor extends QuartzJobBean implements InterruptableJob {

    private final JobLauncher jobLauncher;
    private final JobLocator jobLocator; // JobRegistry
    private final SchedulerFactoryBean schedulerFactoryBean;

    private boolean isJobInterrupted = false;
    private Job batchJob;

    @Autowired
    public QuartzJobExecutor(SchedulerFactoryBean schedulerFactoryBean, JobLocator jobLocator,
            JobLauncher jobLauncher) {
        this.schedulerFactoryBean = schedulerFactoryBean;
        this.jobLocator = jobLocator;
        this.jobLauncher = jobLauncher;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        JobKey jobKey = context.getJobDetail().getKey();
        try {
            //-- get batch job from batch JobRegistry
            batchJob = (Job) jobLocator.getJob(BatchUtil.getJobId(context.getMergedJobDataMap()));

            if (isJobInterrupted) {
                log.warn("job is interrupted. name: {}", batchJob.getName());
                return;
            }

            //JobParameters jobParameters = BatchUtil.getJobParameters(context);
            //jobLauncher.run(batchJob, jobParameters);
            jobLauncher.run(batchJob, new JobParametersBuilder()
                    .addLocalDateTime("time", LocalDateTime.now())
                    .toJobParameters());
        } catch (NoSuchJobException noSuchJobException) {
            log.error(noSuchJobException.toString(), noSuchJobException);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            try {
                scheduler.deleteJob(jobKey);
                log.info("job deleted: jobkey: {}", jobKey);
            } catch (SchedulerException schedulerException) {
                log.error(schedulerException.toString(), schedulerException);
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
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
