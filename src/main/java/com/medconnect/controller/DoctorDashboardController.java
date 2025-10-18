package com.medconnect.controller;

import com.medconnect.entity.Doctor;
import com.medconnect.entity.User;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DoctorDashboardController {
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @GetMapping("/doctor-dashboard")
    public String showDashboard(Model model, Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email = userDetails.getUsername();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // SỬA LẠI LOGIC TÌM KIẾM DOCTOR
        Doctor doctor = doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + currentUser.getEmail()));

        model.addAttribute("doctor", doctor);
        // Dùng doctorId từ đối tượng doctor đã tìm thấy
        model.addAttribute("appointments", appointmentService.getDoctorAppointments(doctor.getDoctorId()));

        return "doctor-dashboard";
    }
}