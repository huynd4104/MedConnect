package com.medconnect.service;

import com.medconnect.dto.ConsultationDocumentDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.ConsultationDocument;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.ConsultationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsultationDocumentService {
    private final ConsultationDocumentRepository documentRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public void writeDocument(ConsultationDocumentDTO dto) {
        if (!validatePrescriptionFormat(dto.getContent())) {
            throw new RuntimeException("MSG25: Prescription does not meet regulations.");
        }

        // xem document đã tồn tại chưa
        Optional<ConsultationDocument> existingDocOpt = documentRepository.findByAppointmentAppointmentId(dto.getAppointmentId());

        ConsultationDocument doc;

        if (existingDocOpt.isPresent()) {
            doc = existingDocOpt.get();
        } else {
            doc = new ConsultationDocument();
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Appointment: " + dto.getAppointmentId()));
            doc.setAppointment(appointment);
        }

        doc.setDocumentType(dto.getDocumentType());
        doc.setContent(dto.getContent());

        documentRepository.save(doc);

        Appointment appointment = doc.getAppointment();
        if (appointment == null) {
            appointment = appointmentRepository.findById(dto.getAppointmentId()).orElseThrow();
            doc.setAppointment(appointment);
        }

        String notificationTitle = doc.getDocumentType() == ConsultationDocument.DocumentType.Prescription
                ? "New Prescription"
                : "New Summary";
        String notificationMessage = doc.getDocumentType() == ConsultationDocument.DocumentType.Prescription
                ? "Bác sĩ đã cập nhật đơn thuốc cho bạn."
                : "Bác sĩ đã cập nhật tóm tắt tư vấn cho bạn.";

        // Gửi thông báo cho bệnh nhân
        notificationService.sendPushNotification(
                appointment.getPatient().getUser(),
                notificationTitle,
                notificationMessage
        );
    }

    private boolean validatePrescriptionFormat(String content) {
        // Logic check Ministry of Health regulations
        return true;
    }
}