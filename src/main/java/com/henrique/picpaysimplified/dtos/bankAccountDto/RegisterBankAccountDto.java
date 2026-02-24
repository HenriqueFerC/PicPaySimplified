package com.henrique.picpaysimplified.dtos.bankAccountDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record RegisterBankAccountDto(
        @NotNull(message = "Agency is required")
        @Min(value = 0, message = "Agency must be a positive integer")
        @Length(min = 4, max = 4, message = "Agency number must be exactly 4 digits")
        @Schema(description = "Bank agency number", example = "1234")
        Integer agency,
        @NotNull(message = "Account number is required")
        @Min(value = 0, message = "Account number must be a positive integer")
        @Length(min = 6, max = 6, message = "Account number must be exactly 6 digits")
        @Schema(description = "Bank account number", example = "567890")
        Integer accountNumber,
        @NotNull(message = "Initial balance is required")
        @Min(value = 0, message = "Initial balance must be a positive value")
        @Schema(description = "Initial balance for the bank account", example = "1000.00")
        BigDecimal balance) {
}
