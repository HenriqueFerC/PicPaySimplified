package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import com.henrique.picpaysimplified.exceptions.ResourceNotFoundException;
import com.henrique.picpaysimplified.model.BankAccount;
import com.henrique.picpaysimplified.repository.BankAccountRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccount registerBankAccount(String email, RegisterBankAccountDto bankAccountDto) {
        validateAgencyDoesNotExist(bankAccountDto.agency());
        validateAccountNumberDoesNotExist(bankAccountDto.accountNumber());

        var user = findUserAuthenticatedByEmail(email);
        BankAccount bankAccount = new BankAccount(bankAccountDto, user);
        user.setBankAccount(bankAccount);
        return bankAccountRepository.save(bankAccount);
    }


    public BankAccount findBankAccountByUser(String email) {
        var user = findUserAuthenticatedByEmail(email);
        return bankAccountRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Bank Account not Found"));
    }

    private void validateAgencyDoesNotExist(Integer agency) {
        boolean exists = bankAccountRepository.existsBankAccountsByAgency(agency);
        if (exists) {
            throw new ConflictException("Agency already exists " + agency);
        }
    }

    private void validateAccountNumberDoesNotExist(Integer accountNumber) {
        boolean exists = bankAccountRepository.existsBankAccountsByAccountNumber(accountNumber);
        if (exists) {
            throw new ConflictException("Account Number already exists " + accountNumber);
        }
    }

    private com.henrique.picpaysimplified.model.User findUserAuthenticatedByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found " + email)
        );
    }
}
