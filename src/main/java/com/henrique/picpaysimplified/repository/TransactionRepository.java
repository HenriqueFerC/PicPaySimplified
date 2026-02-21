package com.henrique.picpaysimplified.repository;

import com.henrique.picpaysimplified.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
