package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.bankAccountDto.DetailsBankAccountDto;
import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/bankAccount")
@RequiredArgsConstructor
public class BankAccountDto {

    private final BankAccountService bankAccountService;

    @PostMapping("/register")
    public ResponseEntity<DetailsBankAccountDto> registerBankAccount(@RequestBody RegisterBankAccountDto bankAccountDto,
                                                                     UriComponentsBuilder uriBuilder, Authentication authentication) {
        var bankAccount = bankAccountService.registerBankAccount(authentication, bankAccountDto);
        var uri = uriBuilder.path("bankAccount/{id}").buildAndExpand(bankAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsBankAccountDto(bankAccount));
    }
}
