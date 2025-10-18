package com.medconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PatientProfileDTO {
    @NotBlank(message = "MSG12: Invalid input format. Please correct and try again.")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$",
            message = "MSG12: Invalid input format. Please correct and try again.")
    private String phoneNumber;

    private String address;

    private LocalDate dateOfBirth;

    private String medicalHistory;
}