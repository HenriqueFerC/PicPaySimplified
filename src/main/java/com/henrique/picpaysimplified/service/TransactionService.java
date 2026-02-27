package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.transactionDto.DetailsTransactionDto;
import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import com.henrique.picpaysimplified.exceptions.ResourceNotFoundException;
import com.henrique.picpaysimplified.exceptions.UnauthorizedException;
import com.henrique.picpaysimplified.model.BankAccount;
import com.henrique.picpaysimplified.model.Consistency;
import com.henrique.picpaysimplified.model.Transaction;
import com.henrique.picpaysimplified.model.TypeUser;
import com.henrique.picpaysimplified.repository.BankAccountRepository;
import com.henrique.picpaysimplified.repository.TransactionRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private final RestTemplateService restTemplate;

    @Transactional
    public Transaction registerTransaction(Authentication authentication, RegisterTransactionalDto transactionDto) {
        var payer = findUserAuthenticatedByEmail(authentication);
        var payee = userRepository.findById(transactionDto.idPayee()).orElseThrow(
                () -> new ResourceNotFoundException("Payee not found " + transactionDto.idPayee())
        );

        if (payee.getId().equals(payer.getId())) {
            throw new ConflictException("Payer and payee cannot be the same user.");
        }

        if (payer.getTypeUser().equals(TypeUser.shopkeeper)) {
            throw new UnauthorizedException("Shopkeepers are not allowed to make transactions.");
        }

        boolean authorized = restTemplate.authorizeTransaction();
        if (!authorized) {
            throw new UnauthorizedException("Transaction not authorized by external service.");
        }

        validateHasBalance(payer.getBankAccount().getBalance(), transactionDto.value());
        transfer(payer, payee, transactionDto.value());

        Transaction transaction = new Transaction(transactionDto, payer, payee);

        payer.addTransaction(transaction);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction revertTransaction(Authentication authentication, Integer id) {
        var payer = findUserAuthenticatedByEmail(authentication);

        var transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction ID not found " + id)
        );

        if (!transaction.getPayer().getId().equals(payer.getId())) {
            throw new UnauthorizedException("User not authorized to revert this transaction.");
        }

        revertTransfer(payer, transaction.getPayee(), transaction.getValue());
        transaction.setConsistency(Consistency.reverted);
        transactionRepository.save(transaction);
        return transaction;
    }

    @Transactional(readOnly = true)
    public Page<DetailsTransactionDto> listTransactions(Authentication authentication, Pageable pageable) {
        var payer = findUserAuthenticatedByEmail(authentication);
        return transactionRepository.findAllByPayer(payer, pageable).map(DetailsTransactionDto::new);
    }

    @Transactional(readOnly = true)
    public Page<DetailsTransactionDto> listLastTransactions(Integer days, Authentication authentication, Pageable pageable) {
        var payer = findUserAuthenticatedByEmail(authentication);
        var startDate = LocalDateTime.now().minusDays(days);
        var endDate = LocalDateTime.now();
        return transactionRepository.findByPayerAndTransactionDateBetween(payer, startDate, endDate, pageable).map(DetailsTransactionDto::new);
    }

    private void transfer(com.henrique.picpaysimplified.model.User payer, com.henrique.picpaysimplified.model.User payee, BigDecimal value) {
        BankAccount bankAccountPayer = payer.getBankAccount();
        bankAccountPayer.transferBalance(value);
        BankAccount bankAccountPayee = payee.getBankAccount();
        bankAccountPayee.receiveBalance(value);
    }

    private void revertTransfer(com.henrique.picpaysimplified.model.User payer, com.henrique.picpaysimplified.model.User payee, BigDecimal value) {
        BankAccount bankAccountPayee = payee.getBankAccount();
        bankAccountPayee.transferBalance(value);
        BankAccount bankAccountPayer = payer.getBankAccount();
        bankAccountPayer.receiveBalance(value);
    }

    private void validateHasBalance(BigDecimal payerBalance, BigDecimal transactionValue) {
        if (payerBalance.compareTo(transactionValue) < 0) {
            throw new ConflictException("Insufficient balance: " + payerBalance);
        }
    }

    private User userAuthenticated(Authentication authentication) {
        var authenticated = (User) authentication.getPrincipal();
        if (authenticated == null) {
            throw new UnauthorizedException("User not authenticated.");
        }
        return authenticated;
    }

    private com.henrique.picpaysimplified.model.User findUserAuthenticatedByEmail(Authentication authentication) {
        var user = userAuthenticated(authentication);
        return userRepository.findByEmail(user.getUsername()).orElseThrow(
                () -> new ResourceNotFoundException("User not Found!")
        );
    }

}
