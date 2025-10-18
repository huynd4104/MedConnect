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
import org.springframework.security.core.userdetails.UserDetails;
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
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Optional<Doctor> existingDoctorOpt = doctorRepository.findByUser(currentUser);

        DoctorProfileDTO dto = new DoctorProfileDTO();
        if (existingDoctorOpt.isPresent()) {
            Doctor doctor = existingDoctorOpt.get();
            // Map dữ liệu cũ
            dto.setExperienceYears(doctor.getExperienceYears());
            dto.setLicenseNumber(doctor.getLicenseNumber());
            if (doctor.getSpecialization() != null) {
                dto.setSpecializationId(doctor.getSpecialization().getSpecializationId());
            }

            // >> 3. LẤY VÀ SET ĐƯỜNG DẪN ẢNH, GIẤY TỜ
            dto.setExistingPhotoPath(doctor.getPhotoPath());
            List<DoctorDocument> documents = doctorDocumentRepository.findByDoctorDoctorId(doctor.getDoctorId());
            dto.setExistingDocuments(documents);

        }

        model.addAttribute("doctorProfileDTO", dto);
        model.addAttribute("specializations", specializationRepository.findAll());
        return "doctor-profile";
    }


    @PostMapping("/doctor-profile")
    public String completeProfile(@ModelAttribute("doctorProfileDTO") DoctorProfileDTO dto, BindingResult result, Authentication auth, RedirectAttributes redirectAttributes) {
        // Giữ lại phần kiểm tra lỗi validation nếu cần
        if (result.hasErrors()) {
            // Thay vì redirect, tốt hơn là trả về view với lỗi
            // Nhưng theo logic hiện tại, ta tạm giữ redirect
            redirectAttributes.addFlashAttribute("error", "MSG09: All fields are required.");
            return "redirect:/doctor-profile";
        }
        try {
            // Lấy principal từ Spring Security
            Object principal = auth.getPrincipal();
            String userEmail;

            if (principal instanceof UserDetails) {
                userEmail = ((UserDetails) principal).getUsername();
            } else {
                userEmail = principal.toString();
            }

            // Dùng email để tìm User entity trong database
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