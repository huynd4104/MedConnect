package com.medconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordDTO {
    @NotBlank
    private String token;

    @NotBlank(message = "MSG02: Password must be at least 8 characters with letters and numbers.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "MSG02: Password must be at least 8 characters with letters and numbers.")
    private String password;

    @NotBlank(message = "MSG02: Password confirmation is required.")
    private String confirmPassword;
}