package com.medconnect.controller;

import com.medconnect.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class VideoCallController {
    private final VideoService videoService;

    @GetMapping("/video-call/{appointmentId}")
    public String joinVideoCall(@PathVariable Integer appointmentId, Model model) {
        // Get appointment, check time
        // videoService.initializeSession(appointment);
        model.addAttribute("token", videoService.generateZegoToken("userId", "sessionId"));
        return "video-call";
    }
}