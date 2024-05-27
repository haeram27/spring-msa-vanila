package com.example.batch.config.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuartzHelper {

    public static JobDetail buildJobDetail(String name, Class<? extends Job> clazz) {
        return JobBuilder.newJob(clazz)
                .withIdentity(name)
                .storeDurably()
                .build();
    }
}
