package com.henrique.picpaysimplified.dtos.bankAccountDto;

import java.math.BigDecimal;

public record RegisterBankAccountDto(Integer agency, Integer accountNumber, BigDecimal balance) {
}
