package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.transactionDto.DetailsTransactionDto;
import com.henrique.picpaysimplified.dtos.transactionDto.RegisterTransactionalDto;
import com.henrique.picpaysimplified.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<DetailsTransactionDto> toDoTransaction(@RequestBody RegisterTransactionalDto transactionalDto, Authentication authentication, UriComponentsBuilder uriBuilder) {
        var transaction = transactionService.registerTransaction(authentication, transactionalDto);
        var uri = uriBuilder.path("transaction/{id}").buildAndExpand(transaction.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsTransactionDto(transaction));
    }

    @GetMapping("/myTransactions")
    public ResponseEntity<Page<DetailsTransactionDto>> listTransactions(Authentication authentication, Pageable pageable) {
        var list = transactionService.listTransactions(authentication, pageable);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/revert/{id}")
    public ResponseEntity<DetailsTransactionDto> revertTransaction(@PathVariable Integer id, Authentication authentication) {
        var transaction = transactionService.revertTransaction(authentication, id);
        return ResponseEntity.ok(new DetailsTransactionDto(transaction));
    }
}
