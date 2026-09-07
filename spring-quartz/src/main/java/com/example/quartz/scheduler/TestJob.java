package com.example.quartz.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.trace("({}) {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH::mm::ss")),
                jobExecutionContext.getJobDetail().getDescription());
    }
}
