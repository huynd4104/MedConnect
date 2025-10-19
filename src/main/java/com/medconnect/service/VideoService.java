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
            TokenInfo tokenInfo = TokenServerAssistant.generateToken04(
                    this.appId,
                    userId,
                    this.serverSecret,
                    (int) expirationInSecondsLong,
                    payloadData.toString()
            );

            // 4. Kiểm tra lỗi
            if (tokenInfo.error.code != ErrorCode.SUCCESS) {
                throw new RuntimeException("Tạo token Zego (Token04) thất bại: " + tokenInfo.error.message);
            }

            // 5. Trả về token đã được mã hóa
            return tokenInfo.data;

        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi tạo ZegoToken (Token04): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo token video call.", e);
        }
    }

    public void initializeSession(Appointment appointment) {
        String sessionId = "session_" + appointment.getAppointmentId();
        appointment.setVideoCallLink(sessionId);
    }

    public void terminateSession(Appointment appointment) {
        appointment.setStatus(Appointment.Status.Completed);
    }
}