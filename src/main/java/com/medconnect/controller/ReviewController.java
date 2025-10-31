package com.medconnect.controller;

// Đảm bảo bạn đã import đầy đủ các class này
import com.medconnect.dto.ReviewDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.Review;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.ReviewRepository;
import com.medconnect.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/review")
    public String showReviewForm(@RequestParam("appointmentId") Integer appointmentId, Model model, RedirectAttributes redirectAttributes) {

        try {
            // 1. Lấy thông tin lịch hẹn
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn: " + appointmentId));
            model.addAttribute("appointment", appointment);

            // 2. Kiểm tra xem có phải đang redirect từ lỗi POST không
            if (!model.containsAttribute("reviewDTO")) {

                // 3. Tìm xem đã có đánh giá cũ chưa
                Optional<Review> existingReview = reviewRepository.findByAppointmentAppointmentId(appointmentId);

                ReviewDTO reviewDTO;
                if (existingReview.isPresent()) {
                    // Nếu có: Chuyển Entity (Review) thành DTO
                    Review review = existingReview.get();
                    reviewDTO = new ReviewDTO();

                    // Lấy ID từ param
                    reviewDTO.setAppointmentId(appointmentId);

                    // Load data cũ
                    reviewDTO.setRating(review.getRating());
                    reviewDTO.setComment(review.getComment());
                    reviewDTO.setAnonymous(review.getAnonymous());
                } else {
                    // Nếu không: Tạo DTO rỗng
                    reviewDTO = new ReviewDTO();
                    reviewDTO.setAppointmentId(appointmentId);
                }
                model.addAttribute("reviewDTO", reviewDTO);
            }

            return "review";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải trang đánh giá: " + e.getMessage());
            return "redirect:/patient-dashboard";
        }
    }

    @PostMapping("/review")
    public String leaveReview(@ModelAttribute ReviewDTO dto, BindingResult result, RedirectAttributes redirectAttributes) {

        String redirectUrl = "redirect:/review?appointmentId=" + dto.getAppointmentId();

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "MSG28: Review contains invalid content.");
            redirectAttributes.addFlashAttribute("reviewDTO", dto);
            return redirectUrl;
        }
        try {
            reviewService.leaveReview(dto);
            redirectAttributes.addFlashAttribute("success", "Review submitted.");
            return "redirect:/patient-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("reviewDTO", dto);
            return redirectUrl;
        }
    }
}