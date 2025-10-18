package com.medconnect.job;

import com.medconnect.repository.AppointmentRepository;
import com.medconnect.service.NotificationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AppointmentReminderJob implements Job {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDateTime start = LocalDateTime.now().plusHours(24).minusMinutes(30);
        LocalDateTime end = LocalDateTime.now().plusHours(24).plusMinutes(30);
        appointmentRepository.findConfirmedAppointmentsForReminder(start, end).forEach(appointment -> {
            notificationService.sendPushNotification(appointment.getPatient().getUser(), "Reminder", "Upcoming appointment in 24 hours.");
            notificationService.sendPushNotification(appointment.getDoctor().getUser(), "Reminder", "Upcoming appointment in 24 hours.");
        });
    }
}