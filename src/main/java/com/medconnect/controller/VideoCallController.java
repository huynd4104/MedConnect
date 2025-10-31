package com.medconnect.controller;

import com.medconnect.entity.Appointment;
import com.medconnect.entity.User;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.UserRepository;
import com.medconnect.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class VideoCallController {
    private final VideoService videoService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Value("${app.zegocloud.app-id}")
    private long zegoAppId;

    @GetMapping("/video-call/{appointmentId}")
    public String joinVideoCall(@PathVariable Integer appointmentId, Model model, Authentication auth) { // THÊM Authentication
        try {
            // 1. Lấy thông tin người dùng đang đăng nhập
            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            // 2. Lấy thông tin lịch hẹn
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn."));

            // 3. (Bảo mật) Kiểm tra xem người này có quyền tham gia không
            boolean isPatient = appointment.getPatient().getUser().getUserId().equals(currentUser.getUserId());
            boolean isDoctor = appointment.getDoctor().getUser().getUserId().equals(currentUser.getUserId());

            if (!isPatient && !isDoctor) {
                model.addAttribute("error", "Bạn không có quyền truy cập phòng video call này.");
                return "video-call"; // Trả về trang video-call với thông báo lỗi
            }

            // 4. Kiểm tra trạng thái lịch hẹn
            if (appointment.getStatus() != Appointment.Status.Confirmed) {
                model.addAttribute("error", "Lịch hẹn này chưa được xác nhận.");
                return "video-call";
            }

            // (logic chỉ cho phép tham gia 10 phút trước giờ hẹn)
            // if (LocalDateTime.now().isBefore(appointment.getAppointmentDateTime().minusMinutes(10))) {
            //     model.addAttribute("error", "Chưa đến giờ hẹn. Vui lòng quay lại sau.");
            //     return "video-call";
            // }

            // 5. Lấy thông tin phòng (sessionId)
            String sessionId = appointment.getVideoCallLink();
            if (sessionId == null) {
                model.addAttribute("error", "Lịch hẹn này không có link video call.");
                return "video-call";
            }

            // 6. Tạo ZegoToken (Zego yêu cầu userId phải là String)
            String zegoUserId = String.valueOf(currentUser.getUserId());
            String zegoUserName = email; // Lấy email làm tên
            String token = videoService.generateZegoToken(zegoUserId, sessionId);

            // 7. Gửi toàn bộ thông tin cần thiết sang template video-call.html
            model.addAttribute("appId", zegoAppId);
            model.addAttribute("token", token);
            model.addAttribute("sessionId", sessionId); // Tên phòng (ví dụ: session_15)
            model.addAttribute("userId", zegoUserId);
            model.addAttribute("userName", zegoUserName);
            model.addAttribute("appointment", appointment);

            return "video-call";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tham gia phòng: " + e.getMessage());
            return "video-call";
        }
    }
}