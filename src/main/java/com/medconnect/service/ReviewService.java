package com.medconnect.service;

import com.medconnect.dto.ReviewDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.Review;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public void leaveReview(ReviewDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn."));

        if (appointment.getStatus() != Appointment.Status.Completed) {
            throw new RuntimeException("MSG27: Cannot review until consultation is complete.");
        }

        if (containsPII(dto.getComment())) {
            throw new RuntimeException("MSG28: Review contains invalid content.");
        }

        // 1. Tìm review cũ (nếu có) bằng appointmentId, nếu không thấy thì tạo mới
        Review review = reviewRepository.findByAppointmentAppointmentId(dto.getAppointmentId())
                .orElse(new Review());

        // 2. Nếu là review mới, set Appointment cho nó
        if (review.getAppointment() == null) {
            review.setAppointment(appointment);
        }

        // 3. Cập nhật nội dung từ DTO
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setAnonymous(dto.getAnonymous());

        // 4. Lưu (JPA sẽ tự biết đây là update hay insert)
        Review savedReview = reviewRepository.save(review);

        // --- GỬI THÔNG BÁO CHO BÁC SĨ ---
        // 5. Lấy thông tin bác sĩ từ cuộc hẹn
        Appointment apptForNotification = savedReview.getAppointment();
        if (apptForNotification == null) {
            apptForNotification = appointmentRepository.findById(dto.getAppointmentId()).orElseThrow();
        }

        // Tạo nội dung thông báo
        String patientName = dto.getAnonymous() ? "Một bệnh nhân ẩn danh" : apptForNotification.getPatient().getFullName();
        String notificationMessage = patientName + " đã để lại đánh giá " + dto.getRating() + " sao cho cuộc hẹn.";

        // Gửi thông báo
        notificationService.sendPushNotification(
                apptForNotification.getDoctor().getUser(),
                "New Review",
                notificationMessage
        );
    }

    private boolean containsPII(String comment) {
        // Logic check PII
        return false; // Demo
    }
}