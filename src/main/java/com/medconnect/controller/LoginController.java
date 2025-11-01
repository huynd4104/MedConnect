package com.medconnect.controller;

import com.medconnect.dto.LoginDTO;
import com.medconnect.dto.ResetPasswordDTO;
import com.medconnect.entity.Token;
import com.medconnect.repository.TokenRepository;
import com.medconnect.service.LoginService;
import jakarta.servlet.http.HttpServletRequest; // <-- THÊM IMPORT NÀY
import jakarta.servlet.http.HttpSession;      // <-- THÊM IMPORT NÀY
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException; // <-- THÊM IMPORT NÀY
import org.springframework.security.web.WebAttributes;             // <-- THÊM IMPORT NÀY
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
public class LoginController {

    private final LoginService loginService;
    private final TokenRepository tokenRepository;

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
    public String processForgotPassword(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            loginService.forgotPassword(email);
            model.addAttribute("success", "Một liên kết đặt lại mật khẩu đã được gửi đến email của bạn. Vui lòng kiểm tra (kể cả thư mục spam).");
            return "forgot-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    // FORM ĐẶT LẠI MẬT KHẨU
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Kiểm tra token có hợp lệ không trước khi hiển thị form
        try {
            tokenRepository.findValidByTokenAndType(token, Token.TokenType.PasswordReset)
                    .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn."));

            ResetPasswordDTO dto = new ResetPasswordDTO();
            dto.setToken(token);
            model.addAttribute("resetPasswordDTO", dto);

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reset-password";
    }

    // XỬ LÝ ĐẶT LẠI MẬT KHẨU
    @PostMapping("/reset-password")
    public String processResetPassword(@Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
                                       BindingResult result,
                                       Model model, // <-- Thêm Model
                                       RedirectAttributes redirectAttributes) {

        // 1. Kiểm tra lỗi validation (regex)
        if (result.hasErrors()) {
            return "reset-password";
        }

        // 2. Kiểm tra mật khẩu khớp
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            // Thêm lỗi vào Model và trả về view
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "reset-password";
        }

        try {
            // 3. Logic reset thành công
            loginService.resetPassword(dto);
            redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/login"; // Chuyển về trang login khi thành công

        } catch (Exception e) {
            // 4. Lỗi logic (ví dụ: token hết hạn trong lúc đang nhập)
            // Lỗi này MỚI cần redirect
            String redirectUrl = "redirect:/reset-password?token=" + dto.getToken();
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return redirectUrl;
        }
    }
}