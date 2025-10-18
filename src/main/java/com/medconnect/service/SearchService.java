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

        // Kiểm tra xem `name` có rỗng hoặc null không
        // StringUtils.hasText() sẽ trả về false nếu name là null, "", hoặc chỉ chứa khoảng trắng
        if (!StringUtils.hasText(name)) {
            // Nếu không có tiêu chí tên, gọi phương thức đơn giản hơn để lấy TẤT CẢ bác sĩ đã được duyệt
            return doctorRepository.findByStatus(Doctor.Status.Approved);
        }

        // Nếu có tên, thực hiện tìm kiếm như cũ
        return doctorRepository.searchApprovedDoctors(name);
    }
}