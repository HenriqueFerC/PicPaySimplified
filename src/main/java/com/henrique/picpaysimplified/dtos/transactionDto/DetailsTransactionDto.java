package com.henrique.picpaysimplified.dtos.transactionDto;

import com.henrique.picpaysimplified.model.Consistency;
import com.henrique.picpaysimplified.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetailsTransactionDto(Integer id, BigDecimal value, Integer idPayer, Integer idPayee,
                                    LocalDateTime transactionDate, Consistency consistency) {
    public DetailsTransactionDto(Transaction transaction) {
        this(transaction.getId(), transaction.getValue(), transaction.getPayer().getId(), transaction.getPayee().getId(), transaction.getTransactionDate(), transaction.getConsistency());
    }
}
