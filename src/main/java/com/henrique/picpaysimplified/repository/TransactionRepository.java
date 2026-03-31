package com.henrique.picpaysimplified.repository;

import com.henrique.picpaysimplified.model.Transaction;
import com.henrique.picpaysimplified.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByPayerOrPayeeAndTransactionDateBetween(User payer, User payee, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Transaction> findAllByPayerOrPayee(User payer, User payee, Pageable pageable);
}
