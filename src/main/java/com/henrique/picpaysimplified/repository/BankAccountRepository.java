package com.henrique.picpaysimplified.repository;

import com.henrique.picpaysimplified.model.BankAccount;
import com.henrique.picpaysimplified.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Integer> {
    boolean existsBankAccountsByAccountNumber(Integer accountNumber);

    boolean existsBankAccountsByAgency(Integer agency);

    Optional<BankAccount> findByUser(User user);
}
