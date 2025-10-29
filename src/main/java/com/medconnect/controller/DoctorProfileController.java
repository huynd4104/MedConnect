package com.medconnect.controller;

import com.medconnect.dto.DoctorProfileDTO;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.DoctorDocument;
import com.medconnect.entity.User;
import com.medconnect.repository.DoctorDocumentRepository;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.SpecializationRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.UserDetails; // <-- Đã xóa
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class DoctorProfileController {
    private final DoctorService doctorService;
    private final SpecializationRepository specializationRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorDocumentRepository doctorDocumentRepository;

    @GetMapping("/doctor-profile")
    public String showProfileForm(Model model, Authentication auth) {
        // 1. Lấy email bằng auth.getName()
        String userEmail = auth.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Optional<Doctor> doctorOpt = doctorRepository.findByUser(user);
        DoctorProfileDTO dto = new DoctorProfileDTO();
        List<DoctorDocument> documents = List.of();

        // Chuẩn bị biến cho status và reason
        String doctorStatus = null;
        String rejectionReason = null;

        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            dto.setFullName(doctor.getFullName());
            dto.setPhoneNumber(doctor.getPhoneNumber());
            dto.setClinicAddress(doctor.getClinicAddress());
            dto.setExperienceYears(doctor.getExperienceYears());
            dto.setLicenseNumber(doctor.getLicenseNumber());
            dto.setSpecializationId(doctor.getSpecialization() != null ? doctor.getSpecialization().getSpecializationId() : null);

            dto.setExistingPhotoPath(doctor.getPhotoPath());

            doctorStatus = doctor.getStatus().name();
            rejectionReason = doctor.getRejectionReason();

            documents = doctorDocumentRepository.findByDoctorDoctorId(doctor.getDoctorId());
        }

        dto.setExistingDocuments(documents);
        model.addAttribute("doctorProfileDTO", dto);

        // Thêm các thuộc tính status và reason riêng biệt cho view
        model.addAttribute("doctorStatus", doctorStatus);
        model.addAttribute("rejectionReason", rejectionReason);

        model.addAttribute("specializations", specializationRepository.findAll());
        return "doctor-profile";
    }

    @PostMapping("/doctor-profile")
    public String submitProfile(@ModelAttribute DoctorProfileDTO dto,
                                BindingResult bindingResult,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ.");
            return "redirect:/doctor-profile";
        }
        try {
            // Lấy email (Đã sửa từ trước)
            String userEmail = auth.getName();
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

            Integer userId = user.getUserId();
            doctorService.completeProfile(userId, dto);
            redirectAttributes.addFlashAttribute("success", "Profile submitted successfully. Please wait for approval.");
            return "redirect:/doctor-dashboard";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "MSG12: File upload failed.");
            return "redirect:/doctor-profile";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "MSG11: Submission failed. " + e.getMessage());
            return "redirect:/doctor-profile";
        }
    }

    @PostMapping("/delete-document/{id}")
    public String deleteDocument(@PathVariable("id") Integer documentId, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDocument(documentId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tài liệu thành công.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: Không thể xóa file vật lý.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa tài liệu.");
        }
        return "redirect:/doctor-profile";
    }
}