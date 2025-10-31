package com.medconnect.controller;

import com.medconnect.dto.ScheduleDTO;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.Schedule; // Đảm bảo đã import
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.ReviewRepository;
import com.medconnect.repository.ScheduleRepository;
import com.medconnect.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DoctorProfileViewController {
    private final DoctorRepository doctorRepository;
    private final ReviewRepository reviewRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;

    @GetMapping("/doctor-profile-view/{id}")
    public String viewDoctorProfile(@PathVariable Integer id, Model model) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow();
        if (doctor.getStatus() != Doctor.Status.Approved) {
            model.addAttribute("error", "MSG20: Profile unavailable.");
            return "error";
        }
        model.addAttribute("doctor", doctor);
        model.addAttribute("reviews", reviewRepository.findByDoctorId(id));

        model.addAttribute("doctorId", doctor.getDoctorId());

        return "doctor-profile-view";
    }
    /**
     * API Public để lấy lịch (dạng DTO 30 phút) cho FullCalendar
     */
    @GetMapping("/api/public/schedules/{doctorId}")
    @ResponseBody
    public ResponseEntity<List<ScheduleDTO>> getPublicDoctorSchedules(@PathVariable Integer doctorId) {
        try {
            // 1. Lấy doctorId
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            // 2. Lấy userId từ doctor
            Integer userId = doctor.getUser().getUserId();

            // 3. Gọi service hiện có (getSchedulesByUserId)
            // Service này đã trả về List<ScheduleDTO> (dạng 30 phút)
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByUserId(userId);

            return ResponseEntity.ok(schedules);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}