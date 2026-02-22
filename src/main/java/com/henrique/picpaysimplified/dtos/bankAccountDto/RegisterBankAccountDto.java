package com.henrique.picpaysimplified.dtos.bankAccountDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RegisterBankAccountDto(
        @NotNull(message = "Agency is required")
        @Min(value = 0, message = "Agency must be a positive integer")
        @Schema(description = "Bank agency number", example = "1234")
        Integer agency,
        @NotNull(message = "Account number is required")
        @Min(value = 0, message = "Account number must be a positive integer")
        @Schema(description = "Bank account number", example = "567890")
        Integer accountNumber,
        @NotNull(message = "Initial balance is required")
        @Min(value = 0, message = "Initial balance must be a positive value")
        @Schema(description = "Initial balance for the bank account", example = "1000.00")
        BigDecimal balance) {
}
