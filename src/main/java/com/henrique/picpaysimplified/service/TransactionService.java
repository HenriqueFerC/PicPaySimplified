package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.transactionDto.DetailsTransactionDto;
import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.model.Consistency;
import com.henrique.picpaysimplified.model.Transaction;
import com.henrique.picpaysimplified.model.TypeUser;
import com.henrique.picpaysimplified.repository.TransactionRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.security.auth.login.CredentialException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public Transaction registerTransaction(Authentication authentication, RegisterTransactionalDto transactionDto) {
        try {
            var payer = findUserAuthenticatedByEmail(authentication);
            var payee = userRepository.findById(transactionDto.idPayee()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payee not found")
            );

            if (payer.getTypeUser().equals(TypeUser.shopkeeper)) {
                throw new CredentialException("Shopkeepers are not allowed to make transactions.");
            }

            Transaction transaction = new Transaction(transactionDto, payer, payee);

            validateHasBalance(payer.getBankAccount().getBalance(), transactionDto.value());
            transfer(payer, payee, transactionDto.value());

            payer.addTransaction(transaction);
            return transactionRepository.save(transaction);
        } catch (CredentialException e) {
            throw new RuntimeException("Shopkeepers are not allowed to make transactions." + e.getCause());
        }

    }

    @Transactional
    public Transaction revertTransaction(Authentication authentication, Integer id) {
        var payer = findUserAuthenticatedByEmail(authentication);

        var transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found ")
        );

        if (!transaction.getPayer().getId().equals(payer.getId())) {
            throw new SecurityException("User not authorized to revert this transaction.");
        }

        revertTransfer(payer, transaction.getPayee(), transaction.getValue());
        transaction.setConsistency(Consistency.reverted);
        return transaction;
    }

    @Transactional(readOnly = true)
    public Page<DetailsTransactionDto> listTransactions(Authentication authentication, Pageable pageable) {
        var payer = findUserAuthenticatedByEmail(authentication);
        return transactionRepository.findAllByPayer(payer, pageable).map(DetailsTransactionDto::new);
    }

    @Transactional
    public void transfer(com.henrique.picpaysimplified.model.User payer, com.henrique.picpaysimplified.model.User payee, BigDecimal value) {
        payer.getBankAccount().transerBalance(value);
        payee.getBankAccount().receiveBalance(value);
        userRepository.save(payer);
        userRepository.save(payee);
    }

    @Transactional
    public void revertTransfer(com.henrique.picpaysimplified.model.User payer, com.henrique.picpaysimplified.model.User payee, BigDecimal value) {
        payee.getBankAccount().transerBalance(value);
        payer.getBankAccount().receiveBalance(value);
        userRepository.save(payer);
        userRepository.save(payee);
    }

    public void validateHasBalance(BigDecimal payerBalance, BigDecimal transactionValue) {
        if (payerBalance.compareTo(transactionValue) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance: " + payerBalance);
        }
    }

    public User userAuthenticated(Authentication authentication) {
        var authenticated = (User) authentication.getPrincipal();
        if (authenticated == null) {
            throw new SecurityException("User not authenticated.");
        }
        return authenticated;
    }

    public com.henrique.picpaysimplified.model.User findUserAuthenticatedByEmail(Authentication authentication) {
        try {
            var user = userAuthenticated(authentication);
            return userRepository.findByEmail(user.getUsername()).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.", e.getCause());
        }
    }

}
