package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.exceptions.CredentialException;
import com.henrique.picpaysimplified.model.BankAccount;
import com.henrique.picpaysimplified.repository.BankAccountRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public BankAccount registerBankAccount(Authentication authentication, RegisterBankAccountDto bankAccountDto) {
        existsAgency(bankAccountDto.agency());
        existsAccountNumber(bankAccountDto.accountNumber());

        var user = findUserByEmail(authentication);
        BankAccount bankAccount = new BankAccount(bankAccountDto, user);
        user.setBankAccount(bankAccount);
        bankAccountRepository.save(bankAccount);
        return bankAccountRepository.save(bankAccount);
    }

    public void existsAgency(Integer agency) {
        try {
            boolean exists = bankAccountRepository.existsBankAccountsByAgency(agency);
            if (exists) {
                throw new CredentialException("Agency already exists " + agency);
            }
        } catch (CredentialException e) {
            throw new CredentialException("Agency already exists" + e.getCause());
        }
    }

    public void existsAccountNumber(Integer accountNumber) {
        try {
            boolean exists = bankAccountRepository.existsBankAccountsByAccountNumber(accountNumber);
            if (exists) {
                throw new CredentialException("Account Number already exists " + accountNumber);
            }
        } catch (CredentialException e) {
            throw new CredentialException("Account Number already exists" + e.getCause());
        }
    }

    public User validateAuthentication(Authentication authentication) {
        var authenticated = (User) authentication.getPrincipal();
        if(authenticated == null) {
            throw new SecurityException("User not authenticated.");
        }
        return authenticated;
    }

    public com.henrique.picpaysimplified.model.User findUserByEmail(Authentication authentication) {
        try {
            var user = validateAuthentication(authentication);
            return userRepository.findByEmail(user.getUsername()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.", e.getCause());
        }
    }
}
