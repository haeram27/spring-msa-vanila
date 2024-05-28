package com.example.batch.utils.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalTriggerListener implements TriggerListener {

    private final boolean triggerFiredLogEnable = true;
    private final boolean vetoJobExecutionLogEnable = true;
    private final boolean triggerMisfiredLogEnable = true;
    private final boolean triggerCompleteLogEnable = true;

    @Override
    public String getName() {
        return "GlobalTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        if (triggerFiredLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}\n{}", methodName, trigger.toString());
        }
    }

    /**
     * check condition of job execution by trigger firing using information of Trigger and JobExecutionContext
     * if reutrn false then Job Execution by new Object() {} time of trigger firing will be progress normally 
     * if return true then Job Execution by new Object() {} time of trigger firing will be vetod
     * so that TestJobListener.jobExecutionVetoed() is called instead of TestJobListener.jobWasExecuted()
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        boolean isVetoJob = false;

        if (vetoJobExecutionLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on
            JobKey k = trigger.getJobKey();

            log.trace("#{}:{} job:{}.{}", methodName, isVetoJob, k.getGroup(), k.getName());
        }

        return isVetoJob;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        if (triggerMisfiredLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}\n{}", methodName, trigger.toString());
        }
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        if (triggerCompleteLogEnable) {
            // @formatter:off
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            // @formatter:on

            log.trace("#{}\n{}", methodName, trigger.toString());
        }
    }
}
