package com.medconnect.controller;

import com.medconnect.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardRedirectController {
    @GetMapping("/dashboard")
    public String redirectToDashboard(Authentication auth) {
        User user = (User) auth.getPrincipal();
        switch (user.getRole()) {
            case Patient:
                return "redirect:/patient-dashboard";
            case Doctor:
                return "redirect:/doctor-dashboard";
            case Admin:
                return "redirect:/admin-doctor-approval";
            default:
                return "redirect:/login";
        }
    }
}