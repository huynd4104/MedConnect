package com.medconnect.repository;

import com.medconnect.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByDoctor_DoctorIdAndActiveTrue(Integer doctorId);

    @Query("SELECT s FROM Schedule s WHERE s.doctor.doctorId = :doctorId " +
            "AND s.dayOfWeek = :dayOfWeek " +
            "AND s.startTime < :endTime AND s.endTime > :startTime " + // Correct overlap logic
            "AND s.active = true")
    List<Schedule> findOverlappingSchedules(
            @Param("doctorId") Integer doctorId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}