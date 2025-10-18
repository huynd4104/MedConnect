package com.medconnect.controller;

import com.medconnect.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminDoctorApprovalController {
    private final DoctorService doctorService;

    @GetMapping("/admin-doctor-approval")
    public String showPendingDoctors(Model model) {
        model.addAttribute("pendingDoctors", doctorService.getPendingDoctors());
        return "admin-doctor-approval";
    }

    @PostMapping("/admin-approve-doctor")
    public String approveDoctor(@RequestParam Integer doctorId, @RequestParam boolean approve, @RequestParam(required = false) String reason) {
        doctorService.approveDoctor(doctorId, approve, reason);
        return "redirect:/admin-doctor-approval";
    }
}