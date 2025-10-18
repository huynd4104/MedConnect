package com.medconnect.service;

import com.medconnect.dto.ConsultationDocumentDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.ConsultationDocument;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.ConsultationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultationDocumentService {
    private final ConsultationDocumentRepository documentRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public void writeDocument(ConsultationDocumentDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId()).orElseThrow();
        // Validate format according to BR-06
        if (!validatePrescriptionFormat(dto.getContent())) {
            throw new RuntimeException("MSG25: Prescription does not meet regulations.");
        }
        ConsultationDocument doc = new ConsultationDocument();
        doc.setAppointment(appointment);
        doc.setDocumentType(dto.getDocumentType());
        doc.setContent(dto.getContent());
        documentRepository.save(doc);

        // Notify patient
        notificationService.sendPushNotification(appointment.getPatient().getUser(), "New Document", "Consultation document available.");
    }

    private boolean validatePrescriptionFormat(String content) {
        // Logic check Ministry of Health regulations
        return true; // Demo
    }
}