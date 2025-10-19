package com.medconnect.repository;

import com.medconnect.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    @Query("SELECT r FROM Review r WHERE r.appointment.doctor.doctorId = :doctorId")
    List<Review> findByDoctorId(Integer doctorId);

    Optional<Review> findByAppointmentAppointmentId(Integer appointmentId);
}