package com.medconnect.controller;

import com.medconnect.dto.AppointmentDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.Schedule;
import com.medconnect.entity.User;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.ScheduleRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping("/book-appointment")
    public String showBookForm(@RequestParam("doctorId") Integer doctorId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ"));

            // *** BẮT ĐẦU THÊM MỚI ***
            // Lấy danh sách lịch làm việc (chỉ lấy các lịch đang active)
            List<Schedule> schedules = scheduleRepository.findByDoctor_DoctorIdAndActiveTrue(doctorId);
            // *** KẾT THÚC THÊM MỚI ***

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
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

            // 2. SỬA LẠI CHỖ NÀY
            // Thay vì gọi "void", hãy nhận lại đối tượng Appointment
            Appointment savedAppointment = appointmentService.bookAppointment(currentUser, dto);

            // 3. Chuyển hướng đến /payment KÈM THEO ID
            return "redirect:/payment?appointmentId=" + savedAppointment.getAppointmentId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/book-appointment" + doctorIdParam;
        }
    }
}