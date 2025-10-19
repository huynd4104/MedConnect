package com.medconnect.controller;

import com.medconnect.dto.PatientProfileDTO;
import com.medconnect.entity.Patient;
import com.medconnect.entity.User;
import com.medconnect.repository.PatientRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.AppointmentService;
import com.medconnect.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PatientProfileController {
    private final PatientService patientService;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentService appointmentService;

    @GetMapping("/patient-profile")
    public String showProfileForm(Model model, Authentication auth) {
        // Lấy thông tin người dùng hiện tại
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        // Tạo DTO và điền thông tin có sẵn của bệnh nhân
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setFullName(patient.getFullName());
        dto.setPhoneNumber(patient.getPhoneNumber());
        dto.setAddress(patient.getAddress());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setMedicalHistory(patient.getMedicalHistory());

        // Thêm DTO và danh sách appointments vào model
        model.addAttribute("patientProfileDTO", dto);
        model.addAttribute("appointments", appointmentService.getPatientAppointments(patient.getPatientId())); // <-- SỬA LỖI TẠI ĐÂY

        return "patient-profile";
    }

    @PostMapping("/patient-profile")
    public String updateProfile(@ModelAttribute("patientProfileDTO") PatientProfileDTO dto, BindingResult result, Authentication auth, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "MSG12: Invalid input format.");
            return "redirect:/patient-profile";
        }
        try {
            // Lấy patientId một cách an toàn hơn
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Patient patient = patientRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Patient profile not found"));

            patientService.updateProfile(patient.getPatientId(), dto);
            redirectAttributes.addFlashAttribute("success", "Profile updated.");
            return "redirect:/patient-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Update failed: " + e.getMessage());
            return "redirect:/patient-profile";
        }
    }
}