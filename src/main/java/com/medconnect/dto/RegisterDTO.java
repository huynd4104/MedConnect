package com.medconnect.dto;

import com.medconnect.entity.User.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterDTO {
    @NotBlank(message = "MSG03: Invalid email format. Please enter a valid email.")
    @Email(message = "MSG03: Invalid email format. Please enter a valid email.")
    private String email;

    @NotBlank(message = "MSG02: Password must be at least 8 characters with letters and numbers.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "MSG02: Password must be at least 8 characters with letters and numbers.")
    private String password;

    @NotBlank(message = "MSG02: Password confirmation is required.")
    private String confirmPassword;

    @NotNull(message = "Account type is required.")
    private Role role;
}