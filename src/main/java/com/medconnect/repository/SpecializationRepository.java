package com.medconnect.repository;

import com.medconnect.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Integer> {
    Optional<Specialization> findByName(String name);

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.specialization.specializationId = :id")
    long countDoctorsBySpecializationId(Integer id);
}