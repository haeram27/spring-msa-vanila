package com.example.batch.utils.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalJobListener implements JobListener {

    private final boolean jobToBeExecutedLogEnable = true;
    private final boolean jobExecutionVetoedLogEnable = true;
    private final boolean jobWasExecutedLogEnable = true;

    @Override
    public String getName() {
        return "GlobalJobListener";
    }

    /**
     * run before job execution
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        if (jobToBeExecutedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on
            JobKey k = jobExecutionContext.getJobDetail().getKey();

            log.trace("#{} job:{}.{}", methodName, k.getGroup(), k.getName());
        }
    }

    /**
     * run before job execution
     * return true then job will be vetoed
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        if (jobExecutionVetoedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on
            JobKey k = jobExecutionContext.getJobDetail().getKey();

            log.debug("#{} job:{}.{}", methodName, k.getGroup(), k.getName());
        }
    }

    /**
     * run after job execution
     *
     * @param jobExecutionContext
     * @param e
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        if (jobWasExecutedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on
            JobKey k = jobExecutionContext.getJobDetail().getKey();

            log.trace("#{} job:{}.{}", methodName, k.getGroup(), k.getName());
        }
    }
}
