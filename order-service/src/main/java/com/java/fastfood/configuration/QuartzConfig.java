package com.java.fastfood.configuration;

import com.java.fastfood.job.OrderCleanupJob;
import com.java.fastfood.job.OrderProcessingJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail orderProcessingJobDetail() {
        return JobBuilder.newJob(OrderProcessingJob.class)
                .withIdentity("orderProcessingJob", "processingGroup")
                .usingJobData("targetStatus", "PLACED")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger orderProcessingTrigger(JobDetail orderProcessingJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(orderProcessingJobDetail)
                .withIdentity("orderProcessingTrigger", "processingGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?")
                        .inTimeZone(TimeZone.getTimeZone("Europe/Kyiv"))
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    @Bean
    public JobDetail orderCleanupJobDetail() {
        return JobBuilder.newJob(OrderCleanupJob.class)
                .withIdentity("orderCleanupJob", "cleanupGroup")
                .usingJobData("targetStatus", "DECLINED")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger orderCleanupTrigger(JobDetail orderCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(orderCleanupJobDetail)
                .withIdentity("orderCleanupTrigger", "cleanupGroup")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(5)
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithExistingCount())
                .build();
    }

    @Bean
    public Trigger weeklyDeliveredCleanupTrigger(JobDetail orderCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(orderCleanupJobDetail)
                .withIdentity("weeklyDeliveredCleanupTrigger", "cleanupGroup")
                .usingJobData("targetStatus", "COMPLETED")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 ? * SUN")
                        .inTimeZone(TimeZone.getTimeZone("Europe/Kyiv"))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}