package com.example.batch.config.quartz;

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
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void triggersPaused(String triggerGroup) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void triggersResumed(String triggerGroup) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobsPaused(String jobGroup) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void jobsResumed(String jobGroup) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulerInStandbyMode() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulerStarted() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulerStarting() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulerShutdown() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulerShuttingdown() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }

    @Override
    public void schedulingDataCleared() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.trace(methodName);
    }
}
