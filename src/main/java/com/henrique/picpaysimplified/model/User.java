package com.henrique.picpaysimplified.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.henrique.picpaysimplified.dtos.userDto.RegisterUserDto;
import com.henrique.picpaysimplified.dtos.userDto.UpdateUserDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tb_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Integer id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "cpf_cnpj", nullable = false, unique = true, length = 18)
    private String cpfCnpj;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "user_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeUser typeUser;

    @OneToMany(mappedBy = "payer")
    private List<Transaction> transactions;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private BankAccount bankAccount;

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public User(RegisterUserDto userDto) {
        fullName = userDto.fullName();
        cpfCnpj = userDto.cpfCnpj();
        email = userDto.email();
        password = userDto.password();
        typeUser = userDto.typeUser();
    }

    public void updateUser(String fullName, String cpfCnpj, String email, String password, TypeUser typeUser) {
        this.fullName = fullName;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.password = password;
        this.typeUser = typeUser;
    }
}
