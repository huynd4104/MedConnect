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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;

    public void addSchedule(Integer userId, ScheduleDTO dto) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("MSG18: Doctor profile not found."));

        LocalTime startTime;
        LocalTime endTime;

        try {
            startTime = LocalTime.parse(dto.getStartTime());
            endTime = LocalTime.parse(dto.getEndTime());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng thời gian không hợp lệ.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new TimeSlotOverlapException("MSG17: Giờ kết thúc phải sau giờ bắt đầu.");
        }

        List<Schedule> overlapping = scheduleRepository.findOverlappingSchedules(
                doctor.getDoctorId(), dto.getDayOfWeek(), startTime, endTime
        );

        if (!overlapping.isEmpty()) {
            throw new TimeSlotOverlapException("MSG17: Khung giờ này bị trùng với một lịch đã có.");
        }

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);
        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setConsultationType(dto.getConsultationType());
        scheduleRepository.save(schedule);
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

        // Kiểm tra logic thời gian và trùng lặp tương tự như khi thêm mới
        LocalTime startTime = LocalTime.parse(dto.getStartTime());
        LocalTime endTime = LocalTime.parse(dto.getEndTime());

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
        dto.setStartTime(schedule.getStartTime().toString());
        dto.setEndTime(schedule.getEndTime().toString());
        dto.setConsultationType(schedule.getConsultationType());
        return dto;
    }
}