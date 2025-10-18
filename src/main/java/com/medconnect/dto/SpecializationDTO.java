package com.medconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecializationDTO {
    @NotBlank(message = "MSG15: Specialization already exists.")
    private String name;

    private String description;
}