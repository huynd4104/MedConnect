package com.medconnect.controller;

import com.medconnect.dto.RegisterDTO;
import com.medconnect.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO registerDTO, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid input.");
            return "redirect:/register";
        }
        try {
            registerService.register(registerDTO);
            redirectAttributes.addFlashAttribute("success", "Registration successful. Check your email for verification.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/verify")
    public String verify(@RequestParam String token, RedirectAttributes redirectAttributes) {
        try {
            registerService.verifyToken(token);
            redirectAttributes.addFlashAttribute("success", "Account verified.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "MSG32: Verification link expired.");
            return "redirect:/register";
        }
    }
}