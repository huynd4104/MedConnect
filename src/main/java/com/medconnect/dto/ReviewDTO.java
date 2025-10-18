package com.medconnect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDTO {
    @NotNull(message = "MSG27: Cannot review until consultation is complete.")
    private Integer appointmentId;

    @Min(value = 1, message = "MSG28: Review contains invalid content.")
    @Max(value = 5, message = "MSG28: Review contains invalid content.")
    private Integer rating;

    private String comment;

    private Boolean anonymous = false;
}