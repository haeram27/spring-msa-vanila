package com.example.quartz.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.println(
                String.format("(%s) %s",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH::mm::ss")),
                        jobExecutionContext.getJobDetail().getDescription()));
        log.info("({}) {}",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH::mm::ss")),
                jobExecutionContext.getJobDetail().getDescription());
    }
}
