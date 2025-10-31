package com.medconnect.service;

import com.medconnect.dto.ScheduleDTO;
import com.medconnect.entity.Doctor;
import com.medconnect.entity.Schedule;
import com.medconnect.exception.TimeSlotOverlapException;
import com.medconnect.repository.DoctorRepository;
import com.medconnect.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    public void addSchedule(Integer userId, ScheduleDTO dto) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("MSG18: Doctor profile not found."));

        // Lặp qua từng slot mà bác sĩ đã chọn
        for (String slot : dto.getSlots()) {
            LocalTime startTime;
            LocalTime endTime;

            try {
                startTime = LocalTime.parse(slot);
                endTime = startTime.plusMinutes(30); // Mỗi slot là 30 phút
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Định dạng slot không hợp lệ: " + slot);
            }

            // Kiểm tra trùng lặp cho từng slot
            List<Schedule> overlapping = scheduleRepository.findOverlappingSchedules(
                    doctor.getDoctorId(), dto.getDayOfWeek(), startTime, endTime
            );

            if (!overlapping.isEmpty()) {
                // Có thể bỏ qua slot này hoặc ném lỗi.
                // Ở đây chúng ta bỏ qua để tránh lỗi nếu user submit form 2 lần.
                // throw new TimeSlotOverlapException("MSG17: Khung giờ " + slot + " bị trùng.");
                continue; // Bỏ qua slot đã tồn tại
            }

            // Tạo một Schedule MỚI cho MỖI slot
            Schedule schedule = new Schedule();
            schedule.setDoctor(doctor);
            schedule.setDayOfWeek(dto.getDayOfWeek());
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setConsultationType(dto.getConsultationType());
            scheduleRepository.save(schedule);
        }
    }

    public void deleteSchedule(Integer scheduleId, Integer userId) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor profile not found."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found."));

        // Kiểm tra xem lịch này có thuộc về bác sĩ đang đăng nhập không
        if (!schedule.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new SecurityException("You do not have permission to delete this schedule.");
        }

        // Thay vì xóa cứng, ta có thể đánh dấu là không hoạt động (soft-delete)
        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(Integer scheduleId, ScheduleDTO dto, Integer userId) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor profile not found."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found."));

        // Kiểm tra quyền sở hữu
        if (!schedule.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
            throw new SecurityException("You do not have permission to update this schedule.");
        }

        // DTO từ modal chỉnh sửa sẽ chỉ có 1 slot trong danh sách
        if (dto.getSlots() == null || dto.getSlots().isEmpty()) {
            throw new IllegalArgumentException("Không có slot nào được chọn để cập nhật.");
        }

        String newSlot = dto.getSlots().get(0); // Lấy slot duy nhất
        LocalTime startTime;
        LocalTime endTime;

        try {
            startTime = LocalTime.parse(newSlot);
            endTime = startTime.plusMinutes(30);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng slot không hợp lệ: " + newSlot);
        }

        // Kiểm tra logic thời gian và trùng lặp
        if (!endTime.isAfter(startTime)) {
            throw new TimeSlotOverlapException("MSG17: Giờ kết thúc phải sau giờ bắt đầu.");
        }

        List<Schedule> overlapping = scheduleRepository.findOverlappingSchedules(
                doctor.getDoctorId(), dto.getDayOfWeek(), startTime, endTime
        );

        // Khi kiểm tra trùng lặp, phải loại trừ chính lịch đang sửa
        if (overlapping.stream().anyMatch(s -> !s.getScheduleId().equals(scheduleId))) {
            throw new TimeSlotOverlapException("MSG17: Khung giờ này bị trùng với một lịch đã có.");
        }

        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setConsultationType(dto.getConsultationType());
        scheduleRepository.save(schedule);
    }

    public List<ScheduleDTO> getSchedulesByUserId(Integer userId) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor profile not found for user."));

        return scheduleRepository.findByDoctor_DoctorIdAndActiveTrue(doctor.getDoctorId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setDayOfWeek(schedule.getDayOfWeek());

        // Gửi về DTO dưới dạng danh sách 1 slot
        dto.setSlots(List.of(schedule.getStartTime().toString()));

        dto.setConsultationType(schedule.getConsultationType());
        return dto;
    }

    public Map<String, String> getAvailableTimeSlots(Integer userId, Integer dayOfWeek) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor profile not found."));

        // 1. Lấy tất cả các slot đã đăng ký
        Set<LocalTime> existingSlots = scheduleRepository.findByDoctor_DoctorIdAndActiveTrueAndDayOfWeek(
                        doctor.getDoctorId(), dayOfWeek
                ).stream()
                .map(Schedule::getStartTime)
                .collect(Collectors.toSet());

        // 2. Tạo tất cả các slot 30 phút từ 07:00 đến 20:30
        Map<String, String> allSlots = new LinkedHashMap<>(); // Giữ thứ tự
        LocalTime time = LocalTime.of(7, 0);
        LocalTime endTime = LocalTime.of(21, 0);

        while (time.isBefore(endTime)) {
            allSlots.put(time.toString(), time.toString() + " - " + time.plusMinutes(30).toString());
            time = time.plusMinutes(30);
        }

        // 3. Lọc ra những slot đã tồn tại
        existingSlots.forEach(existingTime -> {
            allSlots.remove(existingTime.toString());
        });

        // 4. Trả về danh sách các slot còn trống
        return allSlots;
    }
}