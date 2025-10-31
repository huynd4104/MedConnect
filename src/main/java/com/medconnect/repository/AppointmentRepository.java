package com.medconnect.repository;

import com.medconnect.entity.Appointment;
import com.medconnect.entity.Appointment.Status;
import com.medconnect.entity.Appointment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findByPatientPatientId(Integer patientId);

    List<Appointment> findByDoctorDoctorId(Integer doctorId);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime >= :start AND a.appointmentDateTime <= :end " +
            "AND a.status = 'Pending' AND a.paymentStatus = 'Pending'")
    List<Appointment> findUnpaidAppointments(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime >= :start AND a.appointmentDateTime <= :end " +
            "AND a.status = 'Confirmed'")
    List<Appointment> findConfirmedAppointmentsForReminder(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.doctorId = :doctorId " +
            "AND a.appointmentDateTime >= :startOfDay AND a.appointmentDateTime < :endOfDay " +
            "AND a.status IN ('Pending', 'Confirmed')")
    List<Appointment> findBookedAppointmentsByDate(
            @Param("doctorId") Integer doctorId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime < :threshold AND a.status IN ('Completed', 'Cancelled')")
    List<Appointment> findAppointmentsToArchive(LocalDateTime threshold);
}