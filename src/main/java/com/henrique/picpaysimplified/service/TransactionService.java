package com.henrique.picpaysimplified.service;

import com.henrique.picpaysimplified.dtos.transactionDto.DetailsTransactionDto;
import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.exceptions.ConflictException;
import com.henrique.picpaysimplified.exceptions.ResourceNotFoundException;
import com.henrique.picpaysimplified.exceptions.UnauthorizedException;
import com.henrique.picpaysimplified.model.*;
import com.henrique.picpaysimplified.repository.TransactionRepository;
import com.henrique.picpaysimplified.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Transaction registerTransaction(String email, RegisterTransactionalDto transactionDto) {
        var payer = findUserAuthenticatedByEmail(email);
        var payee = userRepository.findById(transactionDto.idPayee()).orElseThrow(
                () -> new ResourceNotFoundException("Payee not found " + transactionDto.idPayee())
        );

        if (payee.getId().equals(payer.getId())) {
            throw new ConflictException("Payer and payee cannot be the same user.");
        }

        if (payer.getUserType().equals(UserType.shopkeeper)) {
            throw new UnauthorizedException("Shopkeepers are not allowed to make transactions.");
        }

        boolean authorized = restTemplate.authorizeTransaction();
        if (!authorized) {
            throw new UnauthorizedException("Transaction not authorized by external service.");
        }

        validateHasBalance(payer.getBankAccount().getBalance(), transactionDto.value());
        doTransaction(payer, payee, transactionDto.value());

        Transaction transaction = new Transaction(transactionDto, payer, payee);

        payer.addTransaction(transaction);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction revertTransaction(String email, Integer id) {
        var payer = findUserAuthenticatedByEmail(email);

        var transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction ID not found!")
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
    public DetailsTransactionDto findTransactionById(int id, String email) {
        var transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction ID Not Found!"));
        if (!transaction.getPayer().getEmail().equals(email)) {
            System.out.println(email);
            System.out.println(transaction.getPayer().getEmail());
            throw new ResourceNotFoundException("You cannot see transaction that are not yours");
        }
        return new DetailsTransactionDto(transaction);
    }

    @Transactional(readOnly = true)
    public Page<DetailsTransactionDto> listTransactions(String email, Pageable pageable) {
        var payer = findUserAuthenticatedByEmail(email);
        return transactionRepository.findAllByPayer(payer, pageable).map(DetailsTransactionDto::new);
    }

    @Transactional(readOnly = true)
    public Page<DetailsTransactionDto> listLastTransactions(Integer days, String email, Pageable pageable) {
        var payer = findUserAuthenticatedByEmail(email);
        var startDate = LocalDateTime.now().minusDays(days);
        var endDate = LocalDateTime.now();
        return transactionRepository.findByPayerAndTransactionDateBetween(payer, startDate, endDate, pageable).map(DetailsTransactionDto::new);
    }

    @Transactional
    public Transaction withdraw(String email, BigDecimal amount) {
        var payer = findUserAuthenticatedByEmail(email);
        RegisterTransactionalDto transactionalDto =  new RegisterTransactionalDto(amount, null, TransactionType.withdraw);
        Transaction transaction = new Transaction(transactionalDto, payer, null);
        transaction.withdraw(amount);
        transactionRepository.save(transaction);
        return transaction;
    }

    @Transactional
    public Transaction deposit(String email, BigDecimal amount) {
        var payer = findUserAuthenticatedByEmail(email);
        RegisterTransactionalDto transactionalDto =  new RegisterTransactionalDto(amount, null, TransactionType.deposit);
        Transaction transaction = new Transaction(transactionalDto, payer, null);
        transaction.deposit(amount);
        transactionRepository.save(transaction);
        return transaction;
    }

    private void doTransaction(com.henrique.picpaysimplified.model.User payer, com.henrique.picpaysimplified.model.User payee, BigDecimal value) {
        if (payer != null) {
            BankAccount bankAccountPayer = payer.getBankAccount();
            bankAccountPayer.transferBalance(value);
        }
        if (payee != null) {
            BankAccount bankAccountPayee = payee.getBankAccount();
            bankAccountPayee.receiveBalance(value);
        }
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

    private com.henrique.picpaysimplified.model.User findUserAuthenticatedByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not Found!")
        );
    }

}
