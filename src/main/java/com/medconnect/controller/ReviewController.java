package com.medconnect.controller;

import com.medconnect.dto.ReviewDTO;
import com.medconnect.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/review")
    public String showReviewForm(Model model) {
        model.addAttribute("reviewDTO", new ReviewDTO());
        return "review";
    }

    @PostMapping("/review")
    public String leaveReview(@ModelAttribute ReviewDTO dto, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "MSG28: Review contains invalid content.");
            return "redirect:/review";
        }
        try {
            reviewService.leaveReview(dto);
            redirectAttributes.addFlashAttribute("success", "Review submitted.");
            return "redirect:/patient-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/review";
        }
    }
}