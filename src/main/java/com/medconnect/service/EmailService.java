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

    private String createHtmlTemplate(String title, String preheader, String bodyContent) {
        // Lấy năm hiện tại
        String currentYear = String.valueOf(java.time.Year.now());

        return "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "  body { margin: 0; padding: 0; font-family: 'Arial', sans-serif; line-height: 1.6; background-color: #f4f7f6; }" +
                "  .container { width: 90%; max-width: 600px; margin: 20px auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; background-color: #ffffff; }" +
                "  .header { background-color: #4f46e5; color: #ffffff; padding: 30px 20px; text-align: center; border-bottom: 5px solid #3730a3; }" +
                "  .header h1 { margin: 0; font-size: 28px; }" +
                "  .content { padding: 30px; color: #333; }" +
                "  .content p { margin-bottom: 20px; font-size: 16px; }" +
                "  .button-container { text-align: center; margin: 30px 0}" +
                "  .button { display: inline-block; background-color: #4f46e5; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px; }" +
                "  .footer { background-color: #008001; color: #888; padding: 20px; text-align: center; font-size: 12px; border-top: 1px solid #ddd; }" +
                "  .preheader { display: none; max-height: 0; overflow: hidden; font-size: 1px; line-height: 1px; color: #fff; }" +
                "  blockquote { border-left: 4px solid #e5e7eb; padding-left: 15px; margin-left: 0; font-style: italic; color: #555; background-color: #f9fafb; padding-top: 10px; padding-bottom: 10px; }" +
                "  ul { list-style-type: none; padding-left: 0; margin-bottom: 20px; }" +
                "  ul li { margin-bottom: 10px; font-size: 16px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<span class'preheader'>" + preheader + "</span>" +
                "<div class='container'>" +
                "  <div class='header'><h1>" + title + "</h1></div>" +
                "  <div class='content'>" + bodyContent + "</div>" +
                "  <div class='footer'><p>&copy; " + currentYear + " MedConnect. All rights reserved.</p></div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Phương thức nội bộ để gửi email.
     * @param toEmail Email người nhận
     * @param subject Tiêu đề email
     * @param htmlContent Nội dung HTML đã được tạo
     * @throws MessagingException
     */
    private void sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Đảm bảo UTF-8
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true = HTML
        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String verificationLink) throws MessagingException {
        String subject = "Xác thực tài khoản MedConnect";
        String preheader = "Chỉ một bước nữa để kích hoạt tài khoản của bạn.";
        String body = "<p>Chào bạn,</p>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại MedConnect. Vui lòng nhấp vào nút bên dưới để xác thực email của bạn:</p>" +
                "<div class='button-container'>" +
                "<a href='" + verificationLink + "' class='button'>Xác thực tài khoản</a>" +
                "</div>" +
                "<p>Nếu bạn không đăng ký, vui lòng bỏ qua email này.</p>";

        String htmlContent = createHtmlTemplate("Xác thực Email", preheader, body);
        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) throws MessagingException {
        String subject = "Đặt lại mật khẩu MedConnect";
        String preheader = "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản MedConnect.";
        String body = "<p>Chào bạn,</p>" +
                "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng nhấp vào nút bên dưới để tạo mật khẩu mới:</p>" +
                "<div class='button-container'>" +
                "<a href='" + resetLink + "' class='button'>Đặt lại mật khẩu</a>" +
                "</div>" +
                "<p>Liên kết này sẽ hết hạn sau 1 giờ. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>";

        String htmlContent = createHtmlTemplate("Đặt lại mật khẩu", preheader, body);
        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendFallbackNotification(String toEmail, String subject, String content) throws MessagingException {
        String preheader = "Bạn có thông báo mới từ MedConnect.";
        String htmlContent = createHtmlTemplate(subject, preheader, content);
        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendAppointmentConfirmationEmail(String toEmail, String patientName, String doctorName, String appointmentTime) throws MessagingException {
        String subject = "Xác nhận lịch hẹn thành công - MedConnect";
        String preheader = "Lịch hẹn của bạn với Bác sĩ " + doctorName + " đã được đặt thành công.";
        String body = "<p>Chào " + patientName + ",</p>" +
                "<p>Lịch hẹn của bạn đã được đặt thành công. Vui lòng kiểm tra thông tin chi tiết:</p>" +
                "<ul>" +
                "  <li><strong>Bác sĩ:</strong> " + doctorName + "</li>" +
                "  <li><strong>Thời gian:</strong> " + appointmentTime + "</li>" +
                "</ul>" +
                "<p>Vui lòng đăng nhập vào dashboard để thanh toán (nếu chưa) và quản lý lịch hẹn.</p>" +
                "<div class='button-container'>" +
                "<a href='http://localhost:8080/patient-dashboard' class='button'>Xem Dashboard</a>" +
                "</div>" +
                "<p>Cảm ơn bạn đã sử dụng MedConnect.</p>";

        String htmlContent = createHtmlTemplate("Lịch hẹn đã được xác nhận", preheader, body);
        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendAppointmentRejectionEmail(String toEmail, String patientName, String doctorName, String appointmentTime, String reason) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Thông báo: Lịch hẹn MedConnect đã bị từ chối");

        String content = "<html><body>" +
                "<p>Chào " + patientName + ",</p>" +
                "<p>Chúng tôi rất tiếc phải thông báo rằng lịch hẹn của bạn với <strong>Bác sĩ " + doctorName + "</strong>" +
                " vào lúc <strong>" + appointmentTime + "</strong> đã bị từ chối.</p>" +
                "<p>Lý do từ bác sĩ: </p>" +
                "<blockquote style='border-left: 4px solid #ccc; padding-left: 10px; font-style: italic;'>" +
                reason +
                "</blockquote>" +
                "<p><strong>Hệ thống sẽ tiến hành hoàn lại khoản thanh toán (nếu có) cho bạn.</strong></p>" +
                "<p>Vui lòng đặt lại một lịch hẹn khác. Cảm ơn bạn đã sử dụng MedConnect.</p>" +
                "</body></html>";

        helper.setText(content, true);
        mailSender.send(message);
    }

    public void sendNewAppointmentNotificationToDoctor(String toEmail, String doctorName, String patientName, String appointmentTime) throws MessagingException {
        String subject = "Bạn có lịch hẹn mới - MedConnect";
        String preheader = "Bệnh nhân " + patientName + " đã đặt lịch hẹn với bạn.";
        String body = "<p>Chào Bác sĩ " + doctorName + ",</p>" +
                "<p>Bạn vừa có một lịch hẹn mới đã được thanh toán thành công. Thông tin chi tiết:</p>" +
                "<ul>" +
                "  <li><strong>Bệnh nhân:</strong> " + patientName + "</li>" +
                "  <li><strong>Thời gian:</strong> " + appointmentTime + "</li>" +
                "</ul>" +
                "<p>Vui lòng kiểm tra trang quản lý (dashboard) của bạn để xem chi tiết và chuẩn bị cho cuộc hẹn.</p>" +
                "<div class='button-container'>" +
                "<a href='http://localhost:8080/doctor-dashboard' class='button'>Xem Dashboard</a>" +
                "</div>" +
                "<p>Cảm ơn bạn đã đồng hành cùng MedConnect.</p>";

        String htmlContent = createHtmlTemplate("Lịch hẹn mới", preheader, body);
        sendEmail(toEmail, subject, htmlContent);
    }
}