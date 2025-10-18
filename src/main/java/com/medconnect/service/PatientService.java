package com.medconnect.service;

import com.medconnect.dto.PatientProfileDTO;
import com.medconnect.entity.Patient;
import com.medconnect.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public void updateProfile(Integer patientId, PatientProfileDTO dto) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        patient.setFullName(dto.getFullName());
        patient.setPhoneNumber(dto.getPhoneNumber());
        patient.setAddress(dto.getAddress());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}