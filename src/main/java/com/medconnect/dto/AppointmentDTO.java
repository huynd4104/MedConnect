package com.medconnect.dto;

import com.medconnect.entity.Appointment.ConsultationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppointmentDTO {
    @NotNull(message = "MSG21: Time slot taken. Choose another.")
    private Integer doctorId;

    @NotBlank(message = "Vui lòng chọn ngày hẹn.")
    private String appointmentDate;

    @NotBlank(message = "Vui lòng chọn một khung giờ (slot).")
    private String appointmentTime;

    @NotNull(message = "MSG21: Consultation type is required.")
    private ConsultationType consultationType;

    private String medicalInfo;
}