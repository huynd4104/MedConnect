package com.medconnect.dto;

import com.medconnect.entity.ConsultationDocument.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConsultationDocumentDTO {
    @NotNull(message = "MSG25: Prescription does not meet regulations.")
    private Integer appointmentId;

    @NotNull(message = "MSG25: Document type is required.")
    private DocumentType documentType;

    @NotBlank(message = "MSG25: Prescription does not meet regulations.")
    private String content;
}