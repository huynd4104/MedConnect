package com.medconnect.controller;

import com.medconnect.entity.DoctorDocument;
import com.medconnect.repository.DoctorDocumentRepository;
import com.medconnect.service.DoctorService;
import com.medconnect.service.PatientService;
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
public class AdminController {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final DoctorDocumentRepository doctorDocumentRepository;

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

    /**
     * API endpoint cho Admin để lấy giấy tờ của một bác sĩ cụ thể.
     */
    @GetMapping("/api/admin/doctors/{doctorId}/documents")
    @ResponseBody
    public ResponseEntity<List<DoctorDocument>> getDoctorDocuments(@PathVariable Integer doctorId) {
        try {
            // Sử dụng repository có sẵn để tìm tài liệu
            List<DoctorDocument> documents = doctorDocumentRepository.findByDoctorDoctorId(doctorId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}