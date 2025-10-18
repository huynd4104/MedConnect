package com.medconnect.service;

import com.medconnect.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// 1. IMPORT CHO JSON (từ thư viện json-simple)
import org.json.simple.JSONObject;

// 2. IMPORTS CHO CÁC FILE LOCAL BẠN ĐÃ TẢI VỀ
import com.zegocloud.token.TokenServerAssistant;
import com.zegocloud.token.TokenServerAssistant.TokenInfo;
import com.zegocloud.token.TokenServerAssistant.ErrorCode;


@Service
@RequiredArgsConstructor
public class VideoService {
    @Value("${app.zegocloud.app-id}")
    private long appId;

    @Value("${app.zegocloud.server-secret}")
    private String serverSecret;

    /**
     * Sửa lại để gọi generateToken04
     * @param userId ID người dùng (ví dụ: "2")
     * @param sessionId ID phòng (ví dụ: "session_15")
     * @return Chuỗi token đã mã hóa
     */
    public String generateZegoToken(String userId, String sessionId) {

        long expirationInSecondsLong = 3600; // Token có hiệu lực trong 1 giờ (dạng long)

        // 1. Tạo Payload theo yêu cầu của Token04
        JSONObject payloadData = new JSONObject();
        // Tên phòng (sessionId) phải được đặt BÊN TRONG payload
        payloadData.put("room_id", sessionId);

        // 2. Cài đặt quyền (cho phép đăng nhập và phát video/audio)
        JSONObject privilege = new JSONObject();
        privilege.put(TokenServerAssistant.PrivilegeKeyLogin, TokenServerAssistant.PrivilegeEnable);
        privilege.put(TokenServerAssistant.PrivilegeKeyPublish, TokenServerAssistant.PrivilegeEnable);

        payloadData.put("privilege", privilege);

        try {
            // *** SỬA LỖI TẠI ĐÂY ***
            // 3. Gọi hàm generateToken04 (thay vì generateToken)
            TokenInfo tokenInfo = TokenServerAssistant.generateToken04(
                    this.appId,
                    userId,
                    this.serverSecret,
                    (int) expirationInSecondsLong, // <-- Ép kiểu sang int
                    payloadData.toString()         // <-- Chuyển payload sang String
            );

            // 4. Kiểm tra lỗi (nếu có)
            if (tokenInfo.error.code != ErrorCode.SUCCESS) {
                // Ném ra lỗi nếu tạo token thất bại
                throw new RuntimeException("Tạo token Zego (Token04) thất bại: " + tokenInfo.error.message);
            }

            // 5. Trả về token đã được mã hóa
            // (Phiên bản Token04 trả về token trong trường .data)
            return tokenInfo.data;

        } catch (Exception e) {
            // Ghi log lỗi nếu có vấn đề
            System.err.println("Lỗi nghiêm trọng khi tạo ZegoToken (Token04): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo token video call.", e);
        }
    }

    // Phương thức initializeSession (Giữ nguyên, không thay đổi)
    public void initializeSession(Appointment appointment) {
        String sessionId = "session_" + appointment.getAppointmentId();
        appointment.setVideoCallLink(sessionId);
    }

    // Phương thức terminateSession (Giữ nguyên, không thay đổi)
    public void terminateSession(Appointment appointment) {
        appointment.setStatus(Appointment.Status.Completed);
    }
}