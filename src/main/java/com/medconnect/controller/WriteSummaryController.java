package com.medconnect.controller;

import com.medconnect.dto.ConsultationDocumentDTO;
import com.medconnect.service.ConsultationDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WriteSummaryController {
    private final ConsultationDocumentService documentService;

    @GetMapping("/write-summary")
    public String showForm(Model model) {
        model.addAttribute("consultationDocumentDTO", new ConsultationDocumentDTO());
        return "write-summary";
    }

    @PostMapping("/write-summary")
    public String writeDocument(@ModelAttribute ConsultationDocumentDTO dto, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "MSG25: Prescription does not meet regulations.");
            return "redirect:/write-summary";
        }
        try {
            documentService.writeDocument(dto);
            redirectAttributes.addFlashAttribute("success", "Document saved.");
            return "redirect:/doctor-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/write-summary";
        }
    }
}