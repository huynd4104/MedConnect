package com.medconnect.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class LogoutService {

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Xoá Authentication trong SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            SecurityContextHolder.clearContext();
        }

        // Hủy session nếu có
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}

