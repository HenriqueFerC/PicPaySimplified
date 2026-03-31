package com.henrique.picpaysimplified.dtos.transactionDto;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.model.Consistency;
import com.henrique.picpaysimplified.model.Transaction;
import com.henrique.picpaysimplified.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DetailsTransactionDto(Integer id, BigDecimal value, DetailsUserDto payer, DetailsUserDto payee,
                                    LocalDateTime transactionDate, TransactionType transactionType, Consistency consistency) {
    public DetailsTransactionDto(Transaction transaction) {
        this(transaction.getId(), transaction.getValue(), new DetailsUserDto(transaction.getPayer()),
                transaction.getPayee() != null ? new DetailsUserDto(transaction.getPayee()): null,
                transaction.getTransactionDate(), transaction.getTransactionType(), transaction.getConsistency());
    }
}
