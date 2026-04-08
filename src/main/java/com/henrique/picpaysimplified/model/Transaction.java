package com.henrique.picpaysimplified.model;

import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_payer", nullable = false)
    private User payer;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_payee")
    private User payee;

    @Column(name = "transaction_date")
    @CreatedDate
    private LocalDateTime transactionDate;

    @Column(name = "type_transaction", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "consistency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Consistency consistency;

    public Transaction(RegisterTransactionalDto transactionalDto, User payer, User payee) {
        value = transactionalDto.value();
        consistency = Consistency.completed;
        transactionType = transactionalDto.transactionType();
        this.payer = payer;
        this.payee = payee;
    }

    public void withdraw(BigDecimal withdrawValue) {
        BankAccount bankAccount = payer.getBankAccount();
        BigDecimal balance = bankAccount.getBalance();
        if (withdrawValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("Withdraw amount must be a positive number!");
        }
        if (balance.compareTo(withdrawValue) < 0) {
            throw new ConflictException("Insufficient balance: " + balance);
        }
        balance = balance.subtract(withdrawValue);
        bankAccount.setBalance(balance);
    }

    public void deposit(BigDecimal depositValue) {
        BankAccount bankAccount = payer.getBankAccount();
        BigDecimal balance = bankAccount.getBalance();
        if (depositValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("Deposit amount must be a positive number!");
        }
        balance = balance.add(depositValue);
        bankAccount.setBalance(balance);
    }
}
