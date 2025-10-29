package com.medconnect.service;

import com.medconnect.dto.AppointmentDTO;
import com.medconnect.entity.Appointment;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.Patient;
import com.medconnect.entity.User;
import com.medconnect.repository.AppointmentRepository;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

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
        notificationService.sendPushNotification(doctor.getUser(), "New Appointment", "Bạn có một cuộc hẹn mới, vui lòng kiểm tra trong trang quản lý.");

        try {
            String subject = "Xác nhận lịch hẹn thành công";
            String content = "Xin chào " + patient.getFullName() + ",<br>" +
                    "Lịch hẹn của bạn với bác sĩ " + doctor.getUser().getEmail() +
                    " vào lúc " + dto.getAppointmentDateTime().toString() +
                    " đã được đặt thành công.";

            // currentUser chính là User của Bệnh nhân
            emailService.sendFallbackNotification(currentUser.getEmail(), subject, content);

        } catch (Exception e) {
            // Log lỗi (ví dụ: Gửi mail thất bại)
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
            // Có thể ném ra lỗi nếu logic nghiệp vụ yêu cầu
            // (ví dụ: không thể hoàn thành lịch hẹn đã hủy)
            throw new IllegalStateException("Chỉ có thể hoàn thành lịch hẹn đã được xác nhận.");
        }
    }
}