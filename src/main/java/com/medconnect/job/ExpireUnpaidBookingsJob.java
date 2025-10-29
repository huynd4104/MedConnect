package com.medconnect.job;

import com.medconnect.entity.Appointment;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.service.NotificationService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ExpireUnpaidBookingsJob implements Job {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDateTime start = LocalDateTime.now().minusMinutes(15);
        LocalDateTime end = LocalDateTime.now();
        appointmentRepository.findUnpaidAppointments(start, end).forEach(appointment -> {
            appointment.setStatus(Appointment.Status.Cancelled);
            appointmentRepository.save(appointment);
            notificationService.sendPushNotification(appointment.getPatient().getUser(), "Expired Booking", "Đặt chỗ của bạn đã hết hạn do chưa thanh toán.");
        });
    }
}