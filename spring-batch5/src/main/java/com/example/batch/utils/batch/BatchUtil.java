package com.example.batch.utils.batch;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

import com.example.batch.utils.quartz.QuartzJobExecutor;

public class BatchUtil {

    private static final String JOB_NAME_KEY = "job";
    private static final String JOB_PARAMETERS_KEY = "job.parameters";
    private static final String JOB_PARAMETERS_INSTANCE_ID_KEY = "instance.id";
    private static final String JOB_PARAMETERS_TIME_KEY = "time";
    private static final String JOB_PARAMETERS_TIMESTAMP_KEY = "timestamp";

    private static final List<String> KEYWORDS = Arrays.asList(JOB_NAME_KEY, JOB_PARAMETERS_KEY);

    public static JobDetailFactoryBeanBuilder jobDetailFactoryBeanBuilder() {
        return new JobDetailFactoryBeanBuilder();
    }

    public static String getJobName(JobDataMap jobDataMap) {
        return (String) jobDataMap.get(JOB_NAME_KEY);
    }

    public static JobParameters getJobParameters(JobExecutionContext context) throws SchedulerException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();

        return new JobParametersBuilder((JobParameters) jobDataMap.get(JOB_PARAMETERS_KEY))
                .addString(JOB_PARAMETERS_INSTANCE_ID_KEY, context.getScheduler().getSchedulerInstanceId())
                .addLong(JOB_PARAMETERS_TIMESTAMP_KEY, System.currentTimeMillis())
                .addLocalDateTime(JOB_PARAMETERS_TIME_KEY, LocalDateTime.now())
                .toJobParameters();
    }

    public static class JobDetailFactoryBeanBuilder {
        private final Map<String, Object> map;
        private final JobParametersBuilder jobParametersBuilder;

        private boolean durability = true;
        private boolean requestsRecovery = true;

        JobDetailFactoryBeanBuilder() {
            this.map = new HashMap<>();
            this.jobParametersBuilder = new JobParametersBuilder();
        }

        public JobDetailFactoryBeanBuilder job(Job job) {
            this.map.put(JOB_NAME_KEY, job.getName());
            return this;
        }

        public JobDetailFactoryBeanBuilder durability(boolean durability) {
            this.durability = durability;
            return this;
        }

        public JobDetailFactoryBeanBuilder requestsRecovery(boolean requestsRecovery) {
            this.requestsRecovery = requestsRecovery;
            return this;
        }

        public JobDetailFactoryBeanBuilder parameters(String key, Object value) {
            if (KEYWORDS.contains(key)) {
                throw new RuntimeException("Invalid Parameter.");
            }

            this.addParameter(key, value);
            return this;
        }

        public JobDetailFactoryBean build() {
            if (!map.containsKey(JOB_NAME_KEY)) {
                throw new RuntimeException("Not Found Job Name.");
            }

            this.map.put(JOB_PARAMETERS_KEY, this.jobParametersBuilder.toJobParameters());

            JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
            jobDetailFactoryBean.setJobClass(QuartzJobExecutor.class);
            jobDetailFactoryBean.setDurability(this.durability);
            jobDetailFactoryBean.setRequestsRecovery(this.requestsRecovery);
            jobDetailFactoryBean.setJobDataAsMap(this.map);

            return jobDetailFactoryBean;
        }

        private void addParameter(String key, Object value) {
            if (value instanceof String) {
                this.jobParametersBuilder.addString(key, (String) value);
                return;
            } else if (value instanceof Float || value instanceof Double) {
                this.jobParametersBuilder.addDouble(key, ((Number) value).doubleValue());
                return;
            } else if (value instanceof Integer || value instanceof Long) {
                this.jobParametersBuilder.addLong(key, ((Number) value).longValue());
                return;
            } else if (value instanceof Date) {
                this.jobParametersBuilder.addDate(key, (Date) value);
                return;
            } else if (value instanceof JobParameter) {
                this.jobParametersBuilder.addJobParameter(key, (JobParameter) value);
                return;
            }

            throw new RuntimeException("Not Supported Parameter Type.");
        }
    }
}
