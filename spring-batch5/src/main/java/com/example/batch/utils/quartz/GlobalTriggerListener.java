package com.example.batch.utils.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalTriggerListener implements TriggerListener {

    @Override
    public String getName() {
        return "GlobalTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}\n{}", methodName, trigger.toString());
    }

    /**
     * check condition of job execution by trigger firing using information of Trigger and JobExecutionContext
     * if reutrn false then Job Execution by this time of trigger firing will be progress normally 
     * if return true then Job Execution by this time of trigger firing will be vetod
     * so that TestJobListener.jobExecutionVetoed() is called instead of TestJobListener.jobWasExecuted()
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on
        JobKey k = trigger.getJobKey();

        log.trace("check to veto job execution: job:{}.{}", k.getGroup(), k.getName());
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}\n{}", methodName, trigger.toString());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        // @formatter:off
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        // @formatter:on

        log.trace("#{}\n{}", methodName, trigger.toString());
    }
}
