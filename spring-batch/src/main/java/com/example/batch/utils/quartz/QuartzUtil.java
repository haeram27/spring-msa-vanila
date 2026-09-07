package com.example.batch.utils.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QuartzUtil {

    public static JobBuilder quartzJobBuilder(String name, Class<? extends Job> clazz) {
        return JobBuilder.newJob(clazz).withIdentity(name);
    }
}
