package com.example.quartz.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.println(
                String.format("(%s) %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH::mm::ss")),
                        jobExecutionContext.getJobDetail().getDescription()));
    }
}
