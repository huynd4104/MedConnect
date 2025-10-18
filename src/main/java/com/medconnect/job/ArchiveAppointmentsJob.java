package com.medconnect.job;

import com.medconnect.repository.AppointmentRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ArchiveAppointmentsJob implements Job {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        appointmentRepository.findAppointmentsToArchive(threshold).forEach(appointment -> {
            // Move to archive table or mark as archived
            // Demo: delete or update
        });
    }
}