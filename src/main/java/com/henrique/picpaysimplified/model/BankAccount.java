package com.henrique.picpaysimplified.model;

import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import jakarta.persistence.*;
import lombok.*;

import javax.security.auth.login.CredentialException;
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

    @Column(name = "agency", nullable = false, unique = true, length = 4)
    private Integer agency;

    @Column(name = "account_number", nullable = false, unique = true, length = 6)
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

    public void transferBalance(BigDecimal value) {
        balance = balance.subtract(value);
    }

    public void receiveBalance(BigDecimal value) {
        balance = balance.add(value);
    }

    public void withdraw(BigDecimal withdrawValue) {
            if (balance.compareTo(withdrawValue) < 0) {
                throw new ConflictException("Insufficient balance: " + balance);
            }
            balance = balance.subtract(withdrawValue);
    }

    public void deposit(BigDecimal depositValue) {
        balance = balance.add(depositValue);
    }
}
