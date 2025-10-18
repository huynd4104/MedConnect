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

    public void leaveReview(ReviewDTO dto) {
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId()).orElseThrow();
        if (appointment.getStatus() != Appointment.Status.Completed) {
            throw new RuntimeException("MSG27: Cannot review until consultation is complete.");
        }
        // Validate content BR-09 (no PII)
        if (containsPII(dto.getComment())) {
            throw new RuntimeException("MSG28: Review contains invalid content.");
        }
        Review review = new Review();
        review.setAppointment(appointment);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setAnonymous(dto.getAnonymous());
        reviewRepository.save(review);
    }

    private boolean containsPII(String comment) {
        // Logic check PII
        return false; // Demo
    }
}