package com.medconnect.repository;

import com.medconnect.entity.Doctor;
import com.medconnect.entity.User;
import com.medconnect.entity.Doctor.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    Optional<Doctor> findByUser(User user);

    List<Doctor> findByStatus(Status status);

    @Query("SELECT d FROM Doctor d WHERE d.specialization.specializationId = :specializationId AND d.status = 'Approved'")
    List<Doctor> findApprovedBySpecializationId(Integer specializationId);

    @Query("SELECT d FROM Doctor d WHERE d.status = 'Approved' AND " +
            "(:name IS NULL OR d.user.email LIKE %:name% OR d.specialization.name LIKE %:name%)")
    List<Doctor> searchApprovedDoctors(String name);

    Optional<Doctor> findByUser_UserId(Integer userId);
}