package com.medconnect.service;

import com.medconnect.entity.Appointment;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.Patient;
import com.medconnect.entity.Payment;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final VideoService videoService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Value("${app.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.vnpay.hash-secret}")
    private String hashSecret;

    public String createVnpayPaymentUrl(Payment payment, String returnUrl) throws Exception {
        // 1. Chuẩn bị params (TreeMap để tự động sắp xếp theo key)
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(payment.getAmount().multiply(new BigDecimal(100))));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(payment.getAppointment().getAppointmentId()));
        params.put("vnp_OrderInfo", "Thanh toan lich hen " + payment.getAppointment().getAppointmentId());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");

        LocalDateTime createTime = LocalDateTime.now();
        LocalDateTime expireTime = createTime.plusMinutes(15);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", createTime.format(formatter));
        params.put("vnp_ExpireDate", expireTime.format(formatter));

        StringBuilder hashData = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();

        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');

                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // 3. Tạo chữ ký
        String secureHash = hmacSHA512(hashSecret, hashData.toString());
        params.put("vnp_SecureHash", secureHash); // Thêm chữ ký vào map

        System.out.println("VNPAY_SECURE_HASH (New): " + secureHash);

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
            query.append('=');
            query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString())); // Encode giá trị
            query.append('&');
        }
        // Xóa dấu '&' cuối cùng
        query.deleteCharAt(query.length() - 1);

        String queryStr = query.toString();
        String paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + queryStr;

        // 5. Trả về URL đầy đủ
        return paymentUrl;
    }


    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmacSha512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmacSha512.init(secretKey);
        byte[] hash = hmacSha512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b)); // Giữ chữ thường "02x"
        }
        return sb.toString();
    }

    public void processVnpayCallback(Map<String, String> params) {
        // ... (Giữ logic xác thực hash nếu có) ...

        String responseCode = params.get("vnp_ResponseCode");
        Integer appointmentId = Integer.parseInt(params.get("vnp_TxnRef"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn: " + appointmentId));

        Payment payment = paymentRepository.findByAppointmentAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thanh toán cho lịch hẹn: " + appointmentId));

        if ("00".equals(responseCode)) {
            // === 2. THANH TOÁN THÀNH CÔNG ===

            // Cập nhật trạng thái Lịch hẹn
            appointment.setStatus(Appointment.Status.Confirmed);
            appointment.setPaymentStatus(Appointment.PaymentStatus.Paid);

            // Tạo video link
            if (appointment.getConsultationType() == Appointment.ConsultationType.Online) {
                videoService.initializeSession(appointment);
            }

            // Cập nhật trạng thái Thanh toán
            payment.setStatus(Payment.Status.Success);
            payment.setTransactionId(params.get("vnp_TransactionNo"));
            payment.setPaidAt(LocalDateTime.now());

            // Lưu cả hai vào CSDL
            appointmentRepository.save(appointment);
            paymentRepository.save(payment);

            // === BẮT ĐẦU THÊM MỚI (GỬI THÔNG BÁO CHO BÁC SĨ) ===
            try {
                Doctor doctor = appointment.getDoctor();
                Patient patient = appointment.getPatient();

                // 1. Gửi Push Notification (Realtime)
                notificationService.sendPushNotification(
                        doctor.getUser(),
                        "Lịch hẹn mới",
                        "Bạn có một cuộc hẹn mới, vui lòng kiểm tra trong trang quản lý."
                );

                // 2. Gửi Email
                String doctorName = (doctor.getFullName() != null && !doctor.getFullName().isEmpty())
                        ? doctor.getFullName()
                        : doctor.getUser().getEmail();
                String patientName = (patient.getFullName() != null && !patient.getFullName().isEmpty())
                        ? patient.getFullName()
                        : patient.getUser().getEmail();
                String appTime = appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy"));

                emailService.sendNewAppointmentNotificationToDoctor(
                        doctor.getUser().getEmail(),
                        doctorName,
                        patientName,
                        appTime
                );

            } catch (Exception e) {
                // Ghi log lỗi gửi thông báo cho bác sĩ (nhưng không làm dừng luồng thanh toán)
                System.err.println("Lỗi khi gửi thông báo cho bác sĩ sau khi thanh toán: " + e.getMessage());
                e.printStackTrace();
            }
            // === KẾT THÚC THÊM MỚI ===

        } else {
            // === 3. THANH TOÁN THẤT BẠI ===
            payment.setStatus(Payment.Status.Failed);
            paymentRepository.save(payment);

            throw new RuntimeException("Thanh toán VNPAY thất bại. Mã lỗi: " + responseCode);
        }
    }
}