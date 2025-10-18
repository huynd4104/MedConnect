package com.medconnect.service;

import com.medconnect.dto.SpecializationDTO;
import com.medconnect.entity.Specialization;
import com.medconnect.repository.SpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final SpecializationRepository specializationRepository;

    public List<Specialization> getSpecializations() {
        return specializationRepository.findAll();
    }

    public void addSpecialization(SpecializationDTO dto) {
        if (specializationRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("MSG15: Specialization already exists.");
        }
        Specialization spec = new Specialization();
        spec.setName(dto.getName());
        spec.setDescription(dto.getDescription());
        specializationRepository.save(spec);
    }

    public void updateSpecialization(Integer id, SpecializationDTO dto) {
        Specialization spec = specializationRepository.findById(id).orElseThrow();
        spec.setName(dto.getName());
        spec.setDescription(dto.getDescription());
        specializationRepository.save(spec);
    }

    public void deleteSpecialization(Integer id) {
        if (specializationRepository.countDoctorsBySpecializationId(id) > 0) {
            throw new RuntimeException("MSG16: Cannot delete specialization in use.");
        }
        specializationRepository.deleteById(id);
    }
}