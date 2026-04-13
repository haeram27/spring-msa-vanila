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
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.example.batch.utils.batch.BatchUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Alternative implementation using ObjectProvider pattern
 * This is for comparison testing with JobRegistry approach
 * 
 * Pros: Simple, direct Spring integration
 * Cons: Less control over job lookup, qualifier management is implicit
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Slf4j
public class QuartzJobExecutorObjectProvider extends QuartzJobBean implements InterruptableJob {

    private final JobOperator jobOperator;
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final ObjectProvider<Job> jobProvider;

    private boolean isJobInterrupted = false;
    private Job batchJob;

    @Autowired
    public QuartzJobExecutorObjectProvider(SchedulerFactoryBean schedulerFactoryBean, 
            ObjectProvider<Job> jobProvider, JobOperator jobOperator) {
        this.schedulerFactoryBean = schedulerFactoryBean;
        this.jobProvider = jobProvider;
        this.jobOperator = jobOperator;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        JobKey jobKey = context.getJobDetail().getKey();
        String jobQualifier = BatchUtil.getJobId(context.getMergedJobDataMap());
        
        try {
            //-- get batch job bean using ObjectProvider (alternative pattern)
            batchJob = jobProvider.getIfAvailable();
            
            if (batchJob == null) {
                throw new IllegalArgumentException(
                    String.format("Job bean not found. Qualifier: '%s'", jobQualifier));
            }

            if (isJobInterrupted) {
                log.warn("job is interrupted. name: {}", batchJob.getName());
                return;
            }

            jobOperator.start(batchJob, new JobParametersBuilder()
                    .addLocalDateTime("time", LocalDateTime.now())
                    .toJobParameters());
        } catch (IllegalArgumentException jobNotFoundException) {
            log.error("Batch job not found with qualifier: {}. Deleting quartz job: {}", 
                    jobQualifier, jobKey, jobNotFoundException);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            try {
                scheduler.deleteJob(jobKey);
                log.info("quartz job deleted due to missing batch job: {}", jobKey);
            } catch (SchedulerException schedulerException) {
                log.error("Failed to delete quartz job: {}", jobKey, schedulerException);
            }
        } catch (Exception e) {
            log.error("Unexpected error during job execution: {}", jobKey, e);
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
