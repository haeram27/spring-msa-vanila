package com.example.batch.config.quartz;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import com.example.batch.utils.quartz.GlobalJobListener;
import com.example.batch.utils.quartz.GlobalSchedulerListener;
import com.example.batch.utils.quartz.GlobalTriggerListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class QuartzConfig {
    private final ApplicationContext applicationContext;
    private final QuartzProperties quartzProperties;
    private final DataSource dataSource;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(30);
        threadPoolTaskExecutor.setMaxPoolSize(30);
        threadPoolTaskExecutor.setQueueCapacity(30);
        threadPoolTaskExecutor.setThreadGroupName("batch-job-group-");
        threadPoolTaskExecutor.setThreadNamePrefix("batch-job-");
        //-- add policy to handle rejected task caused by lack of q capacity or thread number
        //-- CallerRunsPolicy() runs the rejected task directly in the calling thread of the execute method
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        return threadPoolTaskExecutor;
    }

    @Bean
    public JobFactory jobFactory(AutowireCapableBeanFactory beanFactory) {
        return new SpringBeanJobFactory() {

            @Override
            protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                beanFactory.autowireBean(job);
                return job;
            }
        };
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, Trigger[] triggers, JobDetail[] jobDetails,
            ThreadPoolTaskExecutor threadPoolTaskExecutor) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        //-- retrieve all quartz JobDetail beans
        Map<String, JobDetail> jobDetailMap = applicationContext.getBeansOfType(JobDetail.class);

        //-- retrieve all quartz Trigger beans
        Map<String, Trigger> triggerMap = applicationContext.getBeansOfType(Trigger.class);

        schedulerFactoryBean.setJobDetails(jobDetailMap.values().toArray(new JobDetail[0]));
        schedulerFactoryBean.setTriggers(triggerMap.values().toArray(new Trigger[0]));
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setTaskExecutor(threadPoolTaskExecutor);

        //-- set datasource only jdbc datasource is required
        // schedulerFactoryBean.setDataSource(dataSource);

        //-- set listeners when only scheduler debugging is required
        schedulerFactoryBean.setSchedulerListeners(new GlobalSchedulerListener());
        schedulerFactoryBean.setGlobalJobListeners(new GlobalJobListener());
        schedulerFactoryBean.setGlobalTriggerListeners(new GlobalTriggerListener());

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());
        schedulerFactoryBean.setQuartzProperties(properties);
        return schedulerFactoryBean;
    }

    @Bean
    public SmartLifecycle gracefulShutdownHookForQuartz(SchedulerFactoryBean schedulerFactoryBean) {
        return new SmartLifecycle() {

            private boolean isRunning = false;

            @Override
            public void start() {
                log.info("quartz graceful shutdown hook is registered");

                isRunning = true;
            }

            @Override
            public void stop() {
                isRunning = false;

                try {
                    log.info("quartz gracefull shutdown is started");

                    interruptJobs(schedulerFactoryBean);

                    schedulerFactoryBean.destroy();
                } catch (SchedulerException e) {
                    try {
                        log.info(
                                "error while shutting down quartz: " + e.getMessage(), e);
                        schedulerFactoryBean.getScheduler().shutdown(false);
                    } catch (SchedulerException ex) {
                        log.error("Unable to shutdown the Quartz scheduler.", ex);
                    }
                }
                log.info("quartz gracefull shutdown is completed");
            }

            @Override
            public void stop(Runnable callback) {
                log.info("spring container is shutting down");
                stop();
                callback.run();
            }

            @Override
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public boolean isRunning() {
                return isRunning;
            }

            @Override
            public int getPhase() {
                return Integer.MAX_VALUE;
            }
        };
    }

    private void interruptJobs(SchedulerFactoryBean schedulerFactoryBean) throws SchedulerException {

        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (JobExecutionContext jobExecutionContext : scheduler.getCurrentlyExecutingJobs()) {
            final JobDetail jobDetail = jobExecutionContext.getJobDetail();

            log.info("Interrupting job. [job.key : {}]", jobDetail.getKey());

            scheduler.interrupt(jobDetail.getKey());
        }
    }
}