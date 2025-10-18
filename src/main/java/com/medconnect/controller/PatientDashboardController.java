package com.medconnect.controller;

import com.medconnect.entity.Patient;
import com.medconnect.entity.User;
import com.medconnect.repository.PatientRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class PatientDashboardController {
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    @GetMapping("/patient-dashboard")
    public String showDashboard(Model model, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email = userDetails.getUsername();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user: " + currentUser.getEmail()));

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentService.getPatientAppointments(patient.getPatientId()));

        // THÊM DÒNG NÀY ĐỂ KHẮC PHỤC LỖI
        model.addAttribute("documents", Collections.emptyList());

        return "patient-dashboard";
    }
}