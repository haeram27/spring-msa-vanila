package com.example.batch.config;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class QuartzConfig {
    private final DataSource dataSource;

    @Autowired
    QuartzProperties quartzProperties;

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(30);
        threadPoolTaskExecutor.setMaxPoolSize(30);
        threadPoolTaskExecutor.setQueueCapacity(30);
        threadPoolTaskExecutor.setThreadGroupName("batch-job-group-");
        threadPoolTaskExecutor.setThreadNamePrefix("batch-job-");
        // Queue가 full일 때 처리 정책 추가
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
    public Trigger[] registryTrigger(List<SimpleTriggerFactoryBean> simpleTriggerFactoryBeanList) {
        return simpleTriggerFactoryBeanList.stream().map(SimpleTriggerFactoryBean::getObject).toArray(Trigger[]::new);
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory, Trigger[] registryTrigger) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setTriggers(registryTrigger);
        schedulerFactoryBean.setTaskExecutor(threadPoolTaskExecutor());
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setDataSource(dataSource);

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
            public boolean isAutoStartup() {
                return true;
            }

            @Override
            public void stop(Runnable callback) {
                stop();

                log.info("Spring container is shutting down.");

                callback.run();
            }

            @Override
            public void start() {
                log.info("Quartz Graceful Shutdown Hook started.");

                isRunning = true;
            }

            @Override
            public void stop() {
                isRunning = false;

                try {
                    log.info("Quartz Graceful Shutdown... ");

                    interruptJobs(schedulerFactoryBean);

                    schedulerFactoryBean.destroy();
                } catch (SchedulerException e) {
                    try {
                        log.info(
                                "Error shutting down Quartz: " + e.getMessage(), e);
                        schedulerFactoryBean.getScheduler().shutdown(false);
                    } catch (SchedulerException ex) {
                        log.error("Unable to shutdown the Quartz scheduler.", ex);
                    }
                }
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