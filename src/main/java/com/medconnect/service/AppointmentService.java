package com.medconnect.service;

import com.medconnect.dto.AppointmentDTO;
import com.medconnect.entity.*;
import com.medconnect.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final ScheduleRepository scheduleRepository;

    public Map<String, String> getAvailableSlots(Integer doctorId, LocalDate date, Appointment.ConsultationType type) {

        // 1. Chuyển đổi từ Java DayOfWeek (1=T2, 7=CN) sang DB (1=CN, 2=T2, ..., 7=T7)
        int dbDayOfWeek = (date.getDayOfWeek().getValue() % 7) + 1;

        Schedule.ConsultationType scheduleType = Schedule.ConsultationType.valueOf(type.name());

        // 2. Lấy tất cả các slot bác sĩ CÓ ĐĂNG KÝ làm việc (đã dùng scheduleType đã chuyển đổi)
        List<Schedule> schedules = scheduleRepository.findByDoctor_DoctorIdAndActiveTrueAndDayOfWeekAndConsultationType(
                doctorId, dbDayOfWeek, scheduleType
        );
        Set<LocalTime> scheduledSlots = schedules.stream()
                .map(Schedule::getStartTime)
                .collect(Collectors.toSet());

        // 3. Lấy tất cả các slot ĐÃ BỊ ĐẶT (Pending hoặc Confirmed)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        List<Appointment> bookedAppointments = appointmentRepository.findBookedAppointmentsByDate(
                doctorId, startOfDay, endOfDay
        );
        Set<LocalTime> bookedSlots = bookedAppointments.stream()
                .map(app -> app.getAppointmentDateTime().toLocalTime())
                .collect(Collectors.toSet());

        // 4. Lọc: Lấy (Slot đăng ký) TRỪ ĐI (Slot đã đặt)
        scheduledSlots.removeAll(bookedSlots);

        // 5. Ngăn đặt lịch trong quá khứ (nếu ngày chọn là hôm nay)
        if (date.isEqual(LocalDate.now())) {
            LocalTime now = LocalTime.now();
            // Cũng kiểm tra các slot đã bị đặt (vì có thể có lịch hẹn đã qua trong ngày)
            bookedSlots.forEach(slotTime -> {
                if(slotTime.isBefore(now)) {
                    scheduledSlots.remove(slotTime);
                }
            });
            // Và các slot trống chưa tới giờ
            scheduledSlots.removeIf(slotTime -> slotTime.isBefore(now));
        }

        // 6. Tạo Map để trả về (Key="HH:mm", Value="HH:mm - HH:mm")
        Map<String, String> availableSlots = new LinkedHashMap<>();
        scheduledSlots.stream().sorted().forEach(time -> {
            availableSlots.put(
                    time.toString(), // Key (e.g., "09:00")
                    String.format("%s - %s", time.toString(), time.plusMinutes(30).toString()) // Value (e.g., "09:00 - 09:30")
            );
        });

        return availableSlots;
    }

    public Appointment bookAppointment(User currentUser, AppointmentDTO dto) {
        Patient patient = patientRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bệnh nhân cho người dùng này."));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId()).orElseThrow();

        // Chuyển đổi Date (String) và Time (String) từ DTO thành LocalDateTime
        LocalDate date = LocalDate.parse(dto.getAppointmentDate());
        LocalTime time = LocalTime.parse(dto.getAppointmentTime());
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

        // (Kiểm tra logic) Đảm bảo slot này vẫn còn trống (tránh trường hợp 2 người đặt cùng lúc)
        Map<String, String> availableSlots = getAvailableSlots(dto.getDoctorId(), date, dto.getConsultationType());
        if (!availableSlots.containsKey(dto.getAppointmentTime())) {
            throw new RuntimeException("MSG21: Rất tiếc, khung giờ này vừa có người khác đặt. Vui lòng chọn giờ khác.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(appointmentDateTime); // Đặt giờ đã gộp
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
            String appTime = appointmentDateTime.format(DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy"));

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