package com.medconnect.dto;

import com.medconnect.entity.Appointment.ConsultationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AppointmentDTO {
    @NotNull(message = "MSG21: Time slot taken. Choose another.")
    private Integer doctorId;

    @NotNull(message = "MSG21: Time slot taken. Choose another.")
    private LocalDateTime appointmentDateTime;

    @NotNull(message = "MSG21: Consultation type is required.")
    private ConsultationType consultationType;

    private String medicalInfo;
}