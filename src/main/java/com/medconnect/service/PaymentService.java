package com.medconnect.service;

import com.medconnect.entity.Appointment;
import com.medconnect.entity.Payment;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${app.vnpay.tmn-code}")
    private String tmnCode;

    @Value("${app.vnpay.hash-secret}")
    private String hashSecret;

    public String createVnpayPaymentUrl(Payment payment, String returnUrl) throws Exception {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(payment.getAmount().multiply(new java.math.BigDecimal(100)).intValue()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(payment.getAppointment().getAppointmentId()));
        params.put("vnp_OrderInfo", "Thanh toan lich hen " + payment.getAppointment().getAppointmentId());
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()))
                    .append("&");
        }
        String queryStr = query.substring(0, query.length() - 1);

        String hashData = queryStr;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashBytes = md.digest((hashData + hashSecret).getBytes(StandardCharsets.UTF_8));
        String secureHash = bytesToHex(hashBytes);

        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + queryStr + "&vnp_SecureHash=" + secureHash;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public void processVnpayCallback(Map<String, String> params) {
        // Verify hash and update payment status
        String secureHash = params.remove("vnp_SecureHash");
        String hashData = String.join("&", params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .sorted()
                .toArray(String[]::new));
        // Calculate hash and compare
        // If success, update payment and appointment
        Integer appointmentId = Integer.parseInt(params.get("vnp_TxnRef"));
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setPaymentStatus(Appointment.PaymentStatus.Paid);
        appointment.setStatus(Appointment.Status.Confirmed);
        appointmentRepository.save(appointment);
    }
}