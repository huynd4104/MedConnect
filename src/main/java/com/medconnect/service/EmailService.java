package com.medconnect.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.from-email}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String verificationLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Xác thực tài khoản MedConnect");
        helper.setText("Nhấp vào liên kết để xác thực: <a href=\"" + verificationLink + "\">Xác thực</a>", true);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Đặt lại mật khẩu MedConnect");
        helper.setText("Nhấp vào liên kết để đặt lại mật khẩu: <a href=\"" + resetLink + "\">Đặt lại</a>", true);
        mailSender.send(message);
    }

    public void sendFallbackNotification(String toEmail, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, true);
        mailSender.send(message);
    }
}