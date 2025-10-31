package com.medconnect.service;

import com.medconnect.dto.AppointmentDTO;
import com.medconnect.entity.*;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.PatientRepository;
import com.medconnect.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;

    public Appointment bookAppointment(User currentUser, AppointmentDTO dto) {
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bệnh nhân cho người dùng này."));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow();
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(dto.getAppointmentDateTime());
        appointment.setConsultationType(dto.getConsultationType());
        appointmentRepository.save(appointment);

        // Notify doctor
        notificationService.sendPushNotification(
                doctor.getUser(),
                "New Appointment",
                "Bạn có một cuộc hẹn mới, vui lòng kiểm tra trong trang quản lý."
        );

        notificationService.sendPushNotification(
                patient.getUser(),
                "New Appointment",
                "Bạn đã đặt lịch hẹn với bác sĩ " + doctor.getFullName() + ", xin vui lòng thanh toán."
        );
        try {
            // Lấy tên đầy đủ (nếu có), nếu không thì dùng email
            String patientName = (patient.getFullName() != null && !patient.getFullName().isEmpty())
                    ? patient.getFullName()
                    : currentUser.getEmail();

            String doctorName = (doctor.getFullName() != null && !doctor.getFullName().isEmpty())
                    ? doctor.getFullName()
                    : doctor.getUser().getEmail();

            // Format lại thời gian cho đẹp
            String appTime = dto.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy"));

            // Gọi hàm email mới
            emailService.sendAppointmentConfirmationEmail(
                    currentUser.getEmail(),
                    patientName,
                    doctorName,
                    appTime
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return appointment;
    }

    public List<Appointment> getPatientAppointments(Integer patientId) {
        return appointmentRepository.findByPatientPatientId(patientId);
    }

    public List<Appointment> getDoctorAppointments(Integer doctorId) {
        return appointmentRepository.findByDoctorDoctorId(doctorId);
    }

    @Transactional
    public void rejectAppointmentByDoctor(Integer appointmentId, String reason) {
        // 1. Lấy thông tin
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn."));

        // 2. Kiểm tra trạng thái
        if (appointment.getStatus() == Appointment.Status.Completed || appointment.getStatus() == Appointment.Status.Cancelled) {
            throw new IllegalStateException("Không thể từ chối lịch hẹn đã hoàn thành hoặc đã hủy.");
        }

        // 3. Cập nhật trạng thái
        appointment.setStatus(Appointment.Status.Cancelled);
        appointment.setPaymentStatus(Appointment.PaymentStatus.Refunded); // Đánh dấu là cần hoàn tiền

        // 4. Cập nhật trạng thái Payment (nếu đã thanh toán)
        paymentRepository.findByAppointmentAppointmentId(appointmentId).ifPresent(payment -> {
            if (payment.getStatus() == Payment.Status.Success) {
                payment.setStatus(Payment.Status.Refunded); // Cập nhật Payment entity
                paymentRepository.save(payment);
            }
        });

        appointmentRepository.save(appointment); // Lưu thay đổi của Appointment

        // 5. Gửi thông báo (Push Notification)
        Patient patient = appointment.getPatient();
        String notificationMessage = "Lịch hẹn của bạn đã bị từ chối. Lý do: \"" + reason +
                "\". Vui lòng đặt lịch lại.";

        notificationService.sendPushNotification(
                patient.getUser(),
                "Appointment Declined",
                notificationMessage // Truyền nội dung mới vào đây
        );

        // 6. Gửi Email
        try {
            Doctor doctor = appointment.getDoctor();

            String patientName = (patient.getFullName() != null && !patient.getFullName().isEmpty())
                    ? patient.getFullName()
                    : patient.getUser().getEmail();

            String doctorName = (doctor.getFullName() != null && !doctor.getFullName().isEmpty())
                    ? doctor.getFullName()
                    : doctor.getUser().getEmail();

            String appTime = appointment.getAppointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy"));

            emailService.sendAppointmentRejectionEmail(
                    patient.getUser().getEmail(),
                    patientName,
                    doctorName,
                    appTime,
                    reason
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email từ chối: " + e.getMessage());
        }
    }

    public void cancelAppointment(Integer appointmentId, boolean byPatient) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        if (appointment.getAppointmentDateTime().minusHours(24).isBefore(now)) {
            // No refund if <24h (BR-05)
        } else {
            // Full refund
        }
        appointment.setStatus(Appointment.Status.Cancelled);
        appointmentRepository.save(appointment);
        // Notify other party
    }

    public void completeAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn: " + appointmentId));

        // Chỉ cho phép hoàn thành các lịch hẹn đã Confirmed
        if (appointment.getStatus() == Appointment.Status.Confirmed) {
            appointment.setStatus(Appointment.Status.Completed);
            appointment.setUpdatedAt(LocalDateTime.now()); // Cập nhật thời gian
            appointmentRepository.save(appointment);
        } else {
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận.");
        }
    }
}