package com.henrique.picpaysimplified.controller;

import com.henrique.picpaysimplified.dtos.bankAccountDto.DetailsBankAccountDto;
import com.henrique.picpaysimplified.dtos.bankAccountDto.RegisterBankAccountDto;
import com.henrique.picpaysimplified.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

@RestController
@RequestMapping("/bankAccount")
@RequiredArgsConstructor
@Tag(name = "Bank Account Controller", description = "Endpoints for bank account management")
public class BankAccountDto {

    private final BankAccountService bankAccountService;

    @PostMapping("/register")
    @Operation(summary = "Register a new bank account", description = "Endpoint to register a new bank account for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bank account successfully registered.",
                    content = @Content(schema = @Schema(implementation = DetailsBankAccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to register a bank account."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid bank account data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsBankAccountDto> registerBankAccount(@RequestBody RegisterBankAccountDto bankAccountDto, UriComponentsBuilder uriBuilder, Authentication authentication) {
        var bankAccount = bankAccountService.registerBankAccount(authentication, bankAccountDto);
        var uri = uriBuilder.path("bankAccount/{id}").buildAndExpand(bankAccount.getId()).toUri();
        return ResponseEntity.created(uri).body(new DetailsBankAccountDto(bankAccount));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw from bank account", description = "Endpoint to perform a withdrawal from the authenticated user's bank account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Withdrawal successfully performed.",
                    content = @Content(schema = @Schema(implementation = DetailsBankAccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to perform a withdrawal."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid withdrawal data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsBankAccountDto> withdraw(@RequestParam BigDecimal amount, Authentication authentication) {
        var bankAccount = bankAccountService.withdraw(authentication, amount);
        return ResponseEntity.ok().body(new DetailsBankAccountDto(bankAccount));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit to bank account", description = "Endpoint to perform a deposit to the authenticated user's bank account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deposit successfully performed.",
                    content = @Content(schema = @Schema(implementation = DetailsBankAccountDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized, user must be authenticated to perform a deposit."),
            @ApiResponse(responseCode = "404", description = "Bad request, invalid deposit data provided."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    public ResponseEntity<DetailsBankAccountDto> deposit(@RequestParam BigDecimal amount, Authentication authentication) {
        var bankAccount = bankAccountService.deposit(authentication, amount);
        return ResponseEntity.ok().body(new DetailsBankAccountDto(bankAccount));
}
