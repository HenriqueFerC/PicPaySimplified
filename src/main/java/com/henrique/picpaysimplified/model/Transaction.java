package com.henrique.picpaysimplified.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private Integer id;

    @Column(name = "value", nullable = false)
    private BigDecimal value;

    @ManyToOne
    @JoinColumn(name = "id_payer", nullable = false)
    private User payer;

    @ManyToOne
    @JoinColumn(name = "id_payee", nullable = false)
    private User payee;

    @Column(name = "transaction_date")
    @CreatedDate
    private LocalDateTime transactionDate;

    @Column(name = "consistency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Consistency consistency;
}
