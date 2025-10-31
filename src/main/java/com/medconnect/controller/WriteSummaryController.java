package com.medconnect.controller;

import com.medconnect.dto.ConsultationDocumentDTO;
import com.medconnect.service.ConsultationDocumentService;
import com.medconnect.entity.Appointment;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.entity.ConsultationDocument;
import com.medconnect.repository.ConsultationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class WriteSummaryController {
    private final ConsultationDocumentService documentService;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationDocumentRepository documentRepository;

    @GetMapping("/write-summary/{appointmentId}")
    public String showForm(@PathVariable("appointmentId") Integer appointmentId, Model model) {

        // 1. Tìm Appointment (để lấy thông tin bệnh nhân)
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Appointment với ID: " + appointmentId));
        model.addAttribute("appointment", appointment);

        // 2. Tìm tài liệu cũ nếu có
        Optional<ConsultationDocument> docOptional = documentRepository.findByAppointmentAppointmentId(appointmentId);

        ConsultationDocumentDTO dto;
        if (docOptional.isPresent()) {
            // Nếu đã tồn tại, lấy dữ liệu cũ đổ vào DTO
            ConsultationDocument doc = docOptional.get();
            dto = new ConsultationDocumentDTO();
            dto.setAppointmentId(doc.getAppointment().getAppointmentId());
            dto.setDocumentType(doc.getDocumentType());
            dto.setContent(doc.getContent());
        } else {
            // Nếu chưa, tạo DTO mới và chỉ gán ID
            dto = new ConsultationDocumentDTO();
            dto.setAppointmentId(appointmentId);
        }
        // 3. Đưa DTO vào model
        model.addAttribute("consultationDocumentDTO", dto);

        return "write-summary";
    }

    @PostMapping("/write-summary")
    public String writeDocument(@ModelAttribute ConsultationDocumentDTO dto, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "MSG25: Prescription does not meet regulations.");
            return "redirect:/write-summary/" + dto.getAppointmentId();
        }
        try {
            documentService.writeDocument(dto);
            redirectAttributes.addFlashAttribute("success", "Lưu tài liệu thành công.");
            return "redirect:/doctor-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/write-summary/" + dto.getAppointmentId();
        }
    }
}