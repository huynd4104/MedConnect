package com.medconnect.controller;

import com.medconnect.dto.SpecializationDTO;
import com.medconnect.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminSpecializationsController {
    private final AdminService adminService;

    @GetMapping("/admin-specializations")
    public String showSpecializations(Model model) {
        model.addAttribute("specializations", adminService.getSpecializations());
        model.addAttribute("specializationDTO", new SpecializationDTO());
        return "admin-specializations";
    }

    @PostMapping("/admin-add-specialization")
    public String addSpecialization(@ModelAttribute SpecializationDTO dto, RedirectAttributes redirectAttributes) {
        try {
            adminService.addSpecialization(dto);
            redirectAttributes.addFlashAttribute("success", "Specialization added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-specializations";
    }

    @PostMapping("/admin-update-specialization")
    public String updateSpecialization(@RequestParam Integer id, @ModelAttribute SpecializationDTO dto) {
        adminService.updateSpecialization(id, dto);
        return "redirect:/admin-specializations";
    }

    @PostMapping("/admin-delete-specialization")
    public String deleteSpecialization(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteSpecialization(id);
            redirectAttributes.addFlashAttribute("success", "Specialization deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin-specializations";
    }
}