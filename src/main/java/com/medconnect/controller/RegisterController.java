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
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // 1. Xử lý lỗi validation (MSG02, MSG03,...)
        if (result.hasErrors()) {
            return "register";
        }

        // 2. Xử lý lỗi logic (ví dụ: email đã tồn tại)
        try {
            registerService.register(registerDTO);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Kiểm tra email để xác minh.");
            return "redirect:/login";
        } catch (Exception e) {
            // Lỗi logic (như email trùng) MỚI dùng redirect
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Giữ lại dữ liệu người dùng đã nhập khi redirect
            redirectAttributes.addFlashAttribute("registerDTO", registerDTO);
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