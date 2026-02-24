package com.henrique.picpaysimplified.dtos.bankAccountDto;

import com.henrique.picpaysimplified.dtos.userDto.DetailsUserDto;
import com.henrique.picpaysimplified.model.BankAccount;

import java.math.BigDecimal;

public record DetailsBankAccountDto(Integer agency, Integer accountNumber, BigDecimal value, DetailsUserDto userDto) {
    public DetailsBankAccountDto(BankAccount bankAccount) {
        this(bankAccount.getAgency(), bankAccount.getAccountNumber(), bankAccount.getBalance(), new DetailsUserDto(bankAccount.getUser()));
    }
}
