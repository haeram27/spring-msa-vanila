package com.example.batch.utils.batch;

import java.time.LocalDateTime;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

public class BatchUtil {

    public static final String BATCH_JOB_ID_KEY = "batch.job.id";
    public static final String JOB_PARAMETERS_KEY = "job.parameters";
    public static final String JOB_PARAMETERS_SCHEDULER_INSTANCE_ID_KEY = "scheduler.instance.id";
    public static final String JOB_PARAMETERS_TIME_KEY = "time";
    public static final String JOB_PARAMETERS_TIMESTAMP_KEY = "timestamp";

    public static String getJobId(JobDataMap jobDataMap) {
        return (String) jobDataMap.get(BATCH_JOB_ID_KEY);
    }

    // TODO: 
    public static JobParameters getJobParameters(JobExecutionContext context) throws SchedulerException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        return new JobParametersBuilder((JobParameters) jobDataMap.get(JOB_PARAMETERS_KEY))
                .addString(JOB_PARAMETERS_SCHEDULER_INSTANCE_ID_KEY, context.getScheduler().getSchedulerInstanceId())
                .addLong(JOB_PARAMETERS_TIMESTAMP_KEY, System.currentTimeMillis())
                .addLocalDateTime(JOB_PARAMETERS_TIME_KEY, LocalDateTime.now())
                .toJobParameters();
    }
}
