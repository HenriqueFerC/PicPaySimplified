package com.henrique.picpaysimplified.dtos.transactionDto;

import com.henrique.picpaysimplified.model.Consistency;

public record UpdateTransactionDto(Integer id, Consistency consistency) {
}
