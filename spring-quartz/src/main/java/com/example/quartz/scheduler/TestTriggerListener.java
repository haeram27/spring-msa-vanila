package com.example.quartz.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestTriggerListener implements TriggerListener {
    @Override
    public String getName() {
        return "TestTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        log.trace("#\n{}", trigger.toString());
    }

    /**
     * check condition of job execution by trigger firing using information of Trigger and JobExecutionContext
     * if reutrn false then Job Execution by this time of trigger firing will be progress normally 
     * if return true then Job Execution by this time of trigger firing will be vetod
     * so that TestJobListener.jobExecutionVetoed() is called instead of TestJobListener.jobWasExecuted()
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        log.trace("#");
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.trace("#\n{}", trigger.toString());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        log.trace("#\n{}", trigger.toString());
    }

}
