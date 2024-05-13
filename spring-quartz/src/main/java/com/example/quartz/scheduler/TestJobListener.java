package com.example.quartz.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;

public class TestJobListener implements JobListener {
    @Override
    public String getName() {
        return "TestJobListener";
    }

    /**
     * run before job execution
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        JobKey k = jobExecutionContext.getJobDetail().getKey();
        System.out.println(
                String.format("[-] jobToBeExecuted()::%s::%s", k.getGroup(), k.getName()));
    }

    /**
     * run after job vetoed
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        JobKey k = jobExecutionContext.getJobDetail().getKey();
        System.out.println(
                String.format("[?] jobExecutionVetoed()::%s::%s", k.getGroup(), k.getName()));
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
        System.out.println(
                String.format("[+] jobWasExecuted()::%s::%s", k.getGroup(), k.getName()));
    }
}
