package com.henrique.picpaysimplified.model;

import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.exceptions.CredentialException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bank_account")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "agency", nullable = false, unique = true)
    private Integer agency;

    @Column(name = "account_number", nullable = false, unique = true)
    private Integer accountNumber;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @OneToOne
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    private User user;

    public BankAccount(RegisterBankAccountDto bankAccountDto, User user) {
        agency = bankAccountDto.agency();
        accountNumber = bankAccountDto.accountNumber();
        balance = bankAccountDto.balance();
        this.user = user;
    }

    public void transerBalance(BigDecimal value) {
        balance = balance.subtract(value);
    }

    public void receiveBalance(BigDecimal value) {
        balance = balance.add(value);
    }

    public void withdraw(BigDecimal withdrawValue) {
        if(balance.compareTo(withdrawValue) < 0) {
            throw new CredentialException("Insufficient balance: " + balance);
        }
        balance = balance.subtract(withdrawValue);
    }

    public void deposit(BigDecimal depositValue) {
        balance = balance.add(depositValue);
    }
}
