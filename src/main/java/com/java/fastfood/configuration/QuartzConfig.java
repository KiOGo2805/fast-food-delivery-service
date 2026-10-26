package com.java.fastfood.configuration;

import com.java.fastfood.job.OrderCleanupJob;
import com.java.fastfood.job.OrderProcessingJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail orderProcessingJobDetail() {
        return JobBuilder.newJob(OrderProcessingJob.class)
                .withIdentity("orderProcessingJob")
                .withDescription("Job for processing PLACED orders")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger orderProcessingTrigger(JobDetail orderProcessingJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(orderProcessingJobDetail)
                .withIdentity("orderProcessingTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")) // Every minute
                .build();
    }

    @Bean
    public JobDetail orderCleanupJobDetail() {
        return JobBuilder.newJob(OrderCleanupJob.class)
                .withIdentity("orderCleanupJob")
                .withDescription("Job for cleaning up DECLINED orders")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger orderCleanupTrigger(JobDetail orderCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(orderCleanupJobDetail)
                .withIdentity("orderCleanupTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(5)
                        .repeatForever())
                .build();
    }
}
