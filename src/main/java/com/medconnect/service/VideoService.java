package com.medconnect.service;

import com.medconnect.entity.Appointment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VideoService {
    @Value("${app.zegocloud.app-id}")
    private long appId;

    @Value("${app.zegocloud.server-secret}")
    private String serverSecret;

    public String generateZegoToken(String userId, String sessionId) {
        // Logic generate token for ZEGOCLOUD SDK
        // Sử dụng thư viện ZEGOCLOUD nếu có, hoặc HTTP call
        // Ở đây demo return token string
        return "generated_token_" + userId + "_" + sessionId;
    }

    public void initializeSession(Appointment appointment) {
        // Generate session ID and tokens for patient and doctor
        String sessionId = "session_" + appointment.getAppointmentId();
        appointment.setVideoCallLink(sessionId);
        // Lưu token vào user or appointment if needed
    }

    public void terminateSession(Appointment appointment) {
        // Log duration, update status to Completed
        appointment.setStatus(Appointment.Status.Completed);
        // Trigger UC14
    }
}