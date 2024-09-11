package com.example.batch.job;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
    private AtomicBoolean singleStepBatchJobLauncherEnabled = new AtomicBoolean(false);
    private AtomicBoolean multiStepBatchJobLauncherEnabled = new AtomicBoolean(false);
    private AtomicBoolean flowStepBatchJobLauncherEnabled = new AtomicBoolean(true);
    private AtomicInteger batchRunCounter = new AtomicInteger(0);


    private final Job singleStepJob;
    private final Job multiStepJob;
    private final Job flowStepJob;
    private final JobLauncher jobLauncher;

    @Scheduled(fixedRate = 2000)
    public void singleStepBatchJobLauncher() throws Exception {
        Date date = new Date();
        System.out.println("scheduler starts at " + date);
        if (singleStepBatchJobLauncherEnabled.get()) {
            JobExecution jobExecution = jobLauncher.run(singleStepJob,
                    new JobParametersBuilder().addDate("launchDate", date).toJobParameters());
            batchRunCounter.incrementAndGet();
            System.out.println("Batch job ends with status as " + jobExecution.getStatus());
        }
        System.out.println("scheduler ends ");
    }

    @Scheduled(fixedRate = 3000)
    public void multiStepBatchJobLauncher() throws Exception {
        Date date = new Date();
        System.out.println("scheduler starts at " + date);
        if (multiStepBatchJobLauncherEnabled.get()) {
            JobExecution jobExecution = jobLauncher.run(multiStepJob,
                    new JobParametersBuilder().addDate("launchDate", date).toJobParameters());
            batchRunCounter.incrementAndGet();
            System.out.println("Batch job ends with status as " + jobExecution.getStatus());
        }
        System.out.println("scheduler ends ");
    }

    @Scheduled(fixedRate = 3000)
    public void flowStepBatchJobLauncher() throws Exception {
        Date date = new Date();
        System.out.println("scheduler starts at " + date);
        if (flowStepBatchJobLauncherEnabled.get()) {
            JobExecution jobExecution = jobLauncher.run(flowStepJob,
                    new JobParametersBuilder().addDate("launchDate", date).toJobParameters());
            batchRunCounter.incrementAndGet();
            System.out.println("Batch job ends with status as " + jobExecution.getStatus());
        }
        System.out.println("scheduler ends ");
    }

    /* test */
    // @Scheduled(fixedRate = 2000)
    // public void print() throws Exception {
    //     long localTimestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    //     System.out.println(LocalDateTime.ofInstant(Instant.ofEpochMilli(localTimestamp), ZoneId.systemDefault()));
    // }
}
