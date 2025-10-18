package com.medconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginDTO {
    @NotBlank(message = "MSG04: Email and password are required.")
    private String email;

    @NotBlank(message = "MSG04: Email and password are required.")
    private String password;
}