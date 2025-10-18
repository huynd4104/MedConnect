package com.medconnect.repository;

import com.medconnect.entity.Patient;
import com.medconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    // Thêm phương thức này để tìm Patient bằng User
    Optional<Patient> findByUser(User user);
}