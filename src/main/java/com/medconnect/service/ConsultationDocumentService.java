package com.medconnect.service;

import com.medconnect.dto.ConsultationDocumentDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.ConsultationDocument;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.ConsultationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional; // <-- THÊM IMPORT

@Service
@RequiredArgsConstructor
public class ConsultationDocumentService {
    private final ConsultationDocumentRepository documentRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService; // Giả sử service này đã inject

    public void writeDocument(ConsultationDocumentDTO dto) {
        // Validate format...
        if (!validatePrescriptionFormat(dto.getContent())) {
            throw new RuntimeException("MSG25: Prescription does not meet regulations.");
        }

        // --- BẮT ĐẦU SỬA ---
        // Tìm xem document đã tồn tại chưa
        Optional<ConsultationDocument> existingDocOpt = documentRepository.findByAppointmentAppointmentId(dto.getAppointmentId());

        ConsultationDocument doc;
        boolean isNewDocument = false;

        if (existingDocOpt.isPresent()) {
            // Nếu có, lấy ra để cập nhật
            doc = existingDocOpt.get();
        } else {
            // Nếu không, tạo mới
            doc = new ConsultationDocument();
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Appointment: " + dto.getAppointmentId()));
            doc.setAppointment(appointment);
            isNewDocument = true; // Đánh dấu là tài liệu mới để gửi thông báo
        }

        // Cập nhật nội dung và loại từ DTO
        doc.setDocumentType(dto.getDocumentType());
        doc.setContent(dto.getContent());

        documentRepository.save(doc); // Lưu (hoặc cập nhật)
        // --- KẾT THÚC SỬA ---


        // Chỉ gửi thông báo nếu đây là lần đầu tiên tạo tài liệu
        if (isNewDocument) {
            // Đảm bảo appointment không null (vì doc cũ có thể không load appointment)
            Appointment appointment = doc.getAppointment();
            if (appointment == null) {
                appointment = appointmentRepository.findById(dto.getAppointmentId()).orElseThrow();
            }
            notificationService.sendPushNotification(appointment.getPatient().getUser(), "Tài liệu mới", "Bạn có tài liệu tư vấn mới.");
        }
    }

    private boolean validatePrescriptionFormat(String content) {
        // Logic check Ministry of Health regulations
        return true; // Demo
    }
}