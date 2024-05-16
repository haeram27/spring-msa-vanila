package com.example.quartz.config;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Configuration;

import com.example.quartz.scheduler.TestJob;
import com.example.quartz.scheduler.TestJobListener;
import com.example.quartz.scheduler.TestSchedulerListener;
import com.example.quartz.scheduler.TestTriggerListener;

import jakarta.annotation.PostConstruct;

@Configuration
public class SchedulerConfig {

    private Scheduler scheduler;

    public SchedulerConfig(Scheduler scheduler) {
        // use default quartz scheduler bean */
        this.scheduler = scheduler;
        if (this.scheduler != null) {
            try {
                this.scheduler.getListenerManager().addJobListener(new TestJobListener());
                this.scheduler.getListenerManager().addTriggerListener(new TestTriggerListener());
                this.scheduler.getListenerManager().addSchedulerListener(new TestSchedulerListener());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }

        // use user defined quartz scheduler bean */
        /*
        try {
            this.scheduler = new StdSchedulerFactory().getScheduler();
            this.scheduler.getListenerManager().addJobListener(new TestJobListener());
            this.scheduler.getListenerManager().addTriggerListener(new TestTriggerListener());
            this.scheduler.getListenerManager().addSchedulerListener(new TestSchedulerListener());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * add schedule
     */
    @PostConstruct
    private void registAllQuartzSchedule() throws SchedulerException {
        registSimpleSchedule();
        registCronSchedule();
    }

    /**
     * SimpleScheduler
     *
     * @throws SchedulerException
     */
    private void registSimpleSchedule() throws SchedulerException {
        // [STEP1] create job
        JobDetail job = JobBuilder.newJob(TestJob.class) // job builder
                .withIdentity("simpleSchedJob", "testJobGroup") // JobKey(job identifier)'s name and group
                .withDescription("simple sched job desc") // jobDetail's description
                .build();

        // [STEP2] create trigger
        Trigger trigger = TriggerBuilder.newTrigger() // trigger builder
                .withIdentity("simpleSchedTrigger", "testScheduleGroup") // trigger name and group
                .withDescription("simple trigger desc") // trigger description
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(5) // every 5 sec
                        .repeatForever()) // repeat infinitely
                .startNow()
                .build();

        // [STEP3] schedule Job and Trigger into scheduler
        scheduler.scheduleJob(job, trigger);
    }

    /**
     * CronScheduler
     */
    private void registCronSchedule() throws SchedulerException {

        // [STEP1] create job
        JobDetail job = JobBuilder.newJob(TestJob.class) // job builder
                .withIdentity("cronSchedJob", "testJobGroup") // JobKey(job identifier)'s name and group
                .withDescription("cron sched job desc") // jobDetail's description
                .build();

        // [STEP2] create trigger
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity("cronSchedTrigger", "testScheduleGroup") // trigger name and group
                .withDescription("cron trigger desc") // trigger description
                .withSchedule(CronScheduleBuilder.cronSchedule("0/10 * * * * ?"))
                .build(); // every 10 sec

        // [STEP3] schedule Job and Trigger into scheduler
        scheduler.scheduleJob(job, cronTrigger);
    }

}
