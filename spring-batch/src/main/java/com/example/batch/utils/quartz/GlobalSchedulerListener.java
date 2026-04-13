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

    private final boolean jobScheduledLogEnable = true;
    private final boolean jobUnscheduledLogEnable = true;
    private final boolean triggerFinalizedLogEnable = true;
    private final boolean triggerPausedLogEnable = true;
    private final boolean triggersPausedLogEnable = true;
    private final boolean triggerResumedLogEnable = true;
    private final boolean triggersResumedLogEnable = true;
    private final boolean jobAddedLogEnable = true;
    private final boolean jobDeletedLogEnable = true;
    private final boolean jobPausedLogEnable = true;
    private final boolean jobsPausedLogEnable = true;
    private final boolean jobResumedLogEnable = true;
    private final boolean jobsResumedLogEnable = true;
    private final boolean schedulerErrorLogEnable = true;
    private final boolean schedulerInStandbyModeLogEnable = true;
    private final boolean schedulerStartedLogEnable = true;
    private final boolean schedulerStartingLogEnable = true;
    private final boolean schedulerShutdownLogEnable = true;
    private final boolean schedulerShuttingdownLogEnable = true;
    private final boolean schedulingDataClearedLogEnable = true;

    @Override
    public void jobScheduled(Trigger trigger) {
        if (jobScheduledLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        if (jobUnscheduledLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void triggerFinalized(Trigger trigger) {
        if (triggerFinalizedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        if (triggerPausedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void triggersPaused(String triggerGroup) {
        if (triggersPausedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        if (triggerResumedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void triggersResumed(String triggerGroup) {
        if (triggersResumedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobAdded(JobDetail jobDetail) {
        if (jobAddedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        if (jobDeletedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        if (jobPausedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobsPaused(String jobGroup) {
        if (jobsPausedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        if (jobResumedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void jobsResumed(String jobGroup) {
        if (jobsResumedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        if (schedulerErrorLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.error("#{} {}", methodName, msg);
            log.error(cause.toString(), cause);
        }
    }

    @Override
    public void schedulerInStandbyMode() {
        if (schedulerInStandbyModeLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void schedulerStarted() {
        if (schedulerStartedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void schedulerStarting() {
        if (schedulerStartingLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void schedulerShutdown() {
        if (schedulerShutdownLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void schedulerShuttingdown() {
        if (schedulerShuttingdownLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }

    @Override
    public void schedulingDataCleared() {
        if (schedulingDataClearedLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}", methodName);
        }
    }
}
