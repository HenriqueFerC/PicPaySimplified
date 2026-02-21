package com.henrique.picpaysimplified.repository;

import com.henrique.picpaysimplified.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    boolean existsBankAccountsByAccountNumber(Integer accountNumber);
    boolean existsBankAccountsByAgency(Integer agency);
}
