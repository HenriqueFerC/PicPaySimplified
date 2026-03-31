package com.henrique.picpaysimplified.dtos.mailNotificationDto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record MailMessageDto(
        @Schema(description = "Full name of Payer.", example = "Henrique Ferreira Cardoso")
        String payerFullName,
        @Schema(description = "Full name of Payee.", example = "John Does Alfred")
        String payeeFullName,
        @Schema(description = "Email of Payee.", example = "johndoes@email.com")
        String payeeEmail,
        @Schema(description = "Amount transferred by Payer.", example = "100")
        BigDecimal amount) {
}
