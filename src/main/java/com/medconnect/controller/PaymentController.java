package com.medconnect.controller;

import com.medconnect.dto.PaymentDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.Patient;
import com.medconnect.entity.Payment;
import com.medconnect.entity.User;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.PatientRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    @GetMapping("/payment")
    public String showPaymentForm(@RequestParam("appointmentId") Integer appointmentId,
                                  Model model,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        try {
            // 1. Lấy thông tin bệnh nhân đang đăng nhập
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            Patient currentPatient = patientRepository.findByUser(currentUser)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bệnh nhân"));

            // 2. Tìm lịch hẹn
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

            // 3. (Bảo mật) Đảm bảo lịch hẹn này là của bệnh nhân đang đăng nhập
            if (!appointment.getPatient().getPatientId().equals(currentPatient.getPatientId())) {
                redirectAttributes.addFlashAttribute("error", "Lỗi: Bạn không có quyền xem thanh toán này.");
                return "redirect:/patient-dashboard";
            }

            // 4. (Logic nghiệp vụ) Kiểm tra xem đã thanh toán chưa
            if (appointment.getPaymentStatus() == Appointment.PaymentStatus.Paid) {
                redirectAttributes.addFlashAttribute("success", "Lịch hẹn này đã được thanh toán.");
                return "redirect:/patient-dashboard";
            }

            // 5. Tính phí (TẠM THỜI ĐẶT CỨNG LÀ 150.000 VND - BẠN CÓ THỂ THAY ĐỔI SAU)
            BigDecimal fee = new BigDecimal("150000");

            // 6. Tạo DTO và điền sẵn thông tin
            PaymentDTO dto = new PaymentDTO();
            dto.setAppointmentId(appointmentId);
            dto.setAmount(fee);

            // 7. Thêm các đối tượng vào Model
            model.addAttribute("appointment", appointment); // <-- Sửa lỗi Null
            model.addAttribute("fee", fee); // <-- Sửa lỗi Null
            model.addAttribute("paymentDTO", dto);

            return "payment"; // Trả về trang thanh toán

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/patient-dashboard";
        }
    }

    @PostMapping("/payment")
    public String processPayment(@ModelAttribute PaymentDTO dto) throws Exception {
        Payment payment = new Payment(); // Map from DTO
        String url = paymentService.createVnpayPaymentUrl(payment, "http://localhost:8080/payment-callback");
        return "redirect:" + url;
    }

    @GetMapping("/payment-callback")
    public String paymentCallback(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        try {
            paymentService.processVnpayCallback(params);
            redirectAttributes.addFlashAttribute("success", "Payment successful.");
            return "redirect:/patient-dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "MSG22: Payment failed.");
            return "redirect:/payment";
        }
    }
}