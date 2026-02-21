package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.model.Consistency;
import com.henrique.picpaysimplified.model.Transaction;
import com.henrique.picpaysimplified.repository.TransactionRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Transaction registerTransaction(Authentication authentication, RegisterTransactionalDto transactionDto) {
        var payer = findUserAuthenticatedByEmail(authentication);
        var payee = userRepository.findById(transactionDto.idPayee()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payee not found")
        );


        Transaction transaction = new Transaction(transactionDto, payer, payee);

        validateHasBalance(payer.getBankAccount().getBalance(), transactionDto.value());
        transfer(payer, payee, transactionDto.value());

        payer.addTransaction(transaction);
        return transactionRepository.save(transaction);
    }

    public void transfer(com.henrique.picpaysimplified.model.User payer, com.henrique.picpaysimplified.model.User payee, BigDecimal value) {
        payer.getBankAccount().transerBalance(value);
        payee.getBankAccount().receiveBalance(value);
        userRepository.save(payer);
        userRepository.save(payee);
    }

    public void validateHasBalance(BigDecimal payerBalance, BigDecimal transactionValue) {
        if(payerBalance.compareTo(transactionValue) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance.");
        }
    }

    public User validateAuthentication(Authentication authentication) {
        var authenticated = (User) authentication.getPrincipal();
        if(authenticated == null) {
            throw new SecurityException("User not authenticated.");
        }
        return authenticated;
    }

    public com.henrique.picpaysimplified.model.User findUserAuthenticatedByEmail(Authentication authentication) {
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
