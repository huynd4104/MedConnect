package com.medconnect.controller;

import com.medconnect.dto.AppointmentDTO;
import com.medconnect.entity.*;
import com.medconnect.repository.*;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookAppointmentController {
    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    @GetMapping("/book-appointment")
    public String showBookForm(@RequestParam("doctorId") Integer doctorId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

            // Lấy danh sách lịch làm việc (chỉ lấy các lịch đang active)
            List<Schedule> schedules = scheduleRepository.findByDoctor_DoctorIdAndActiveTrue(doctorId);

            AppointmentDTO dto = new AppointmentDTO();
            dto.setDoctorId(doctorId);

            model.addAttribute("doctor", doctor);
            model.addAttribute("appointmentDTO", dto);
            model.addAttribute("schedules", schedules); // <-- THÊM DÒNG NÀY

            return "book-appointment";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute AppointmentDTO dto, BindingResult result, Authentication auth, RedirectAttributes redirectAttributes) {

        String doctorIdParam = "?doctorId=" + dto.getDoctorId();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "MSG21: Time slot taken.");
            return "redirect:/book-appointment" + doctorIdParam;
        }

        try {
            // 1. Lấy User (bạn đã sửa ở bước trước)
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email).orElseThrow();

            Appointment savedAppointment = appointmentService.bookAppointment(currentUser, dto);

            // 3. Chuyển hướng đến /payment KÈM THEO ID
            return "redirect:/payment?appointmentId=" + savedAppointment.getAppointmentId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/book-appointment" + doctorIdParam;
        }
    }

    @PostMapping("/cancel-appointment")
    public String cancelAppointment(@RequestParam("appointmentId") Integer appointmentId,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy thông tin bệnh nhân đang đăng nhập
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            Patient currentPatient = patientRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

            // 2. (Bảo mật) Kiểm tra xem lịch hẹn này có phải của họ không
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

            if (!appointment.getPatient().getPatientId().equals(currentPatient.getPatientId())) {
                redirectAttributes.addFlashAttribute("error", "Lỗi: Bạn không có quyền hủy lịch hẹn này.");
                return "redirect:/patient-dashboard";
            }

            // 3. Gọi service để hủy (true = byPatient)
            appointmentService.cancelAppointment(appointmentId, true);

            redirectAttributes.addFlashAttribute("success", "Đã hủy lịch hẹn thành công.");
            return "redirect:/patient-dashboard";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/patient-dashboard";
        }
    }
}