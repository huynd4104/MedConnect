package com.medconnect.service;

import com.medconnect.entity.Doctor;
import com.medconnect.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Quan trọng: Thêm dòng import này

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final DoctorRepository doctorRepository;

    @Cacheable("doctors")
    public List<Doctor> searchDoctors(String name, Integer specializationId, String location) {
        // Ưu tiên tìm theo chuyên khoa nếu có
        if (specializationId != null) {
            return doctorRepository.findApprovedBySpecializationId(specializationId);
        }

        if (!StringUtils.hasText(name)) {
            return doctorRepository.findByStatus(Doctor.Status.Approved);
        }

        return doctorRepository.searchApprovedDoctors(name);
    }
}