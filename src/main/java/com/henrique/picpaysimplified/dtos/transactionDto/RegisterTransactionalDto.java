package com.henrique.picpaysimplified.dtos.transactionDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterTransactionalDto(
        @NotNull(message = "Value is required")
        @Min(value = 0, message = "Value must be a positive number")
        @Schema(description = "Amount to be transferred", example = "100.00")
        BigDecimal value,
        @NotNull(message = "Payee ID is required")
        @Min(value = 0, message = "Payee ID must be a positive integer")
        @Schema(description = "ID of the payee user", example = "1")
        Integer idPayee) {
}
