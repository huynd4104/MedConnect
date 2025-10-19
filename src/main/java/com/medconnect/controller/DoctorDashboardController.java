package com.medconnect.controller;

import com.medconnect.entity.Appointment;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.User;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class DoctorDashboardController {
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/doctor-dashboard")
    public String showDashboard(Model model, Authentication auth) {
        // Lấy email trực tiếp từ Authentication, hoạt động cho cả form login và OAuth2
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Doctor doctor = doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + currentUser.getEmail()));

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointmentService.getDoctorAppointments(doctor.getDoctorId()));

        return "doctor-dashboard";
    }

    @PostMapping("/doctor-dashboard/complete/{id}")
    public String completeAppointment(@PathVariable("id") Integer appointmentId,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy thông tin bác sĩ đang đăng nhập
            String email = auth.getName(); // Lấy email trực tiếp
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng."));

            Doctor doctor = doctorRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bác sĩ."));

            // 2. (Bảo mật) Kiểm tra xem lịch hẹn này có phải của bác sĩ không
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn."));

            if (!appointment.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thao tác trên lịch hẹn này.");
                return "redirect:/doctor-dashboard";
            }

            // 3. Gọi service để hoàn thành
            appointmentService.completeAppointment(appointmentId);
            redirectAttributes.addFlashAttribute("success", "Đã hoàn thành lịch hẹn.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/doctor-dashboard";
    }
}