package com.medconnect.dto;

import com.medconnect.entity.Payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTO {
    @NotNull(message = "MSG22: Payment failed. Please try again.")
    private Integer appointmentId;

    @NotNull(message = "MSG22: Payment failed. Please try again.")
    private BigDecimal amount;

    @NotNull(message = "MSG22: Payment method is required.")
    private PaymentMethod paymentMethod;
}