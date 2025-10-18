package com.medconnect.repository;

import com.medconnect.entity.DoctorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorDocumentRepository extends JpaRepository<DoctorDocument, Integer> {
    List<DoctorDocument> findByDoctorDoctorId(Integer doctorId);
}