package com.medconnect.repository;

import com.medconnect.entity.ConsultationDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationDocumentRepository extends JpaRepository<ConsultationDocument, Integer> {
    Optional<ConsultationDocument> findByAppointmentAppointmentId(Integer appointmentId);
}