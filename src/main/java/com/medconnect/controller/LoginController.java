package com.medconnect.controller;

import com.medconnect.dto.LoginDTO;
import com.medconnect.service.LoginService;
import jakarta.servlet.http.HttpServletRequest; // <-- THÊM IMPORT NÀY
import jakarta.servlet.http.HttpSession;      // <-- THÊM IMPORT NÀY
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException; // <-- THÊM IMPORT NÀY
import org.springframework.security.web.WebAttributes;             // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    // SỬA LẠI PHƯƠNG THỨC NÀY
    public String showLoginForm(Model model, HttpServletRequest request) {
        // Cung cấp một DTO trống cho form
        if (!model.containsAttribute("loginDTO")) {
            model.addAttribute("loginDTO", new LoginDTO());
        }

        HttpSession session = request.getSession(false);
        String errorMessage = null;
        if (session != null) {
            AuthenticationException ex = (AuthenticationException) session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (ex != null) {
                errorMessage = ex.getMessage();
                // Xóa lỗi khỏi session để không hiển thị lại khi refresh
                session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            }
        }

        if (errorMessage != null && !model.containsAttribute("error")) {
            model.addAttribute("error", errorMessage);
        }

        return "login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            loginService.forgotPassword(email);
            redirectAttributes.addFlashAttribute("success", "Một liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }
}