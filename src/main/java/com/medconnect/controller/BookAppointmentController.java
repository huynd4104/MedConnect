package com.medconnect.controller;

import com.medconnect.dto.AppointmentDTO;
import com.medconnect.entity.*;
import com.medconnect.repository.*;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            schedules.sort(Comparator.comparing(Schedule::getStartTime));

            AppointmentDTO dto = new AppointmentDTO();
            dto.setDoctorId(doctorId);

            model.addAttribute("doctor", doctor);
            model.addAttribute("appointmentDTO", dto);
            model.addAttribute("schedules", schedules);

            return "book-appointment";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * API Endpoint để lấy các slot trống
     */
    @GetMapping("/api/appointments/available-slots")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getAvailableSlots(
            @RequestParam("doctorId") Integer doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("type") Appointment.ConsultationType type) {
        try {
            Map<String, String> slots = appointmentService.getAvailableSlots(doctorId, date, type);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute AppointmentDTO dto, BindingResult result, Authentication auth, RedirectAttributes redirectAttributes) {

        String doctorIdParam = "?doctorId=" + dto.getDoctorId();

        // SỬA LẠI: Kiểm tra lỗi validation DTO mới
        if (result.hasErrors()) {
            // Lấy lỗi cụ thể
            String errorMessage = result.getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + errorMessage);
            return "redirect:/book-appointment" + doctorIdParam;
        }

        try {
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email).orElseThrow();

            Appointment savedAppointment = appointmentService.bookAppointment(currentUser, dto);

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