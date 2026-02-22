package com.henrique.picpaysimplified.dtos.transactionDto;

import java.math.BigDecimal;

public record RegisterTransactionalDto(BigDecimal value, Integer idPayee) {
}
