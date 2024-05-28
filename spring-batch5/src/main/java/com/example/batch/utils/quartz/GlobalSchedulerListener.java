package com.example.batch.utils.quartz;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalSchedulerListener implements SchedulerListener {

    @Override
    public void jobScheduled(Trigger trigger) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void triggersPaused(String triggerGroup) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void triggersResumed(String triggerGroup) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobsPaused(String jobGroup) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void jobsResumed(String jobGroup) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulerInStandbyMode() {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulerStarted() {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulerStarting() {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulerShutdown() {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulerShuttingdown() {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }

    @Override
    public void schedulingDataCleared() {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}", methodName);
    }
}
