package com.example.quartz.scheduler;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSchedulerListener implements SchedulerListener {

    @Override
    public void jobScheduled(Trigger trigger) {
        log.trace("#");
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        log.trace("#");
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        log.trace("#");
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        log.trace("#");
    }

    @Override
    public void triggersPaused(String triggerGroup) {
        log.trace("#");
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        log.trace("#");
    }

    @Override
    public void triggersResumed(String triggerGroup) {
        log.trace("#");
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        log.trace("#");
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        log.trace("#");
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        log.trace("#");
    }

    @Override
    public void jobsPaused(String jobGroup) {
        log.trace("#");
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        log.trace("#");
    }

    @Override
    public void jobsResumed(String jobGroup) {
        log.trace("#");
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        log.trace("#");
    }

    @Override
    public void schedulerInStandbyMode() {
        log.trace("#");
    }

    @Override
    public void schedulerStarted() {
        log.trace("#");
    }

    @Override
    public void schedulerStarting() {
        log.trace("#");
    }

    @Override
    public void schedulerShutdown() {
        log.trace("#");
    }

    @Override
    public void schedulerShuttingdown() {
        log.trace("#");
    }

    @Override
    public void schedulingDataCleared() {
        log.trace("#");
    }

}
