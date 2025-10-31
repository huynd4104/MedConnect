package com.medconnect.controller;

import com.medconnect.entity.Doctor;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.ReviewRepository;
import com.medconnect.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class DoctorProfileViewController {
    private final DoctorRepository doctorRepository;
    private final ReviewRepository reviewRepository;
    private final ScheduleRepository scheduleRepository;

    @GetMapping("/doctor-profile-view/{id}")
    public String viewDoctorProfile(@PathVariable Integer id, Model model) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow();
        if (doctor.getStatus() != Doctor.Status.Approved) {
            model.addAttribute("error", "MSG20: Profile unavailable.");
            return "error";
        }
        model.addAttribute("doctor", doctor);
        model.addAttribute("reviews", reviewRepository.findByDoctorId(id));
        model.addAttribute("schedules", scheduleRepository.findByDoctor_DoctorIdAndActiveTrue(id));
        return "doctor-profile-view";
    }
}