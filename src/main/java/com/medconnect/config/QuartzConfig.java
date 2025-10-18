package com.medconnect.config;

import com.medconnect.job.AppointmentReminderJob;
import com.medconnect.job.ArchiveAppointmentsJob;
import com.medconnect.job.ExpireUnpaidBookingsJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail appointmentReminderJobDetail() {
        return JobBuilder.newJob(AppointmentReminderJob.class)
                .withIdentity("appointmentReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger appointmentReminderTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(appointmentReminderJobDetail())
                .withIdentity("appointmentReminderTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?")) // Chạy mỗi ngày 8h sáng
                .build();
    }

    @Bean
    public JobDetail archiveAppointmentsJobDetail() {
        return JobBuilder.newJob(ArchiveAppointmentsJob.class)
                .withIdentity("archiveAppointmentsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger archiveAppointmentsTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(archiveAppointmentsJobDetail())
                .withIdentity("archiveAppointmentsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 1 * ?")) // Chạy ngày 1 hàng tháng
                .build();
    }

    @Bean
    public JobDetail expireUnpaidBookingsJobDetail() {
        return JobBuilder.newJob(ExpireUnpaidBookingsJob.class)
                .withIdentity("expireUnpaidBookingsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger expireUnpaidBookingsTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(expireUnpaidBookingsJobDetail())
                .withIdentity("expireUnpaidBookingsTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 */15 * * * ?")) // Chạy mỗi 15 phút
                .build();
    }
}