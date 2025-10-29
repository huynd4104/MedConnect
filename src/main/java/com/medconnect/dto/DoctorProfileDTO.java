package com.medconnect.dto;

import com.medconnect.entity.DoctorDocument;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DoctorProfileDTO {
    @NotNull(message = "MSG09: All fields are required. Please complete the form.")
    private Integer specializationId;

    @Min(value = 0, message = "MSG09: Experience years must be a positive number.")
    private Integer experienceYears;

    @NotBlank(message = "MSG09: All fields are required. Please complete the form.")
    private String licenseNumber;

    private MultipartFile photo;

    private MultipartFile[] credentials;

    private String existingPhotoPath;

    private List<DoctorDocument> existingDocuments;

    @NotBlank(message = "Họ tên không được để trống.")
    private String fullName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Số điện thoại không hợp lệ.")
    private String phoneNumber;

    private String clinicAddress;
}
