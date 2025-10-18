package com.medconnect.controller;

import com.medconnect.service.DoctorService;
import com.medconnect.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final DoctorService doctorService;
    private final PatientService patientService;

    // Use case 1: Hiển thị danh sách tất cả bác sĩ
    @GetMapping("/admin-doctor-list")
    public String showDoctorList(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin-doctor-list";
    }

    // Use case 2: Hiển thị danh sách tất cả bệnh nhân
    @GetMapping("/admin-patient-list")
    public String showPatientList(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "admin-patient-list";
    }
}