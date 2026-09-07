package com.example.batch.utils.quartz;

import java.time.LocalDateTime;
import java.util.Map;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.example.batch.utils.batch.BatchUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Alternative implementation using bean-name map lookup pattern
 * This is for comparison testing with JobRegistry approach
 * 
 * Pros: Simple, direct Spring integration
 * Cons: Relies on bean name conventions for qualifier mapping
 */
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Slf4j
public class QuartzJobExecutorBeanMap extends QuartzJobBean implements InterruptableJob {

    private final JobOperator jobOperator;
    private final SchedulerFactoryBean schedulerFactoryBean;
    private final Map<String, Job> jobMap;

    private boolean isJobInterrupted = false;
    private Job batchJob;

        /*
            jobMap injection:
            - Spring auto-injects Map<String, Job> from Job beans in the ApplicationContext.
            - No manual Map implementation is required.

            Missing-bean behavior:
            - Required single-bean injection fails at startup when no matching bean exists.
                (e.g., SomeType, @Autowired(required = true))
            - Optional/provider/collection injection does not fail when beans are missing.
                (e.g., Optional<T>, ObjectProvider<T>, List<T>, Map<String, T>, @Autowired(required = false))
        */

    @Autowired
    public QuartzJobExecutorBeanMap(SchedulerFactoryBean schedulerFactoryBean, 
            Map<String, Job> jobMap, JobOperator jobOperator) {
        this.schedulerFactoryBean = schedulerFactoryBean;
        // Spring automatically builds this map from Job beans in ApplicationContext
        // (key: bean name, value: Job bean), so no manual Map instance implementation is required.
        // Unlike required single-bean injection, Map injection does not fail when no Job beans exist.
        // In that case, Spring injects an empty map and lookup returns null at runtime.
        this.jobMap = jobMap;
        this.jobOperator = jobOperator;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        // quartz job key
        JobKey jobKey = context.getJobDetail().getKey();
        // batch job qualifier
        String jobQualifier = BatchUtil.getJobId(context.getMergedJobDataMap());
        
        try {
            // Resolve batch job by qualifier using Spring bean name mapping.
            batchJob = jobMap.get(jobQualifier);
            
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
                boolean deleted = scheduler.deleteJob(jobKey);
                if (deleted) {
                    log.info("quartz job deleted due to missing batch job: {}", jobKey);
                } else {
                    log.warn("quartz job was not deleted (already absent?): {}", jobKey);
                }
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
