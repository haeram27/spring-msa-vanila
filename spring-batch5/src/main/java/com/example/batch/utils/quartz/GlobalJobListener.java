package com.example.batch.utils.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalJobListener implements JobListener {

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
        JobKey k = jobExecutionContext.getJobDetail().getKey();
        log.trace("# g={} n={}", k.getGroup(), k.getName());
    }

    /**
     * run after job vetoed
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        JobKey k = jobExecutionContext.getJobDetail().getKey();
        log.trace("# g={} n={}", k.getGroup(), k.getName());
    }

    /**
     * run after job execution
     *
     * @param jobExecutionContext
     * @param e
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        JobKey k = jobExecutionContext.getJobDetail().getKey();
        log.trace("# g={} n={}", k.getGroup(), k.getName());
    }
}
