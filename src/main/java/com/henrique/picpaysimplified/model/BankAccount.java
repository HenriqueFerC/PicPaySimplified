package com.henrique.picpaysimplified.model;

import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
