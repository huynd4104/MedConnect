package com.medconnect.controller;

import com.medconnect.entity.ConsultationDocument;
import com.medconnect.entity.Patient;
import com.medconnect.entity.User;
import com.medconnect.repository.ConsultationDocumentRepository;
import com.medconnect.repository.PatientRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PatientDashboardController {
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final ConsultationDocumentRepository documentRepository;

    @GetMapping("/patient-dashboard")
    public String showDashboard(Model model, Authentication auth) {
        // Lấy email trực tiếp từ Authentication, hoạt động cho cả form login và OAuth2
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Patient profile not found for user: " + currentUser.getEmail()));

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", appointmentService.getPatientAppointments(patient.getPatientId()));

        List<ConsultationDocument> documents = documentRepository.findByAppointmentPatientPatientId(patient.getPatientId());
        model.addAttribute("documents", documents);

        return "patient-dashboard";
    }
}