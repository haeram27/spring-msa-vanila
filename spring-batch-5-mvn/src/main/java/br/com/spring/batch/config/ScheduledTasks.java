package br.com.spring.batch.config;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private AtomicBoolean enabled = new AtomicBoolean(true);
    private AtomicInteger batchRunCounter = new AtomicInteger(0);

    @Scheduled(fixedRate = 2000)
    public void launchJob(JobLauncher jobLauncher, Job singleStepjob) throws Exception {
        Date date = new Date();
        System.out.println("scheduler starts at " + date);
        if (enabled.get()) {
            JobExecution jobExecution = jobLauncher.run(singleStepjob,
                    new JobParametersBuilder().addDate("launchDate", date).toJobParameters());
            batchRunCounter.incrementAndGet();
            System.out.println("Batch job ends with status as " + jobExecution.getStatus());
        }
        System.out.println("scheduler ends ");
    }

}
