package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import com.henrique.picpaysimplified.exceptions.ResourceNotFoundException;
import com.henrique.picpaysimplified.exceptions.UnauthorizedException;
import com.henrique.picpaysimplified.model.BankAccount;
import com.henrique.picpaysimplified.repository.BankAccountRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccount registerBankAccount(Authentication authentication, RegisterBankAccountDto bankAccountDto) {
        validateAgencyDoesNotExist(bankAccountDto.agency());
        validateAccountNumberDoesNotExist(bankAccountDto.accountNumber());

        var user = findUserAuthenticatedByEmail(authentication);
        BankAccount bankAccount = new BankAccount(bankAccountDto, user);
        user.setBankAccount(bankAccount);
        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount withdraw(Authentication authentication, BigDecimal amount) {
        var user = findUserAuthenticatedByEmail(authentication);
        var bankAccount = user.getBankAccount();
        if(bankAccount == null) {
            throw new ResourceNotFoundException("User does not have a bank account.");
        }
        bankAccount.withdraw(amount);
        return bankAccountRepository.save(bankAccount);
    }

    public BankAccount deposit(Authentication authentication, BigDecimal amount) {
        var user = findUserAuthenticatedByEmail(authentication);
        var bankAccount = user.getBankAccount();
        if (bankAccount == null) {
            throw new ResourceNotFoundException("User does not have a bank account.");
        }
        bankAccount.deposit(amount);
        return bankAccountRepository.save(bankAccount);
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

    private User validateAuthentication(Authentication authentication) {
        var authenticated = (User) authentication.getPrincipal();
        if (authenticated == null) {
            throw new UnauthorizedException("User not authenticated.");
        }
        return authenticated;
    }

    private com.henrique.picpaysimplified.model.User findUserAuthenticatedByEmail(Authentication authentication) {
        var user = validateAuthentication(authentication);
        return userRepository.findByEmail(user.getUsername()).orElseThrow(
                () -> new ResourceNotFoundException("User not found " + user.getUsername())
        );
    }
}
